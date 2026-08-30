package com.traceapp.core.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochMillis: Long,
)

interface TokenStore {
    fun read(): SessionTokens?
    fun write(tokens: SessionTokens)
    fun clear()
}

@Singleton
class KeystoreTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) : TokenStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): SessionTokens? = runCatching {
        val encoded = preferences.getString(SESSION_KEY, null) ?: return null
        val parts = encoded.split(':', limit = 2)
        require(parts.size == 2)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)),
        )
        val plaintext = String(
            cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)),
            StandardCharsets.UTF_8,
        )
        val fields = plaintext.split('\n', limit = 3)
        require(fields.size == 3)
        SessionTokens(fields[0], fields[1], fields[2].toLong())
    }.getOrElse {
        clear()
        null
    }

    override fun write(tokens: SessionTokens) {
        val plaintext = listOf(
            tokens.accessToken,
            tokens.refreshToken,
            tokens.expiresAtEpochMillis.toString(),
        ).joinToString("\n").toByteArray(StandardCharsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encoded = buildString {
            append(Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            append(':')
            append(Base64.encodeToString(cipher.doFinal(plaintext), Base64.NO_WRAP))
        }
        check(preferences.edit().putString(SESSION_KEY, encoded).commit())
    }

    override fun clear() {
        preferences.edit().remove(SESSION_KEY).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "trace_auth"
        const val SESSION_KEY = "encrypted_session"
        const val KEY_ALIAS = "trace_auth_session_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
