package com.traceapp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface ObjectDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertObject(entity: LocalObjectEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReference(entity: LocalObjectReferenceEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmbeddings(entities: List<LocalReferenceEmbeddingEntity>)

    @Transaction
    suspend fun insertGraph(
        objectEntity: LocalObjectEntity,
        referenceEntity: LocalObjectReferenceEntity,
        embeddings: List<LocalReferenceEmbeddingEntity>,
    ) {
        insertObject(objectEntity)
        insertReference(referenceEntity)
        insertEmbeddings(embeddings)
    }

    @Query("SELECT * FROM local_objects WHERE account_id = :accountId AND object_id = :objectId")
    suspend fun getObject(accountId: String, objectId: String): LocalObjectEntity?

    @Query("SELECT * FROM local_object_references WHERE object_id = :objectId ORDER BY created_at_epoch_ms DESC")
    suspend fun getReferences(objectId: String): List<LocalObjectReferenceEntity>

    @Query(
        """
        SELECT r.* FROM local_object_references r
        INNER JOIN local_objects o ON o.object_id = r.object_id
        WHERE o.account_id = :accountId
        ORDER BY r.created_at_epoch_ms DESC
        """,
    )
    suspend fun getAllReferences(accountId: String): List<LocalObjectReferenceEntity>

    @Query("SELECT * FROM local_reference_embeddings WHERE reference_id = :referenceId ORDER BY ordinal")
    suspend fun getEmbeddings(referenceId: String): List<LocalReferenceEmbeddingEntity>

    @Query("DELETE FROM local_objects WHERE account_id = :accountId AND object_id = :objectId")
    suspend fun deleteObject(accountId: String, objectId: String): Int
}

@Dao
interface SightingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LocalSightingEntity)

    @Update
    suspend fun update(entity: LocalSightingEntity)

    @Query(
        """
        SELECT s.* FROM local_sightings s
        INNER JOIN local_objects o ON o.object_id = s.object_id
        WHERE o.account_id = :accountId AND s.object_id = :objectId
        ORDER BY s.detected_at_epoch_ms DESC LIMIT 1
        """,
    )
    suspend fun getLatest(accountId: String, objectId: String): LocalSightingEntity?

    @Query(
        """
        SELECT s.* FROM local_sightings s
        INNER JOIN local_objects o ON o.object_id = s.object_id
        WHERE o.account_id = :accountId AND s.object_id = :objectId
        ORDER BY s.detected_at_epoch_ms DESC LIMIT :limit
        """,
    )
    suspend fun getTimeline(accountId: String, objectId: String, limit: Int): List<LocalSightingEntity>

    @Query(
        """
        SELECT s.* FROM local_sightings s
        INNER JOIN local_objects o ON o.object_id = s.object_id
        WHERE o.account_id = :accountId AND s.sync_status = 'PENDING'
        ORDER BY s.detected_at_epoch_ms LIMIT :limit
        """,
    )
    suspend fun getPending(accountId: String, limit: Int): List<LocalSightingEntity>

    @Query(
        """
        SELECT s.detected_at_epoch_ms FROM local_sightings s
        INNER JOIN local_objects o ON o.object_id = s.object_id
        WHERE o.account_id = :accountId
          AND s.detected_at_epoch_ms >= :fromEpochMillis
          AND s.detected_at_epoch_ms < :toEpochMillis
        ORDER BY s.detected_at_epoch_ms
        """,
    )
    suspend fun getAllTimestamps(
        accountId: String,
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): List<Long>

    @Query(
        """
        SELECT s.detected_at_epoch_ms FROM local_sightings s
        INNER JOIN local_objects o ON o.object_id = s.object_id
        WHERE o.account_id = :accountId AND s.object_id = :objectId
          AND s.detected_at_epoch_ms >= :fromEpochMillis
          AND s.detected_at_epoch_ms < :toEpochMillis
        ORDER BY s.detected_at_epoch_ms
        """,
    )
    suspend fun getObjectTimestamps(
        accountId: String,
        objectId: String,
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): List<Long>
}

@Dao
interface SecureAssetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: SecureAssetEntity)

    @Query("SELECT * FROM secure_assets WHERE account_id = :accountId AND asset_id = :assetId")
    suspend fun get(accountId: String, assetId: String): SecureAssetEntity?

    @Query("DELETE FROM secure_assets WHERE account_id = :accountId AND asset_id = :assetId")
    suspend fun delete(accountId: String, assetId: String): Int
}
