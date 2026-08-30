package com.traceapp.core.network

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

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

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: TraceBackendApi,
    private val tokenStore: TokenStore,
) : AuthRepository {
    override suspend fun restore(): AuthResult<AuthUser?> {
        if (tokenStore.read() == null) return AuthResult.Success(null)
        return request { api.me().toDomain() }
    }

    override suspend fun login(email: String, password: String): AuthResult<AuthUser> = request {
        api.login(LoginRequestDto(email.trim(), password)).also(::persist).user.toDomain()
    }

    override suspend fun register(
        displayName: String,
        email: String,
        password: String,
    ): AuthResult<AuthUser> = request {
        api.register(RegisterRequestDto(email.trim(), password, displayName.trim()))
            .also(::persist)
            .user
            .toDomain()
    }

    override suspend fun logout() {
        runCatching { api.logout() }
        tokenStore.clear()
    }

    private fun persist(response: AuthResponseDto) {
        tokenStore.write(
            SessionTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresAtEpochMillis = System.currentTimeMillis() + response.expiresInSeconds * 1_000,
            ),
        )
    }

    private suspend fun <T> request(block: suspend () -> T): AuthResult<T> = try {
        AuthResult.Success(block())
    } catch (error: HttpException) {
        if (error.code() == 401) tokenStore.clear()
        AuthResult.Failure(
            message = when (error.code()) {
                401 -> "Email hoặc mật khẩu không đúng."
                409 -> "Email này đã được đăng ký."
                else -> "Máy chủ trả về lỗi ${error.code()}."
            },
            unauthorized = error.code() == 401,
        )
    } catch (_: IOException) {
        AuthResult.Failure("Không kết nối được máy chủ. Hãy kiểm tra backend và mạng.")
    } catch (_: Exception) {
        AuthResult.Failure("Đã có lỗi không mong đợi.")
    }

    private fun UserDto.toDomain() = AuthUser(id, email, displayName)
}
