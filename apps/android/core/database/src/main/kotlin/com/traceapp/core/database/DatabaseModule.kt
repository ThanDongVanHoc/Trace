package com.traceapp.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TraceDatabase =
        Room.databaseBuilder(context, TraceDatabase::class.java, "trace.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun provideObjectDao(database: TraceDatabase): ObjectDao = database.objectDao()
    @Provides fun provideSightingDao(database: TraceDatabase): SightingDao = database.sightingDao()
    @Provides fun provideAssetDao(database: TraceDatabase): SecureAssetDao = database.secureAssetDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE local_objects ADD COLUMN account_id TEXT NOT NULL DEFAULT 'legacy-local'",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_local_objects_account_id ON local_objects(account_id)")
            db.execSQL(
                "ALTER TABLE secure_assets ADD COLUMN account_id TEXT NOT NULL DEFAULT 'legacy-local'",
            )
            db.execSQL(
                "ALTER TABLE secure_assets ADD COLUMN owner_record_id TEXT NOT NULL DEFAULT ''",
            )
            db.execSQL(
                "ALTER TABLE secure_assets ADD COLUMN envelope_version INTEGER NOT NULL DEFAULT 1",
            )
            db.execSQL(
                "ALTER TABLE secure_assets ADD COLUMN algorithm TEXT NOT NULL DEFAULT 'AES-256-GCM'",
            )
            db.execSQL(
                "ALTER TABLE secure_assets ADD COLUMN key_id TEXT NOT NULL DEFAULT 'trace-vault-key-v1'",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_secure_assets_account_id ON secure_assets(account_id)")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_secure_assets_owner_record_id ON secure_assets(owner_record_id)",
            )
        }
    }
}
