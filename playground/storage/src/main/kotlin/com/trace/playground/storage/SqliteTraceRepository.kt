package com.trace.playground.storage

import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.LocationInput
import com.trace.playground.contracts.MemoryResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.Sighting
import com.trace.playground.contracts.TraceRepository
import com.trace.playground.contracts.UsageRow
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.math.PI

private const val TAU = 2.0 * PI

class SqliteTraceRepository(
    private val databasePath: Path,
) : TraceRepository {
    private val jdbcUrl: String
        get() = "jdbc:sqlite:${databasePath.toAbsolutePath()}"

    override fun initialize() {
        databasePath.parent?.let(Files::createDirectories)
        connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys = ON")
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS objects (
                        id TEXT PRIMARY KEY,
                        tag TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS object_references (
                        id TEXT PRIMARY KEY,
                        object_id TEXT NOT NULL REFERENCES objects(id) ON DELETE CASCADE,
                        asset_path TEXT NOT NULL,
                        embedding TEXT NOT NULL,
                        model_name TEXT NOT NULL,
                        model_version TEXT NOT NULL,
                        quality_score REAL NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS sightings (
                        id TEXT PRIMARY KEY,
                        object_id TEXT NOT NULL REFERENCES objects(id) ON DELETE CASCADE,
                        detected_at INTEGER NOT NULL,
                        confidence REAL NOT NULL,
                        latitude REAL,
                        longitude REAL,
                        accuracy_meters REAL
                    )
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_objects_tag ON objects(tag)",
                )
                statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_sightings_object_time " +
                        "ON sightings(object_id, detected_at DESC)",
                )
            }
            migrateUsageTime(connection)
        }
    }

    private fun migrateUsageTime(connection: Connection) {
        val version = connection.createStatement().use { it.executeQuery("PRAGMA user_version") }
            .use { rows -> if (rows.next()) rows.getInt(1) else 0 }
        if (version >= 1) return

        val script = javaClass.getResourceAsStream("/db/migration/V1__create_usage_time.sql")
            ?: throw IllegalStateException("Missing migration resource V1__create_usage_time.sql")
        val sql = script.bufferedReader().use { it.readText() }

        val statements = sql.split(";").map { it.trim() }.filter { it.isNotEmpty() }

        connection.autoCommit = false
        try {
            statements.forEach { statementText ->
                connection.createStatement().use { it.executeUpdate(statementText) }
            }
            connection.createStatement().use { it.executeUpdate("PRAGMA user_version = 1") }
            connection.commit()
        } catch (failure: Exception) {
            connection.rollback()
            throw failure
        } finally {
            connection.autoCommit = true
        }
    }

    override fun saveEnrollment(result: EnrollmentResult, assetPath: String) {
        connection().use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement(
                    "INSERT INTO objects(id, tag, created_at) VALUES (?, ?, ?)",
                ).use { statement ->
                    statement.setString(1, result.objectId)
                    statement.setString(2, result.tag)
                    statement.setLong(3, System.currentTimeMillis())
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    """
                    INSERT INTO object_references(
                        id, object_id, asset_path, embedding, model_name,
                        model_version, quality_score, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, result.referenceId)
                    statement.setString(2, result.objectId)
                    statement.setString(3, assetPath)
                    statement.setString(4, result.embedding.values.joinToString(","))
                    statement.setString(5, result.embedding.modelName)
                    statement.setString(6, result.embedding.modelVersion)
                    statement.setFloat(7, result.qualityScore)
                    statement.setLong(8, System.currentTimeMillis())
                    statement.executeUpdate()
                }
                connection.commit()
            } catch (failure: Exception) {
                connection.rollback()
                throw failure
            }
        }
    }

    override fun references(): List<ReferenceVector> = connection().use { connection ->
        connection.prepareStatement(
            """
            SELECT r.id, r.object_id, o.tag, r.embedding, r.model_name, r.model_version
            FROM object_references r
            JOIN objects o ON o.id = r.object_id
            ORDER BY r.created_at DESC
            """.trimIndent(),
        ).use { statement ->
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            ReferenceVector(
                                referenceId = rows.getString("id"),
                                objectId = rows.getString("object_id"),
                                tag = rows.getString("tag"),
                                values = rows.getString("embedding").split(',').map(String::toFloat),
                                modelName = rows.getString("model_name"),
                                modelVersion = rows.getString("model_version"),
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun recordSighting(request: RecordSightingRequest): Sighting {
        require(request.confidence in 0f..1f) { "confidence must be between 0 and 1" }
        val sightingId = UUID.randomUUID().toString()
        connection().use { connection ->
            val tag = objectTag(connection, request.objectId)
                ?: throw IllegalArgumentException("objectId does not exist")
            connection.prepareStatement(
                """
                INSERT INTO sightings(
                    id, object_id, detected_at, confidence,
                    latitude, longitude, accuracy_meters
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, sightingId)
                statement.setString(2, request.objectId)
                statement.setLong(3, request.detectedAtEpochMillis)
                statement.setFloat(4, request.confidence)
                statement.setNullableDouble(5, request.location?.latitude)
                statement.setNullableDouble(6, request.location?.longitude)
                statement.setNullableDouble(7, request.location?.accuracyMeters?.toDouble())
                statement.executeUpdate()
            }
            val usage = usageVector(request.detectedAtEpochMillis)
            connection.prepareStatement(
                """
                INSERT INTO usage_time(
                    id, daytime_angle, weekday_angle, confidence_score, object_id
                ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, sightingId)
                statement.setDouble(2, usage.first)
                statement.setDouble(3, usage.second)
                statement.setFloat(4, request.confidence)
                statement.setString(5, request.objectId)
                statement.executeUpdate()
            }
            return Sighting(
                sightingId = sightingId,
                objectId = request.objectId,
                tag = tag,
                detectedAtEpochMillis = request.detectedAtEpochMillis,
                confidence = request.confidence,
                location = request.location,
            )
        }
    }

    override fun findObjects(query: String): List<MemoryResult> = connection().use { connection ->
        connection.prepareStatement(
            "SELECT id, tag FROM objects WHERE lower(tag) LIKE ? ORDER BY tag LIMIT 20",
        ).use { statement ->
            statement.setString(1, "%${query.trim().lowercase()}%")
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        val objectId = rows.getString("id")
                        add(
                            MemoryResult(
                                objectId = objectId,
                                tag = rows.getString("tag"),
                                lastSeen = latestSighting(connection, objectId),
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun timeline(objectId: String, limit: Int): List<Sighting> = connection().use { connection ->
        val tag = objectTag(connection, objectId)
            ?: throw IllegalArgumentException("objectId does not exist")
        connection.prepareStatement(
            """
            SELECT * FROM sightings
            WHERE object_id = ?
            ORDER BY detected_at DESC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, objectId)
            statement.setInt(2, limit.coerceIn(1, 200))
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.toSighting(tag))
                }
            }
        }
    }

    override fun usageRows(): List<UsageRow> = connection().use { connection ->
        connection.prepareStatement(
            """
            SELECT u.id, u.object_id, o.tag, u.daytime_angle, u.weekday_angle,
                   u.confidence_score, s.detected_at AS detected_at
            FROM usage_time u
            JOIN objects o ON o.id = u.object_id
            LEFT JOIN sightings s ON s.id = u.id
            ORDER BY u.id
            """.trimIndent(),
        ).use { statement ->
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            UsageRow(
                                id = rows.getString("id"),
                                objectId = rows.getString("object_id"),
                                tag = rows.getString("tag"),
                                daytimeAngle = rows.getDouble("daytime_angle"),
                                weekdayAngle = rows.getDouble("weekday_angle"),
                                confidenceScore = rows.getFloat("confidence_score"),
                                detectedAtEpochMillis = rows.getNullableLong("detected_at"),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun usageVector(epochMillis: Long): Pair<Double, Double> {
        val millisOfDay = Math.floorMod(epochMillis, 86_400_000L)
        val daytimeAngle = TAU * millisOfDay / 86_400_000.0
        val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
        val weekdayIndex = local.dayOfWeek.value % 7
        val weekdayAngle = TAU * weekdayIndex / 7.0
        return daytimeAngle to weekdayAngle
    }

    private fun latestSighting(connection: Connection, objectId: String): Sighting? {
        val tag = objectTag(connection, objectId) ?: return null
        return connection.prepareStatement(
            "SELECT * FROM sightings WHERE object_id = ? ORDER BY detected_at DESC LIMIT 1",
        ).use { statement ->
            statement.setString(1, objectId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toSighting(tag) else null }
        }
    }

    private fun objectTag(connection: Connection, objectId: String): String? =
        connection.prepareStatement("SELECT tag FROM objects WHERE id = ?").use { statement ->
            statement.setString(1, objectId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("tag") else null }
        }

    private fun ResultSet.toSighting(tag: String): Sighting {
        val latitude = getNullableDouble("latitude")
        val longitude = getNullableDouble("longitude")
        val location = if (latitude != null && longitude != null) {
            LocationInput(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = getNullableDouble("accuracy_meters")?.toFloat(),
            )
        } else {
            null
        }
        return Sighting(
            sightingId = getString("id"),
            objectId = getString("object_id"),
            tag = tag,
            detectedAtEpochMillis = getLong("detected_at"),
            confidence = getFloat("confidence"),
            location = location,
        )
    }

    private fun ResultSet.getNullableDouble(column: String): Double? =
        getDouble(column).let { value -> if (wasNull()) null else value }

    private fun ResultSet.getNullableLong(column: String): Long? =
        getLong(column).let { value -> if (wasNull()) null else value }

    private fun java.sql.PreparedStatement.setNullableDouble(index: Int, value: Double?) {
        if (value == null) setNull(index, java.sql.Types.REAL) else setDouble(index, value)
    }

    private fun connection(): Connection = DriverManager.getConnection(jdbcUrl).also { connection ->
        connection.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
    }
}
