package com.traceapp.feature.securevault

import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SightingStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecureVaultModule {
    @Binds
    @Singleton
    abstract fun bindObjectStore(implementation: InMemoryObjectStore): ObjectStore

    @Binds
    @Singleton
    abstract fun bindSightingStore(implementation: InMemorySightingStore): SightingStore

    @Binds
    @Singleton
    abstract fun bindAssetStore(implementation: InMemoryAssetStore): SecureAssetStore
}
