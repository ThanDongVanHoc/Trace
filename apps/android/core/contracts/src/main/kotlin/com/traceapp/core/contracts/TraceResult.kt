package com.traceapp.core.contracts

sealed interface TraceResult<out T> {
    data class Success<T>(val value: T) : TraceResult<T>
    data class Failure(val error: TraceError) : TraceResult<Nothing>
}

data class TraceError(
    val code: TraceErrorCode,
    val message: String,
    val cause: Throwable? = null,
)

enum class TraceErrorCode {
    INVALID_INPUT,
    IMAGE_TOO_DARK,
    IMAGE_TOO_BLURRY,
    ROI_TOO_SMALL,
    MODEL_MISMATCH,
    NOT_FOUND,
    STORAGE_FAILURE,
    CRYPTO_FAILURE,
    NETWORK_FAILURE,
    UNAUTHORIZED,
    INTERNAL_FAILURE,
}

inline fun <T, R> TraceResult<T>.map(transform: (T) -> R): TraceResult<R> = when (this) {
    is TraceResult.Success -> TraceResult.Success(transform(value))
    is TraceResult.Failure -> this
}

inline fun <T> TraceResult<T>.getOrElse(defaultValue: (TraceError) -> T): T = when (this) {
    is TraceResult.Success -> value
    is TraceResult.Failure -> defaultValue(error)
}
