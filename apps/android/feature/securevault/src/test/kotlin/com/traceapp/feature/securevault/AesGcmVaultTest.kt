package com.traceapp.feature.securevault

import com.google.common.truth.Truth.assertThat
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.assertThrows
import org.junit.Test

class AesGcmVaultTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val vault = AesGcmVault(FixedKeyProvider(key))
    private val context = VaultContext("account-1", "record-1", "REFERENCE_IMAGE")

    @Test
    fun roundTrip_returnsOriginalPlaintext() {
        val plaintext = "TRACE dữ liệu bí mật".toByteArray()
        val opened = vault.open(vault.seal(plaintext, context), context)
        assertThat(opened).isEqualTo(plaintext)
    }

    @Test
    fun samePlaintext_usesDifferentNoncesAndCiphertext() {
        val first = vault.seal("same".toByteArray(), context)
        val second = vault.seal("same".toByteArray(), context)
        assertThat(first.nonce).isNotEqualTo(second.nonce)
        assertThat(first.cipherTextAndTag).isNotEqualTo(second.cipherTextAndTag)
    }

    @Test
    fun tamperedCiphertext_isRejected() {
        val envelope = vault.seal("secret".toByteArray(), context)
        envelope.cipherTextAndTag[0] = (envelope.cipherTextAndTag[0].toInt() xor 1).toByte()
        val failure = assertThrows(VaultException::class.java) { vault.open(envelope, context) }
        assertThat(failure.reason).isEqualTo(VaultFailureReason.AUTHENTICATION_FAILED)
    }

    @Test
    fun wrongContext_isRejected() {
        val envelope = vault.seal("secret".toByteArray(), context)
        val failure = assertThrows(VaultException::class.java) {
            vault.open(envelope, context.copy(recordId = "record-2"))
        }
        assertThat(failure.reason).isEqualTo(VaultFailureReason.AUTHENTICATION_FAILED)
    }

    @Test
    fun wrongKey_isRejected() {
        val envelope = vault.seal("secret".toByteArray(), context)
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val otherVault = AesGcmVault(FixedKeyProvider(otherKey))
        assertThrows(VaultException::class.java) { otherVault.open(envelope, context) }
    }

    @Test
    fun tenThousandSeals_doNotRepeatNonceInTestRun() {
        val seen = HashSet<String>()
        repeat(10_000) {
            val nonce = vault.seal(byteArrayOf(1), context).nonce.joinToString("") { "%02x".format(it) }
            assertThat(seen.add(nonce)).isTrue()
        }
    }

    @Test
    fun envelopeCodec_roundTripsAndRejectsTrailingBytes() {
        val envelope = vault.seal(ByteArray(64).also(SecureRandom()::nextBytes), context)
        val decoded = VaultEnvelopeCodec.decode(VaultEnvelopeCodec.encode(envelope))
        assertThat(decoded.envelopeVersion).isEqualTo(envelope.envelopeVersion)
        assertThat(decoded.algorithm).isEqualTo(envelope.algorithm)
        assertThat(decoded.keyId).isEqualTo(envelope.keyId)
        assertThat(decoded.nonce).isEqualTo(envelope.nonce)
        assertThat(decoded.cipherTextAndTag).isEqualTo(envelope.cipherTextAndTag)
        assertThrows(VaultException::class.java) {
            VaultEnvelopeCodec.decode(VaultEnvelopeCodec.encode(envelope) + 0)
        }
    }

    private class FixedKeyProvider(private val key: SecretKey) : VaultKeyProvider {
        override fun activeKey() = VaultKey("v1", key)
        override fun findKey(keyId: String) = if (keyId == "v1") key else null
    }
}
