package com.traceapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalObjectEntity::class,
        SecureAssetEntity::class,
        LocalObjectReferenceEntity::class,
        LocalReferenceEmbeddingEntity::class,
        LocalSightingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TraceDatabase : RoomDatabase() {
    abstract fun objectDao(): ObjectDao
    abstract fun sightingDao(): SightingDao
    abstract fun secureAssetDao(): SecureAssetDao
}
