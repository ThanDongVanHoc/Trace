package com.traceapp.android.ui.pattern

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The axis along which detection ("usage") timestamps are bucketed. */
enum class UsageAxis(val barCount: Int) {
    /** A single local calendar day, 24 hourly bars. */
    HOUR_OF_DAY(24),

    /** A single local calendar week (Mon–Sun), 7 daily bars. */
    WEEKDAY(7),
}

/** A half-open local period [startEpochMillis, endEpochMillis). */
data class UsagePeriod(
    val startEpochMillis: Long,
    val endEpochMillis: Long,
)

/** Bucketed bar counts plus the derived highlight for one window/axis. */
data class UsageSeries(
    val axis: UsageAxis,
    val counts: List<Int>,
    val highlightIndex: Int?,
) {
    val total: Int get() = counts.sum()
    val max: Int get() = counts.maxOrNull() ?: 0
}

private val WEEKDAY_NAMES = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

object UsagePattern {

    /** Day-of-week key so Monday = 0 ... Sunday = 6 (Calendar.DAY_OF_WEEK is 1..7, Monday = 2). */
    fun weekdayIndex(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): Int {
        val c = calendar(zone)
        c.timeInMillis = epochMillis
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        return (dayOfWeek + 5) % 7
    }

    fun hourIndex(epochMillis: Long, zone: TimeZone = TimeZone.getDefault()): Int {
        val c = calendar(zone)
        c.timeInMillis = epochMillis
        return c.get(Calendar.HOUR_OF_DAY)
    }

    /** Start (local midnight) of the period that currently contains [nowMillis]. */
    fun currentPeriodStart(
        axis: UsageAxis,
        nowMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Long = when (axis) {
        UsageAxis.HOUR_OF_DAY -> startOfDay(nowMillis, zone)
        UsageAxis.WEEKDAY -> startOfWeek(nowMillis, zone)
    }

    /** Period start moved by [steps] units: ±1 day for hours, ±1 week for weekdays. */
    fun stepPeriod(
        axis: UsageAxis,
        fromStartMillis: Long,
        steps: Int,
        zone: TimeZone = TimeZone.getDefault(),
    ): Long {
        val c = calendar(zone)
        c.timeInMillis = fromStartMillis
        when (axis) {
            UsageAxis.HOUR_OF_DAY -> c.add(Calendar.DATE, steps)
            UsageAxis.WEEKDAY -> c.add(Calendar.DATE, steps * 7)
        }
        return when (axis) {
            UsageAxis.HOUR_OF_DAY -> startOfDay(c.timeInMillis, zone)
            UsageAxis.WEEKDAY -> startOfWeek(c.timeInMillis, zone)
        }
    }

    fun periodFor(
        axis: UsageAxis,
        periodStartMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): UsagePeriod {
        val end = stepPeriod(axis, periodStartMillis, 1, zone)
        return UsagePeriod(periodStartMillis, end)
    }

    /** Builds a full [UsageSeries], only counting timestamps that fall inside the window. */
    fun series(
        axis: UsageAxis,
        periodStartMillis: Long,
        epochMillis: List<Long>,
        nowMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): UsageSeries {
        val period = periodFor(axis, periodStartMillis, zone)
        val counts = IntArray(axis.barCount)
        for (epoch in epochMillis) {
            if (epoch < period.startEpochMillis || epoch >= period.endEpochMillis) continue
            val index = when (axis) {
                UsageAxis.HOUR_OF_DAY -> hourIndex(epoch, zone)
                UsageAxis.WEEKDAY -> weekdayIndex(epoch, zone)
            }
            if (index in counts.indices) counts[index]++
        }
        return UsageSeries(
            axis = axis,
            counts = counts.toList(),
            highlightIndex = highlightIndex(axis, nowMillis, period, zone),
        )
    }

    /** Highlight current hour/day bar only when [nowMillis] falls inside the window. */
    fun highlightIndex(
        axis: UsageAxis,
        nowMillis: Long,
        period: UsagePeriod,
        zone: TimeZone = TimeZone.getDefault(),
    ): Int? {
        if (nowMillis < period.startEpochMillis || nowMillis >= period.endEpochMillis) return null
        return when (axis) {
            UsageAxis.HOUR_OF_DAY -> hourIndex(nowMillis, zone)
            UsageAxis.WEEKDAY -> weekdayIndex(nowMillis, zone)
        }
    }

    fun barLabel(axis: UsageAxis, index: Int): String = when (axis) {
        UsageAxis.HOUR_OF_DAY -> index.toString()
        UsageAxis.WEEKDAY -> WEEKDAY_NAMES.getOrElse(index) { "" }
    }

    fun periodLabel(
        axis: UsageAxis,
        period: UsagePeriod,
        nowMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): String {
        val nowInWindow = nowMillis >= period.startEpochMillis && nowMillis < period.endEpochMillis
        if (nowInWindow && axis == UsageAxis.HOUR_OF_DAY) return "Hôm nay"
        if (nowInWindow && axis == UsageAxis.WEEKDAY) return "Tuần này"
        return when (axis) {
            UsageAxis.HOUR_OF_DAY -> dayFormatter(zone).format(Date(period.startEpochMillis))
            UsageAxis.WEEKDAY -> {
                val start = dayFormatter(zone).format(Date(period.startEpochMillis))
                val end = dayFormatter(zone).format(Date(period.endEpochMillis - 1))
                "$start – $end"
            }
        }
    }

    /** True when a "next" step is allowed (i.e. moving toward the present/current period). */
    fun canStepForward(
        axis: UsageAxis,
        periodStartMillis: Long,
        nowMillis: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Boolean = periodStartMillis < currentPeriodStart(axis, nowMillis, zone)

    private fun startOfDay(epochMillis: Long, zone: TimeZone): Long {
        val c = calendar(zone)
        c.timeInMillis = epochMillis
        return dayStart(c)
    }

    private fun startOfWeek(epochMillis: Long, zone: TimeZone): Long {
        val c = calendar(zone)
        c.timeInMillis = epochMillis
        c.add(Calendar.DATE, -weekdayIndex(epochMillis, zone))
        return dayStart(c)
    }

    private fun dayStart(c: Calendar): Long {
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun calendar(zone: TimeZone): Calendar = Calendar.getInstance(zone, Locale.getDefault())

    private fun dayFormatter(zone: TimeZone): SimpleDateFormat =
        SimpleDateFormat("d/M", Locale.getDefault()).apply { timeZone = zone }
}
