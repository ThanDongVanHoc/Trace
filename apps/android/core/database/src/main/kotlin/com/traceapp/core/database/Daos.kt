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

    @Query("SELECT * FROM local_objects WHERE object_id = :objectId")
    suspend fun getObject(objectId: String): LocalObjectEntity?

    @Query("SELECT * FROM local_object_references WHERE object_id = :objectId ORDER BY created_at_epoch_ms DESC")
    suspend fun getReferences(objectId: String): List<LocalObjectReferenceEntity>

    @Query("SELECT * FROM local_object_references ORDER BY created_at_epoch_ms DESC")
    suspend fun getAllReferences(): List<LocalObjectReferenceEntity>

    @Query("SELECT * FROM local_reference_embeddings WHERE reference_id = :referenceId ORDER BY ordinal")
    suspend fun getEmbeddings(referenceId: String): List<LocalReferenceEmbeddingEntity>

    @Query("DELETE FROM local_objects WHERE object_id = :objectId")
    suspend fun deleteObject(objectId: String): Int
}

@Dao
interface SightingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LocalSightingEntity)

    @Update
    suspend fun update(entity: LocalSightingEntity)

    @Query("SELECT * FROM local_sightings WHERE object_id = :objectId ORDER BY detected_at_epoch_ms DESC LIMIT 1")
    suspend fun getLatest(objectId: String): LocalSightingEntity?

    @Query("SELECT * FROM local_sightings WHERE object_id = :objectId ORDER BY detected_at_epoch_ms DESC LIMIT :limit")
    suspend fun getTimeline(objectId: String, limit: Int): List<LocalSightingEntity>

    @Query("SELECT * FROM local_sightings WHERE sync_status = 'PENDING' ORDER BY detected_at_epoch_ms LIMIT :limit")
    suspend fun getPending(limit: Int): List<LocalSightingEntity>
}

@Dao
interface SecureAssetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: SecureAssetEntity)

    @Query("SELECT * FROM secure_assets WHERE asset_id = :assetId")
    suspend fun get(assetId: String): SecureAssetEntity?

    @Query("DELETE FROM secure_assets WHERE asset_id = :assetId")
    suspend fun delete(assetId: String): Int
}
