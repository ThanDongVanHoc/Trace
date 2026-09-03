package com.traceapp.android.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.traceapp.core.auth.AuthResult
import com.traceapp.core.auth.LocalAuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalAuthRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun clearAccounts() {
        check(context.getSharedPreferences("trace_local_accounts", Context.MODE_PRIVATE).edit().clear().commit())
    }

    @Test
    fun registerRestoreAndLogin_roundTripsOnAndroidStorage() = runBlocking {
        val repository = LocalAuthRepository(context)

        val registered = repository.register("Minh Nguyen", " MINH@EXAMPLE.COM ", "trace123")
        assertThat(registered).isInstanceOf(AuthResult.Success::class.java)
        assertThat((registered as AuthResult.Success).value.email).isEqualTo("minh@example.com")

        val restored = repository.restore() as AuthResult.Success
        assertThat(restored.value?.displayName).isEqualTo("Minh Nguyen")

        repository.logout()
        val loggedIn = repository.login("minh@example.com", "trace123") as AuthResult.Success
        assertThat(loggedIn.value.id).isEqualTo(registered.value.id)
    }

    @Test
    fun duplicateEmail_returnsReadableFailure() = runBlocking {
        val repository = LocalAuthRepository(context)
        repository.register("Minh Nguyen", "minh@example.com", "trace123")

        val result = repository.register("Another User", "MINH@example.com", "trace456")

        assertThat(result).isInstanceOf(AuthResult.Failure::class.java)
        assertThat((result as AuthResult.Failure).message).contains("đã được đăng ký")
    }
}
