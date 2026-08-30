package com.traceapp.core.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ApiConfig(val baseUrl: String, val debug: Boolean)

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindings {
    @Binds
    @Singleton
    abstract fun bindTokenStore(implementation: KeystoreTokenStore): TokenStore

    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: RemoteAuthRepository): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @Named("public")
    fun providePublicClient(config: ApiConfig): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (config.debug) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader("Authorization")
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    @Named("public")
    fun providePublicRetrofit(
        config: ApiConfig,
        @Named("public") client: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideRefreshApi(@Named("public") retrofit: Retrofit): RefreshApi =
        retrofit.create(RefreshApi::class.java)

    @Provides
    @Singleton
    fun provideAuthenticator(tokenStore: TokenStore, refreshApi: RefreshApi): Authenticator =
        RefreshTokenAuthenticator(tokenStore, refreshApi)

    @Provides
    @Singleton
    @Named("authenticated")
    fun provideAuthenticatedClient(
        config: ApiConfig,
        tokenStore: TokenStore,
        authenticator: Authenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AccessTokenInterceptor(tokenStore))
        .authenticator(authenticator)
        .apply {
            if (config.debug) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader("Authorization")
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideBackendApi(
        config: ApiConfig,
        @Named("authenticated") client: OkHttpClient,
    ): TraceBackendApi = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TraceBackendApi::class.java)
}

private class AccessTokenInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.read()?.accessToken
        val request = if (token == null) {
            chain.request()
        } else {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(request)
    }
}

private class RefreshTokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi,
) : Authenticator {
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount >= 2 || response.request.url.encodedPath.contains("/auth/")) {
            return null
        }
        synchronized(lock) {
            val session = tokenStore.read() ?: return null
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (sentToken != session.accessToken) {
                return response.request.withBearer(session.accessToken)
            }
            val refreshed = runCatching {
                refreshApi.refresh(RefreshRequestDto(session.refreshToken)).execute()
            }.getOrNull()
            val body = refreshed?.takeIf { it.isSuccessful }?.body()
            if (body == null) {
                tokenStore.clear()
                return null
            }
            tokenStore.write(
                SessionTokens(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresAtEpochMillis = System.currentTimeMillis() + body.expiresInSeconds * 1_000,
                ),
            )
            return response.request.withBearer(body.accessToken)
        }
    }

    private fun Request.withBearer(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}
