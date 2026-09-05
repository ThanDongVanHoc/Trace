package com.traceapp.android.ui.pattern

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UsageChartCard(
    title: String,
    subtitle: String,
    axis: UsageAxis,
    periodStartMillis: Long,
    series: UsageSeries?,
    loading: Boolean,
    canStepForward: Boolean,
    onAxisChange: (UsageAxis) -> Unit,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodLabel = run {
        val now = System.currentTimeMillis()
        val period = UsagePattern.periodFor(axis, periodStartMillis)
        UsagePattern.periodLabel(axis, period, now)
    }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onStep(-1) }, enabled = !loading) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Về trước")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp)) {
                    Text(periodLabel, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
                IconButton(onClick = { onStep(1) }, enabled = !loading && canStepForward) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Về sau")
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                SingleChoiceSegmentedButtonRow {
                    UsageAxis.entries.forEachIndexed { index, entry ->
                        SegmentedButton(
                            selected = axis == entry,
                            onClick = { onAxisChange(entry) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = UsageAxis.entries.size),
                        ) {
                            Text(axisTitle(entry))
                        }
                    }
                }
            }
                UsageChartBody(series = series, loading = loading)
        }
    }
}

@Composable
private fun UsageChartBody(series: UsageSeries?, loading: Boolean) {
    if (loading) {
        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
        return
    }
    val data = series
    if (data == null || data.total == 0) {
        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
            Text(
                "Chưa có dữ liệu trong khoảng thời gian này.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        return
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(80.dp)) {
            data.counts.forEachIndexed { index, count ->
                UsageBar(
                    index = index,
                    count = count,
                    max = data.max,
                    highlighted = data.highlightIndex == index,
                    axis = data.axis,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            data.axis.let { axis ->
                repeat(axis.barCount) { index ->
                    val label = labelAt(axis, index)
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageBar(
    index: Int,
    count: Int,
    max: Int,
    highlighted: Boolean,
    axis: UsageAxis,
    modifier: Modifier = Modifier,
) {
    val fraction = if (max > 0) count.toFloat() / max else 0f
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.5.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(barColor(highlighted)),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun barColor(highlighted: Boolean) =
    if (highlighted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

private fun axisTitle(axis: UsageAxis): String = when (axis) {
    UsageAxis.HOUR_OF_DAY -> "Giờ"
    UsageAxis.WEEKDAY -> "Thứ"
}

/** Sparse hour labels (0,6,12,18,23) and every weekday name. */
private fun labelAt(axis: UsageAxis, index: Int): String = when (axis) {
    UsageAxis.HOUR_OF_DAY -> if (index in intArrayOf(0, 6, 12, 18, 23)) "$index" else ""
    UsageAxis.WEEKDAY -> UsagePattern.barLabel(axis, index)
}
