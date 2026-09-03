package com.traceapp.feature.securevault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeystoreKeyProvider @Inject constructor() : VaultKeyProvider {
    private val lock = Any()

    override fun activeKey(): VaultKey = synchronized(lock) {
        VaultKey(ACTIVE_KEY_ID, findKey(ACTIVE_KEY_ID) ?: generate(ACTIVE_KEY_ID))
    }

    override fun findKey(keyId: String): SecretKey? = synchronized(lock) {
        if (keyId !in SUPPORTED_KEY_IDS) return@synchronized null
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        store.getKey(alias(keyId), null) as? SecretKey
    }

    private fun generate(keyId: String): SecretKey =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    alias(keyId),
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }

    private fun alias(keyId: String): String = "trace_secure_vault_$keyId"

    companion object {
        const val ACTIVE_KEY_ID = "v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private val SUPPORTED_KEY_IDS = setOf(ACTIVE_KEY_ID)
    }
}
