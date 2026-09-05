package com.traceapp.android.ui.pattern

import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Test

class UsagePatternTest {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /** 2026-09-07 is a Monday. All fixtures are expressed in UTC for determinism. */
    private fun epoch(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance(utc, Locale.US).apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun series(axis: UsageAxis, periodStart: Long, times: List<Long>, now: Long): UsageSeries =
        UsagePattern.series(axis, periodStart, times, now, utc)

    @Test
    fun `hour axis buckets by local hour of a single day`() {
        val monday = epoch(2026, 9, 7)
        val now = epoch(2026, 9, 7, 9, 0)
        val result = series(
            UsageAxis.HOUR_OF_DAY,
            UsagePattern.currentPeriodStart(UsageAxis.HOUR_OF_DAY, monday, utc),
            listOf(
                epoch(2026, 9, 7, 9, 0),
                epoch(2026, 9, 7, 9, 45),
                epoch(2026, 9, 7, 14, 10),
            ),
            now,
        )
        assertThat(result.axis.barCount).isEqualTo(24)
        assertThat(result.counts[9]).isEqualTo(2)
        assertThat(result.counts[14]).isEqualTo(1)
        assertThat(result.total).isEqualTo(3)
        assertThat(result.highlightIndex).isEqualTo(9)
    }

    @Test
    fun `hour axis ignores out of window timestamps`() {
        val monday = epoch(2026, 9, 7)
        val result = series(
            UsageAxis.HOUR_OF_DAY,
            UsagePattern.currentPeriodStart(UsageAxis.HOUR_OF_DAY, monday, utc),
            listOf(
                epoch(2026, 9, 8, 9, 0), // next day: out of window
                epoch(2026, 9, 7, 20, 0),
            ),
            monday,
        )
        assertThat(result.counts[20]).isEqualTo(1)
        assertThat(result.total).isEqualTo(1)
    }

    @Test
    fun `weekday axis maps Monday to index zero and Sunday to six`() {
        assertThat(UsagePattern.weekdayIndex(epoch(2026, 9, 7), utc)).isEqualTo(0) // Monday
        assertThat(UsagePattern.weekdayIndex(epoch(2026, 9, 13), utc)).isEqualTo(6) // Sunday
    }

    @Test
    fun `weekday axis buckets across a single week`() {
        val weekStart = UsagePattern.currentPeriodStart(UsageAxis.WEEKDAY, epoch(2026, 9, 7), utc)
        val now = epoch(2026, 9, 9, 12, 0) // Wednesday
        val result = series(
            UsageAxis.WEEKDAY,
            weekStart,
            listOf(
                epoch(2026, 9, 7, 8, 0), // Mon
                epoch(2026, 9, 8, 8, 0), // Tue
                epoch(2026, 9, 10, 8, 0), // Thu
                epoch(2026, 9, 20, 8, 0), // following week, out of window
            ),
            now,
        )
        assertThat(result.axis.barCount).isEqualTo(7)
        assertThat(result.counts[0]).isEqualTo(1)
        assertThat(result.counts[1]).isEqualTo(1)
        assertThat(result.counts[3]).isEqualTo(1)
        assertThat(result.total).isEqualTo(3)
        assertThat(result.highlightIndex).isEqualTo(2) // Wednesday
    }

    @Test
    fun `no highlight when window does not contain now`() {
        val pastWeekStart = UsagePattern.stepPeriod(UsageAxis.WEEKDAY, epoch(2026, 9, 7), -2, utc)
        val now = epoch(2026, 9, 9, 12, 0)
        val result = series(UsageAxis.WEEKDAY, pastWeekStart, emptyList(), now)
        assertThat(result.highlightIndex).isNull()
    }

    @Test
    fun `stepPeriod moves one day for hours and one week for weekdays`() {
        val monday = epoch(2026, 9, 7)
        assertThat(UsagePattern.stepPeriod(UsageAxis.HOUR_OF_DAY, monday, 1, utc))
            .isEqualTo(epoch(2026, 9, 8))
        assertThat(UsagePattern.stepPeriod(UsageAxis.HOUR_OF_DAY, monday, -1, utc))
            .isEqualTo(epoch(2026, 9, 6))
        assertThat(UsagePattern.stepPeriod(UsageAxis.WEEKDAY, monday, 1, utc))
            .isEqualTo(epoch(2026, 9, 14))
        assertThat(UsagePattern.stepPeriod(UsageAxis.WEEKDAY, monday, -1, utc))
            .isEqualTo(epoch(2026, 8, 31))
    }

    @Test
    fun `can step forward only while in the past relative to the current period`() {
        val monday = epoch(2026, 9, 7)
        val now = epoch(2026, 9, 9, 12, 0) // same week as monday
        // Current week start: cannot advance further.
        assertThat(UsagePattern.canStepForward(UsageAxis.WEEKDAY, monday, now, utc)).isFalse()
        val previousWeek = epoch(2026, 8, 31)
        assertThat(UsagePattern.canStepForward(UsageAxis.WEEKDAY, previousWeek, now, utc)).isTrue()
    }

    @Test
    fun `period label reflects current window and absolute dates`() {
        val now = epoch(2026, 9, 9, 12, 0)
        val today = UsagePattern.currentPeriodStart(UsageAxis.HOUR_OF_DAY, now, utc)
        assertThat(UsagePattern.periodLabel(UsageAxis.HOUR_OF_DAY, UsagePattern.periodFor(UsageAxis.HOUR_OF_DAY, today, utc), now, utc)).isEqualTo("Hôm nay")
        val yesterday = UsagePattern.stepPeriod(UsageAxis.HOUR_OF_DAY, today, -1, utc)
        assertThat(UsagePattern.periodLabel(UsageAxis.HOUR_OF_DAY, UsagePattern.periodFor(UsageAxis.HOUR_OF_DAY, yesterday, utc), now, utc)).isEqualTo("8/9")
    }
}
