package com.trace.playground.vault

import com.trace.playground.contracts.OpenResult
import com.trace.playground.contracts.SealRequest
import com.trace.playground.contracts.SealedPayload
import com.trace.playground.contracts.VaultEngine
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Thành viên 4 thay key management và envelope design tại đây. */
class VaultAlgorithm(
    private val key: SecretKey = generateKey(),
) : VaultEngine {
    private val random = SecureRandom()

    override fun seal(request: SealRequest): SealedPayload {
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        request.aad?.let { cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8)) }
        val encrypted = cipher.doFinal(request.plainText.toByteArray(StandardCharsets.UTF_8))
        return SealedPayload(
            algorithm = "AES-256-GCM",
            keyId = "dev-ephemeral-key",
            nonceBase64 = Base64.getEncoder().encodeToString(nonce),
            cipherTextBase64 = Base64.getEncoder().encodeToString(encrypted),
            aad = request.aad,
        )
    }

    override fun open(payload: SealedPayload): OpenResult {
        require(payload.algorithm == "AES-256-GCM") { "unsupported algorithm" }
        val nonce = Base64.getDecoder().decode(payload.nonceBase64)
        require(nonce.size == NONCE_BYTES) { "invalid nonce" }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        payload.aad?.let { cipher.updateAAD(it.toByteArray(StandardCharsets.UTF_8)) }
        return try {
            OpenResult(String(cipher.doFinal(Base64.getDecoder().decode(payload.cipherTextBase64))))
        } catch (failure: AEADBadTagException) {
            throw IllegalArgumentException("authentication failed: payload or AAD was modified", failure)
        }
    }

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128

        private fun generateKey(): SecretKey = KeyGenerator.getInstance("AES").run {
            init(256)
            generateKey()
        }
    }
}
