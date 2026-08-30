package com.traceapp.core.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TraceBackendApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()

    @GET("auth/me")
    suspend fun me(): UserDto

    @GET("objects")
    suspend fun objects(): List<ObjectDto>

    @POST("objects")
    suspend fun createObject(@Body body: CreateObjectRequestDto): ObjectDto

    @PATCH("objects/{id}")
    suspend fun updateObject(@Path("id") id: String, @Body body: Map<String, Any>): ObjectDto

    @DELETE("objects/{id}")
    suspend fun deleteObject(@Path("id") id: String)

    @POST("sightings/batch")
    suspend fun saveSightings(@Body body: BatchSightingsRequestDto): List<SightingDto>

    @GET("objects/{id}/last-seen")
    suspend fun lastSeen(@Path("id") objectId: String): SightingDto?

    @GET("objects/{id}/sightings")
    suspend fun timeline(
        @Path("id") objectId: String,
        @Query("limit") limit: Int = 50,
    ): List<SightingDto>

    @PUT("devices/{installationId}")
    suspend fun upsertDevice(
        @Path("installationId") installationId: String,
        @Body body: DeviceRequestDto,
    )

    @DELETE("devices/{installationId}")
    suspend fun deleteDevice(@Path("installationId") installationId: String)

    @POST("notifications/test")
    suspend fun sendTestNotification()
}

interface RefreshApi {
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequestDto): Call<AuthResponseDto>
}
