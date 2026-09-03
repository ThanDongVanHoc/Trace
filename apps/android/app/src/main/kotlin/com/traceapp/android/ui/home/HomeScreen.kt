package com.traceapp.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.traceapp.core.contracts.ObjectReference

@Composable
fun HomeScreen(
    state: ObjectMemoryUiState,
    displayName: String,
    onScan: () -> Unit,
    onFind: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Chào ${displayName.substringBefore(' ')} 👋",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                "Bạn muốn ghi nhớ hay tìm lại đồ vật?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(14.dp).size(28.dp),
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Ghi nhớ đồ vật mới", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Chụp, khoanh vùng và đặt tên",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Mở camera")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    icon = Icons.Outlined.Backpack,
                    title = "Đã ghi nhớ",
                    value = state.references.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    icon = Icons.Outlined.Lock,
                    title = "Lưu riêng tư",
                    value = "AES-GCM",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Đồ vật gần đây",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onFind) {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                    Text("Tìm đồ")
                }
            }
        }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.references.isEmpty() -> item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Backpack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                        Text("Chưa có đồ vật nào", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Bắt đầu bằng một món đồ bạn thường quên vị trí.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(onClick = onScan) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ghi nhớ món đầu tiên")
                        }
                    }
                }
            }
            else -> items(state.references, key = ObjectReference::objectId) { reference ->
                ObjectCard(reference = reference, onClick = onFind)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ObjectCard(reference: ObjectReference, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Outlined.Backpack,
                    contentDescription = null,
                    modifier = Modifier.padding(11.dp).size(24.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(reference.tag, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${reference.embeddings.size} mẫu · chất lượng ${(reference.qualityScore * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = "Tìm vị trí ${reference.tag}",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
