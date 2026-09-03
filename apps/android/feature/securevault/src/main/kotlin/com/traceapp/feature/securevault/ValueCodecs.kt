package com.traceapp.feature.securevault

import com.traceapp.core.contracts.GeoFix
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FloatVectorCodec {
    fun encode(values: FloatArray): ByteArray = ByteBuffer.allocate(values.size * Float.SIZE_BYTES)
        .order(ByteOrder.BIG_ENDIAN)
        .apply { values.forEach(::putFloat) }
        .array()

    fun decode(bytes: ByteArray, expectedDimensions: Int): FloatArray {
        require(expectedDimensions > 0 && bytes.size == expectedDimensions * Float.SIZE_BYTES)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        return FloatArray(expectedDimensions) { buffer.float }
    }
}

object GeoFixCodec {
    private const val SIZE_BYTES = Double.SIZE_BYTES * 2 + Float.SIZE_BYTES + Long.SIZE_BYTES

    fun encode(value: GeoFix): ByteArray = ByteBuffer.allocate(SIZE_BYTES)
        .order(ByteOrder.BIG_ENDIAN)
        .putDouble(value.latitude)
        .putDouble(value.longitude)
        .putFloat(value.accuracyMeters)
        .putLong(value.capturedAtEpochMillis)
        .array()

    fun decode(bytes: ByteArray): GeoFix {
        require(bytes.size == SIZE_BYTES)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        return GeoFix(
            latitude = buffer.double,
            longitude = buffer.double,
            accuracyMeters = buffer.float,
            capturedAtEpochMillis = buffer.long,
        )
    }
}
