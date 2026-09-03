package com.traceapp.core.auth

import android.content.Context
import android.util.Base64
import android.util.Patterns
import com.traceapp.core.contracts.AccountSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
)

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val message: String, val unauthorized: Boolean = false) : AuthResult<Nothing>
}

interface AuthRepository {
    suspend fun restore(): AuthResult<AuthUser?>
    suspend fun login(email: String, password: String): AuthResult<AuthUser>
    suspend fun register(displayName: String, email: String, password: String): AuthResult<AuthUser>
    suspend fun logout()
}

/**
 * Standalone account repository. Password verifiers and the active session never leave the device.
 * This is an app lock/account partition, not a claim of remote identity verification.
 */
@Singleton
class LocalAuthRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AuthRepository, AccountSession {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private val lock = Any()

    override fun currentAccountId(): String? = preferences.getString(SESSION_ACCOUNT_ID, null)

    override suspend fun restore(): AuthResult<AuthUser?> = synchronized(lock) {
        val accountId = currentAccountId() ?: return@synchronized AuthResult.Success(null)
        val account = allAccounts().firstOrNull { it.user.id == accountId }
        if (account == null) {
            preferences.edit().remove(SESSION_ACCOUNT_ID).commit()
            AuthResult.Success(null)
        } else {
            AuthResult.Success(account.user)
        }
    }

    override suspend fun login(email: String, password: String): AuthResult<AuthUser> = synchronized(lock) {
        val normalizedEmail = normalizeEmail(email)
        val account = readAccount(normalizedEmail)
            ?: return@synchronized AuthResult.Failure("Email hoặc mật khẩu không đúng.")
        val candidate = derivePassword(password, account.salt, account.iterations)
        if (!MessageDigest.isEqual(candidate, account.passwordVerifier)) {
            return@synchronized AuthResult.Failure("Email hoặc mật khẩu không đúng.")
        }
        check(preferences.edit().putString(SESSION_ACCOUNT_ID, account.user.id).commit())
        AuthResult.Success(account.user)
    }

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): AuthResult<AuthUser> = synchronized(lock) {
        val name = displayName.trim()
        val normalizedEmail = normalizeEmail(email)
        when {
            name.length !in 2..80 -> return@synchronized AuthResult.Failure("Tên phải có từ 2 đến 80 ký tự.")
            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() ->
                return@synchronized AuthResult.Failure("Email không hợp lệ.")
            password.length < 10 -> return@synchronized AuthResult.Failure("Mật khẩu cần ít nhất 10 ký tự.")
            readAccount(normalizedEmail) != null ->
                return@synchronized AuthResult.Failure("Email này đã được đăng ký trên thiết bị.")
        }

        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val user = AuthUser(UUID.randomUUID().toString(), normalizedEmail, name)
        val account = StoredAccount(
            user = user,
            salt = salt,
            passwordVerifier = derivePassword(password, salt, PBKDF2_ITERATIONS),
            iterations = PBKDF2_ITERATIONS,
        )
        check(
            preferences.edit()
                .putString(accountKey(normalizedEmail), encode(account))
                .putString(SESSION_ACCOUNT_ID, user.id)
                .commit(),
        )
        AuthResult.Success(user)
    }

    override suspend fun logout() {
        preferences.edit().remove(SESSION_ACCOUNT_ID).commit()
    }

    private fun readAccount(email: String): StoredAccount? =
        preferences.getString(accountKey(email), null)?.let(::decode)

    private fun allAccounts(): List<StoredAccount> = preferences.all
        .filterKeys { it.startsWith(ACCOUNT_PREFIX) }
        .values
        .mapNotNull { (it as? String)?.let(::decode) }

    private fun derivePassword(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_BITS)
        return try {
            SecretKeyFactory.getInstance(PBKDF2_ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(account: StoredAccount): String = listOf(
        FORMAT_VERSION,
        account.user.id,
        encodeText(account.user.email),
        encodeText(account.user.displayName),
        encodeBytes(account.salt),
        encodeBytes(account.passwordVerifier),
        account.iterations.toString(),
    ).joinToString(DELIMITER)

    private fun decode(value: String): StoredAccount? = runCatching {
        val fields = value.split(DELIMITER)
        require(fields.size == 7 && fields[0] == FORMAT_VERSION)
        StoredAccount(
            user = AuthUser(fields[1], decodeText(fields[2]), decodeText(fields[3])),
            salt = decodeBytes(fields[4]),
            passwordVerifier = decodeBytes(fields[5]),
            iterations = fields[6].toInt(),
        )
    }.getOrNull()

    private fun accountKey(email: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(email.toByteArray(StandardCharsets.UTF_8))
        return ACCOUNT_PREFIX + encodeBytes(digest)
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)
    private fun encodeText(value: String): String = encodeBytes(value.toByteArray(StandardCharsets.UTF_8))
    private fun decodeText(value: String): String = String(decodeBytes(value), StandardCharsets.UTF_8)
    private fun encodeBytes(value: ByteArray): String =
        Base64.encodeToString(value, Base64.NO_WRAP or Base64.URL_SAFE)
    private fun decodeBytes(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP or Base64.URL_SAFE)

    private data class StoredAccount(
        val user: AuthUser,
        val salt: ByteArray,
        val passwordVerifier: ByteArray,
        val iterations: Int,
    )

    private companion object {
        const val PREFERENCES_NAME = "trace_local_accounts"
        const val SESSION_ACCOUNT_ID = "active_account_id"
        const val ACCOUNT_PREFIX = "account_"
        const val FORMAT_VERSION = "v1"
        const val DELIMITER = ":"
        const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1"
        const val PBKDF2_ITERATIONS = 210_000
        const val PASSWORD_BITS = 256
        const val SALT_BYTES = 16
    }
}
