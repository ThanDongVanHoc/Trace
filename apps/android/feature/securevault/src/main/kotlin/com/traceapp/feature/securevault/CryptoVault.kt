package com.traceapp.feature.securevault

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

data class VaultContext(
    val accountId: String,
    val recordId: String,
    val field: String,
    val schemaVersion: Int = 1,
)

data class VaultEnvelope(
    val envelopeVersion: Int,
    val algorithm: String,
    val keyId: String,
    val nonce: ByteArray,
    val cipherTextAndTag: ByteArray,
)

data class VaultKey(val keyId: String, val secretKey: SecretKey)

interface VaultKeyProvider {
    fun activeKey(): VaultKey
    fun findKey(keyId: String): SecretKey?
}

interface CryptoVault {
    fun seal(plaintext: ByteArray, context: VaultContext): VaultEnvelope
    fun open(envelope: VaultEnvelope, expectedContext: VaultContext): ByteArray
}

enum class VaultFailureReason {
    AUTHENTICATION_FAILED,
    KEY_UNAVAILABLE,
    UNSUPPORTED_ENVELOPE,
    INVALID_INPUT,
}

class VaultException(
    val reason: VaultFailureReason,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class AesGcmVault @Inject constructor(
    private val keyProvider: VaultKeyProvider,
) : CryptoVault {
    private val random = SecureRandom()

    override fun seal(plaintext: ByteArray, context: VaultContext): VaultEnvelope {
        validateContext(context)
        val key = keyProvider.activeKey()
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key.secretKey, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(VaultContextCodec.encode(context))
        return VaultEnvelope(
            envelopeVersion = ENVELOPE_VERSION,
            algorithm = ALGORITHM_ID,
            keyId = key.keyId,
            nonce = nonce,
            cipherTextAndTag = cipher.doFinal(plaintext),
        )
    }

    override fun open(envelope: VaultEnvelope, expectedContext: VaultContext): ByteArray {
        validateContext(expectedContext)
        if (
            envelope.envelopeVersion != ENVELOPE_VERSION ||
            envelope.algorithm != ALGORITHM_ID ||
            envelope.nonce.size != NONCE_BYTES ||
            envelope.cipherTextAndTag.size < TAG_BYTES
        ) {
            throw VaultException(
                VaultFailureReason.UNSUPPORTED_ENVELOPE,
                "Encrypted payload format is not supported",
            )
        }
        val key = keyProvider.findKey(envelope.keyId)
            ?: throw VaultException(VaultFailureReason.KEY_UNAVAILABLE, "Encryption key is unavailable")
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, envelope.nonce))
            cipher.updateAAD(VaultContextCodec.encode(expectedContext))
            cipher.doFinal(envelope.cipherTextAndTag)
        } catch (failure: AEADBadTagException) {
            throw VaultException(
                VaultFailureReason.AUTHENTICATION_FAILED,
                "Encrypted payload authentication failed",
                failure,
            )
        }
    }

    private fun validateContext(context: VaultContext) {
        if (
            context.accountId.isBlank() || context.recordId.isBlank() ||
            context.field.isBlank() || context.schemaVersion <= 0
        ) {
            throw VaultException(VaultFailureReason.INVALID_INPUT, "Vault context is incomplete")
        }
    }

    companion object {
        const val ENVELOPE_VERSION = 1
        const val ALGORITHM_ID = "AES-256-GCM"
        const val NONCE_BYTES = 12
        private const val TAG_BYTES = 16
        private const val TAG_BITS = 128
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

object VaultContextCodec {
    fun encode(context: VaultContext): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(CONTEXT_MAGIC)
            output.writeInt(context.schemaVersion)
            output.writeSizedUtf8(context.accountId)
            output.writeSizedUtf8(context.recordId)
            output.writeSizedUtf8(context.field)
        }
        bytes.toByteArray()
    }

    private fun DataOutputStream.writeSizedUtf8(value: String) {
        val encoded = value.toByteArray(Charsets.UTF_8)
        require(encoded.size <= MAX_CONTEXT_FIELD_BYTES)
        writeInt(encoded.size)
        write(encoded)
    }

    private const val CONTEXT_MAGIC = 0x54524358
    private const val MAX_CONTEXT_FIELD_BYTES = 4_096
}

object VaultEnvelopeCodec {
    fun encode(envelope: VaultEnvelope): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(ENVELOPE_MAGIC)
            output.writeInt(envelope.envelopeVersion)
            output.writeSizedUtf8(envelope.algorithm)
            output.writeSizedUtf8(envelope.keyId)
            output.writeSizedBytes(envelope.nonce)
            output.writeSizedBytes(envelope.cipherTextAndTag)
        }
        bytes.toByteArray()
    }

    fun decode(encoded: ByteArray): VaultEnvelope = try {
        require(encoded.size <= MAX_ENVELOPE_BYTES)
        DataInputStream(ByteArrayInputStream(encoded)).use { input ->
            require(input.readInt() == ENVELOPE_MAGIC)
            val result = VaultEnvelope(
                envelopeVersion = input.readInt(),
                algorithm = input.readSizedUtf8(MAX_TEXT_BYTES),
                keyId = input.readSizedUtf8(MAX_TEXT_BYTES),
                nonce = input.readSizedBytes(AesGcmVault.NONCE_BYTES),
                cipherTextAndTag = input.readSizedBytes(MAX_ENVELOPE_BYTES),
            )
            require(input.available() == 0)
            result
        }
    } catch (failure: Exception) {
        throw VaultException(
            VaultFailureReason.UNSUPPORTED_ENVELOPE,
            "Encrypted payload cannot be parsed",
            failure,
        )
    }

    private fun DataOutputStream.writeSizedUtf8(value: String) =
        writeSizedBytes(value.toByteArray(Charsets.UTF_8))

    private fun DataOutputStream.writeSizedBytes(value: ByteArray) {
        writeInt(value.size)
        write(value)
    }

    private fun DataInputStream.readSizedUtf8(maxBytes: Int): String =
        String(readSizedBytes(maxBytes), Charsets.UTF_8)

    private fun DataInputStream.readSizedBytes(maxBytes: Int): ByteArray {
        val length = readInt()
        require(length in 0..maxBytes && length <= available())
        return ByteArray(length).also(::readFully)
    }

    private const val ENVELOPE_MAGIC = 0x54525631
    private const val MAX_TEXT_BYTES = 256
    private const val MAX_ENVELOPE_BYTES = 16 * 1024 * 1024
}
