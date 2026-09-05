package com.traceapp.android.ui.find

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.traceapp.android.ui.home.ObjectMemoryUiState
import com.traceapp.android.ui.pattern.UsageAxis
import com.traceapp.android.ui.pattern.UsageChartCard
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.Sighting
import java.text.DateFormat
import java.util.Date

@Composable
fun FindScreen(
    state: ObjectMemoryUiState,
    onFind: (String) -> Unit,
    onDelete: (String) -> Unit,
    onUsageAxisChange: (UsageAxis) -> Unit,
    onUsageStep: (Int) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<ObjectReference?>(null) }
    pendingDelete?.let { reference ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xoá đồ vật?") },
            text = { Text("\u201C${reference.tag}\u201D và toàn bộ lịch sử xuất hiện của nó sẽ bị xoá. Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(reference.objectId)
                        pendingDelete = null
                    },
                ) { Text("Xoá", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Huỷ") }
            },
        )
    }
    val filtered = remember(query, state.references) {
        state.references.filter { it.tag.contains(query.trim(), ignoreCase = true) }
    }
    val selectable = remember(filtered, state.selected?.objectId) {
        filtered.filterNot { it.objectId == state.selected?.objectId }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Tìm đồ vật", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Chọn tag cần tìm",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Ví dụ: balô, tai nghe…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        state.selected?.let { selected ->
            item { LastSeenCard(tag = selected.tag, sighting = selected.lastSeen) }
            item {
                UsageChartCard(
                    title = "Thói quen dùng \"${selected.tag}\"",
                    subtitle = "theo giờ hoặc ngày",
                    axis = state.usageAxis,
                    periodStartMillis = state.usagePeriodStartMillis,
                    series = state.itemUsage,
                    loading = state.usageLoading,
                    canStepForward = state.usageCanStepForward,
                    onAxisChange = onUsageAxisChange,
                    onStep = onUsageStep,
                )
            }
        }
        if (state.loading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            }
        } else if (filtered.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        if (state.references.isEmpty()) "Hãy gắn tag một đồ vật trước." else "Không tìm thấy tag phù hợp.",
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        } else if (selectable.isNotEmpty()) {
            items(selectable, key = { it.objectId }) { reference ->
                SwipeableFindCard(
                    reference = reference,
                    onClick = { onFind(reference.objectId) },
                    onDeleteRequest = { pendingDelete = reference },
                )
            }
        }
    }
}

@Composable
private fun SwipeableFindCard(
    reference: ObjectReference,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Xoá ${reference.tag}",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null)
                Column(Modifier.weight(1f)) {
                    Text(reference.tag, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Chạm để xem lần cuối xuất hiện",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
            }
        }
    }
}

@Composable
private fun LastSeenCard(tag: String, sighting: Sighting?) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (sighting == null) Icons.Outlined.LocationOff else Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(tag, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (sighting == null) {
                Text("Chưa ghi nhận lần xuất hiện nào.")
            } else {
                Text("Lần cuối: ${DateFormat.getDateTimeInstance().format(Date(sighting.detectedAtEpochMillis))}")
                sighting.location?.let {
                    Text("Đã lưu vị trí · độ chính xác khoảng %.0f m".format(it.accuracyMeters))
                    FilledTonalButton(
                        onClick = { openDirections(context, it.latitude, it.longitude, tag) },
                        modifier = Modifier.fillMaxWidth().testTag("open_directions"),
                    ) {
                        Icon(Icons.Outlined.Navigation, contentDescription = null)
                        Text("Mở chỉ đường")
                    }
                } ?: Text("Không có quyền/vị trí GPS ở lần ghi nhận này.")
                Text("Độ tin cậy: ${(sighting.confidence * 100).toInt()}%")
            }
        }
    }
}

private fun openDirections(context: Context, latitude: Double, longitude: Double, tag: String) {
    val coordinate = "$latitude,$longitude"
    val label = Uri.encode("($tag)")
    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$coordinate$label&z=17"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$coordinate"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(geoIntent)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(webIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Không tìm thấy ứng dụng bản đồ", Toast.LENGTH_SHORT).show()
        }
    }
}
