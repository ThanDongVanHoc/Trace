package com.trace.playground.contracts

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoiTest {
    @Test
    fun `accepts normalized rectangle`() {
        assertTrue(Roi(0.1f, 0.2f, 0.8f, 0.9f).isValid)
    }

    @Test
    fun `rejects inverted rectangle`() {
        assertFalse(Roi(0.8f, 0.2f, 0.1f, 0.9f).isValid)
    }
}
