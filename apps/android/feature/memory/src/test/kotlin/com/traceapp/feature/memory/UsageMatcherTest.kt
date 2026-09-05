package com.traceapp.feature.memory

import com.google.common.truth.Truth.assertThat
import com.traceapp.core.contracts.SightingTime
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Test

class UsageMatcherTest {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /** 2026-09-07 is a Monday (UTC), matching the prototype fixtures. */
    private val baseMondayEpochMillis: Long = Calendar.getInstance(utc, Locale.US).apply {
        set(2026, Calendar.SEPTEMBER, 7, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun moment(daysFromMonday: Int, hour: Int, minute: Int): Long =
        baseMondayEpochMillis + daysFromMonday * 86_400_000L + (hour * 60L + minute) * 60_000L

    private fun top(now: Long, vararg rows: SightingTime) =
        UsageMatcher.topMatch(now, rows.toList(), emptyMap())

    @Test
    fun `ranks by weekday when clock time is equal`() {
        val now = moment(0, 9, 0)
        val result = top(
            now,
            SightingTime("b", moment(1, 9, 0)), // Tuesday
            SightingTime("a", moment(0, 9, 0)), // Monday, exact
            SightingTime("c", moment(3, 9, 0)), // Thursday
        )
        assertThat(result?.objectId).isEqualTo("a")
    }

    @Test
    fun `ranks by time of day when weekday is equal`() {
        val now = moment(0, 9, 0)
        val result = top(
            now,
            SightingTime("a", moment(0, 10, 0)),
            SightingTime("b", moment(0, 9, 0)),
        )
        assertThat(result?.objectId).isEqualTo("b")
    }

    @Test
    fun `wraps across the week boundary`() {
        val now = moment(0, 9, 0) // Monday
        val result = top(
            now,
            SightingTime("wednesday", moment(2, 9, 0)), // two days ahead
            SightingTime("sunday", moment(6, 9, 0)), // one day behind (wrap)
        )
        assertThat(result?.objectId).isEqualTo("sunday")
    }

    @Test
    fun `wraps across midnight`() {
        val now = moment(0, 0, 5) // Monday 00:05
        val result = top(
            now,
            SightingTime("late", moment(0, 23, 30)), // ~35 min away across midnight
            SightingTime("afterMidnight", moment(0, 0, 30)), // ~25 min ahead
        )
        assertThat(result?.objectId).isEqualTo("afterMidnight")
    }

    @Test
    fun `returns null when there is no history`() {
        assertThat(top(moment(0, 9, 0))).isNull()
    }

    @Test
    fun `keeps only the best row per object`() {
        val now = moment(0, 9, 0)
        val result = top(
            now,
            SightingTime("x", moment(0, 23, 0)), // far
            SightingTime("x", moment(0, 9, 0)), // exact
            SightingTime("x", moment(4, 14, 0)), // distant weekday
        )
        assertThat(result?.objectId).isEqualTo("x")
        assertThat(result?.matchedEpochMillis).isEqualTo(moment(0, 9, 0))
    }

    @Test
    fun `attaches the display tag from the map`() {
        val now = moment(0, 9, 0)
        val match = UsageMatcher.topMatch(
            now,
            listOf(SightingTime("x", now)),
            mapOf("x" to "Ba lô"),
        )
        assertThat(match?.tag).isEqualTo("Ba lô")
    }
}
