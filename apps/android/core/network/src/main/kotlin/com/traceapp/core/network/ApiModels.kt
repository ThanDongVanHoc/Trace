package com.traceapp.core.network

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
)

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
)

data class LoginRequestDto(
    val email: String,
    val password: String,
)

data class RefreshRequestDto(val refreshToken: String)

data class AuthResponseDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
)

data class ObjectDto(
    val id: String,
    val tag: String,
    val referenceRevision: Int,
)

data class CreateObjectRequestDto(
    val id: String,
    val tag: String,
    val referenceRevision: Int,
)

data class SightingDto(
    val id: String,
    val objectId: String,
    val detectedAt: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val confidence: Float,
    val evidenceAssetId: String?,
)

data class BatchSightingsRequestDto(val items: List<SightingDto>)

data class DeviceRequestDto(
    val platform: String = "android",
    val pushToken: String?,
    val locale: String,
    val notificationsEnabled: Boolean,
)
