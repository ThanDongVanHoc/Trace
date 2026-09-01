package com.trace.playground.recognition

import kotlin.test.Test
import kotlin.test.assertEquals

class RecognitionAlgorithmTest {
    @Test
    fun `cosine returns one for identical vectors`() {
        assertEquals(1f, RecognitionAlgorithm().cosine(listOf(1f, 2f), listOf(1f, 2f)), 0.0001f)
    }
}
