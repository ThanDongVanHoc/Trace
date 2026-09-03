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
    abstract fun bindObjectStore(implementation: RoomObjectStore): ObjectStore

    @Binds
    @Singleton
    abstract fun bindSightingStore(implementation: RoomSightingStore): SightingStore

    @Binds
    @Singleton
    abstract fun bindAssetStore(implementation: EncryptedSecureAssetStore): SecureAssetStore

    @Binds
    @Singleton
    abstract fun bindCryptoVault(implementation: AesGcmVault): CryptoVault

    @Binds
    @Singleton
    abstract fun bindKeyProvider(implementation: AndroidKeystoreKeyProvider): VaultKeyProvider
}
