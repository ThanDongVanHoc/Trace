package com.traceapp.feature.recognition

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CosineSimilarityTest {
    @Test
    fun identicalVectors_scoreOne() {
        assertThat(
            CosineSimilarity.score(floatArrayOf(1f, 2f), floatArrayOf(1f, 2f)),
        ).isWithin(0.0001f).of(1f)
    }

    @Test
    fun orthogonalVectors_scoreZero() {
        assertThat(
            CosineSimilarity.score(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)),
        ).isWithin(0.0001f).of(0f)
    }
}
