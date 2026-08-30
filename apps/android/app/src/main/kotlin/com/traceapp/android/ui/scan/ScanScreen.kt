package com.traceapp.android.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ScanScreen(
    onDataChanged: () -> Unit,
    onOpenFind: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(state.dataRevision) {
        if (state.dataRevision > 0) onDataChanged()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Scan", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeButton(
                selected = state.mode == ScanMode.TAG,
                label = "Gắn tag",
                onClick = { viewModel.setMode(ScanMode.TAG) },
                modifier = Modifier.weight(1f),
            )
            ModeButton(
                selected = state.mode == ScanMode.RECOGNIZE,
                label = "Nhận diện",
                onClick = { viewModel.setMode(ScanMode.RECOGNIZE) },
                modifier = Modifier.weight(1f),
            )
        }

        if (!hasCameraPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TRACE cần quyền camera để chụp và nhận diện đồ vật.")
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Cấp quyền camera")
                    }
                }
            }
        } else if (state.image == null) {
            LiveCamera(
                mode = state.mode,
                onImage = viewModel::setImage,
                modifier = Modifier.weight(1f),
            )
        } else {
            CapturedImage(
                state = state,
                onRoiChange = viewModel::setRoi,
                modifier = Modifier.weight(1f),
            )
            if (state.mode == ScanMode.TAG) {
                OutlinedTextField(
                    value = state.tag,
                    onValueChange = viewModel::setTag,
                    label = { Text("Tên đồ vật") },
                    placeholder = { Text("Ví dụ: Balô của tôi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            state.message?.let { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                        Text(message, modifier = Modifier.weight(1f))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = viewModel::resetCapture,
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Text("Chụp lại")
                }
                Button(
                    onClick = if (state.mode == ScanMode.TAG) viewModel::enroll else viewModel::recognize,
                    enabled = !state.busy && (state.mode == ScanMode.RECOGNIZE || state.tag.isNotBlank()),
                    modifier = Modifier.weight(1f),
                ) {
                    if (state.busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(if (state.mode == ScanMode.TAG) "Lưu tag" else "Nhận diện")
                }
            }
            if (state.canOpenFind) {
                FilledTonalButton(onClick = onOpenFind, modifier = Modifier.fillMaxWidth()) {
                    Text("Xem vị trí vừa ghi nhận")
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(label) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
}

@Composable
private fun LiveCamera(
    mode: ScanMode,
    onImage: (com.traceapp.core.contracts.ImageInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    Box(modifier = modifier.fillMaxWidth()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCaptureReady = { imageCapture = it },
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (mode == ScanMode.TAG) "Đặt đồ vật trong khung rồi chụp" else "Chụp nơi đồ vật có thể xuất hiện",
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        captureImage(context, capture, onImage, onError = {})
                    }
                },
                enabled = imageCapture != null,
                modifier = Modifier.size(72.dp),
            ) { Icon(Icons.Outlined.CameraAlt, contentDescription = "Chụp ảnh") }
        }
    }
}

@Composable
private fun CapturedImage(
    state: ScanUiState,
    onRoiChange: (com.traceapp.core.contracts.NormalizedRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val image = requireNotNull(state.image)
    val bitmap = remember(image.jpegBytes) {
        BitmapFactory.decodeByteArray(image.jpegBytes, 0, image.jpegBytes.size)?.asImageBitmap()
    }
    val ratio = if (image.height > 0) image.width.toFloat() / image.height else 1f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio.coerceIn(0.55f, 1.8f)),
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Ảnh vừa chụp",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (state.mode == ScanMode.TAG) {
            RoiOverlay(
                rect = state.roi,
                onRectChange = onRoiChange,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
