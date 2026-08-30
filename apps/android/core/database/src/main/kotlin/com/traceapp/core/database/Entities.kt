package com.traceapp.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "local_objects")
data class LocalObjectEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "object_id")
    val objectId: String,
    @ColumnInfo(name = "encrypted_tag", typeAffinity = ColumnInfo.BLOB)
    val encryptedTag: ByteArray,
    @ColumnInfo(name = "reference_revision")
    val referenceRevision: Int,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMillis: Long,
)

@Entity(tableName = "secure_assets")
data class SecureAssetEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "asset_id")
    val assetId: String,
    val type: String,
    @ColumnInfo(name = "relative_path")
    val relativePath: String,
    @ColumnInfo(name = "nonce", typeAffinity = ColumnInfo.BLOB)
    val nonce: ByteArray,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "local_object_references",
    foreignKeys = [
        ForeignKey(
            entity = LocalObjectEntity::class,
            parentColumns = ["object_id"],
            childColumns = ["object_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SecureAssetEntity::class,
            parentColumns = ["asset_id"],
            childColumns = ["image_asset_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("object_id"), Index("image_asset_id")],
)
data class LocalObjectReferenceEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "reference_id")
    val referenceId: String,
    @ColumnInfo(name = "object_id")
    val objectId: String,
    @ColumnInfo(name = "image_asset_id")
    val imageAssetId: String,
    @ColumnInfo(name = "roi_left")
    val roiLeft: Float,
    @ColumnInfo(name = "roi_top")
    val roiTop: Float,
    @ColumnInfo(name = "roi_right")
    val roiRight: Float,
    @ColumnInfo(name = "roi_bottom")
    val roiBottom: Float,
    @ColumnInfo(name = "quality_score")
    val qualityScore: Float,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "local_reference_embeddings",
    primaryKeys = ["reference_id", "ordinal"],
    foreignKeys = [
        ForeignKey(
            entity = LocalObjectReferenceEntity::class,
            parentColumns = ["reference_id"],
            childColumns = ["reference_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("reference_id")],
)
data class LocalReferenceEmbeddingEntity(
    @ColumnInfo(name = "reference_id")
    val referenceId: String,
    val ordinal: Int,
    @ColumnInfo(name = "encrypted_values", typeAffinity = ColumnInfo.BLOB)
    val encryptedValues: ByteArray,
    val dimensions: Int,
    @ColumnInfo(name = "model_name")
    val modelName: String,
    @ColumnInfo(name = "model_version")
    val modelVersion: String,
)

@Entity(
    tableName = "local_sightings",
    foreignKeys = [
        ForeignKey(
            entity = LocalObjectEntity::class,
            parentColumns = ["object_id"],
            childColumns = ["object_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("object_id"),
        Index(value = ["object_id", "detected_at_epoch_ms"]),
        Index("sync_status"),
    ],
)
data class LocalSightingEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "sighting_id")
    val sightingId: String,
    @ColumnInfo(name = "object_id")
    val objectId: String,
    @ColumnInfo(name = "detected_at_epoch_ms")
    val detectedAtEpochMillis: Long,
    @ColumnInfo(name = "encrypted_location", typeAffinity = ColumnInfo.BLOB)
    val encryptedLocation: ByteArray?,
    val confidence: Float,
    @ColumnInfo(name = "evidence_asset_id")
    val evidenceAssetId: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String,
)
