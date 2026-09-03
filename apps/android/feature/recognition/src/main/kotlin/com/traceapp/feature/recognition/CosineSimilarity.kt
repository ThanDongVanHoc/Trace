package com.traceapp.feature.recognition

import kotlin.math.sqrt

object CosineSimilarity {
    fun score(left: FloatArray, right: FloatArray): Float {
        if (left.size != right.size || left.isEmpty()) return 0f
        var dot = 0.0
        var leftNorm = 0.0
        var rightNorm = 0.0
        left.indices.forEach { index ->
            dot += left[index] * right[index]
            leftNorm += left[index] * left[index]
            rightNorm += right[index] * right[index]
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0f
        return (dot / sqrt(leftNorm * rightNorm)).toFloat().coerceIn(-1f, 1f)
    }
}
