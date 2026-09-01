package com.trace.playground.vault

import com.trace.playground.contracts.SealRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VaultAlgorithmTest {
    @Test
    fun `round trips authenticated plaintext`() {
        val vault = VaultAlgorithm()
        val payload = vault.seal(SealRequest("secret", "object:1"))
        assertEquals("secret", vault.open(payload).plainText)
    }

    @Test
    fun `rejects modified aad`() {
        val vault = VaultAlgorithm()
        val payload = vault.seal(SealRequest("secret", "object:1"))
        assertFailsWith<IllegalArgumentException> { vault.open(payload.copy(aad = "object:2")) }
    }
}
