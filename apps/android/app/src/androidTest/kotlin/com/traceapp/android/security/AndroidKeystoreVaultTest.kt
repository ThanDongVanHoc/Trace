package com.traceapp.android.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.traceapp.feature.securevault.AesGcmVault
import com.traceapp.feature.securevault.AndroidKeystoreKeyProvider
import com.traceapp.feature.securevault.VaultContext
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreVaultTest {
    @Test
    fun sealAndOpen_roundTripsWithHardwareBackedApi() {
        val vault = AesGcmVault(AndroidKeystoreKeyProvider())
        val context = VaultContext("account", "record", "REFERENCE_IMAGE:image/jpeg")
        val plaintext = "private image bytes".encodeToByteArray()

        val envelope = vault.seal(plaintext, context)
        val restored = vault.open(envelope, context)

        assertThat(envelope.nonce).hasLength(12)
        assertThat(restored).isEqualTo(plaintext)
    }
}
