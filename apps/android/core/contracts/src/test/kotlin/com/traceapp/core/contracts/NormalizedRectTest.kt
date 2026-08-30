package com.traceapp.core.contracts

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NormalizedRectTest {
    @Test
    fun validRect_isAccepted() {
        val rect = NormalizedRect(0.1f, 0.2f, 0.8f, 0.9f)

        assertThat(rect.isValid).isTrue()
        assertThat(rect.area).isWithin(0.0001f).of(0.49f)
    }

    @Test
    fun invertedOrOutOfRangeRect_isRejected() {
        assertThat(NormalizedRect(0.8f, 0f, 0.2f, 1f).isValid).isFalse()
        assertThat(NormalizedRect(-0.1f, 0f, 0.5f, 1f).isValid).isFalse()
    }
}
