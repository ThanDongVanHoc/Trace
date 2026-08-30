package com.traceapp.android.ui.find

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.traceapp.android.ui.home.ObjectMemoryUiState
import com.traceapp.core.contracts.Sighting
import java.text.DateFormat
import java.util.Date

@Composable
fun FindScreen(
    state: ObjectMemoryUiState,
    onFind: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, state.references) {
        state.references.filter { it.tag.contains(query.trim(), ignoreCase = true) }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Tìm đồ vật", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Chọn tag cần tìm — không cần chụp thêm ảnh.",
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
        } else {
            items(filtered, key = { it.objectId }) { reference ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFind(reference.objectId) },
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
    }
}

@Composable
private fun LastSeenCard(tag: String, sighting: Sighting?) {
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
                    Text("Vị trí: %.5f, %.5f · sai số %.0f m".format(it.latitude, it.longitude, it.accuracyMeters))
                } ?: Text("Không có quyền/vị trí GPS ở lần ghi nhận này.")
                Text("Độ tin cậy: ${(sighting.confidence * 100).toInt()}%")
            }
        }
    }
}
