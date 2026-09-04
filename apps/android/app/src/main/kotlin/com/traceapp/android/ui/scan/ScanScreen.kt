package com.traceapp.android.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { hasCameraPermission = it }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        hasLocationPermission = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.recognize()
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(state.dataRevision) {
        if (state.dataRevision > 0) onDataChanged()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Ghi nhớ đồ vật", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Chụp một lần để TRACE giúp bạn tìm lại về sau.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ScanModeSelector(selected = state.mode, onSelected = viewModel::setMode)

        when {
            !hasCameraPermission -> CameraPermissionCard {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            state.image == null -> LiveCamera(
                mode = state.mode,
                onImage = viewModel::setImage,
                modifier = Modifier.weight(1f),
            )
            else -> CapturedContent(
                state = state,
                hasLocationPermission = hasLocationPermission,
                onRoiChange = viewModel::setRoi,
                onTagChange = viewModel::setTag,
                onReset = viewModel::resetCapture,
                onAction = {
                    if (state.mode == ScanMode.TAG) {
                        viewModel.enroll()
                    } else if (hasLocationPermission) {
                        viewModel.recognize()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                onOpenFind = onOpenFind,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ScanModeSelector(selected: ScanMode, onSelected: (ScanMode) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(ScanMode.TAG to "Gắn đồ vật", ScanMode.RECOGNIZE to "Tìm trong ảnh").forEach { (mode, label) ->
                val active = selected == mode
                Surface(
                    color = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                    contentColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = if (active) 2.dp else 0.dp,
                    modifier = Modifier.weight(1f).clickable { onSelected(mode) },
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionCard(onRequest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Cho phép TRACE sử dụng camera", style = MaterialTheme.typography.titleMedium)
            Text(
                "Camera chỉ dùng để ghi nhớ và nhận diện trên thiết bị; ảnh không được tải lên server.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRequest) { Text("Tiếp tục") }
        }
    }
}

@Composable
private fun CapturedContent(
    state: ScanUiState,
    hasLocationPermission: Boolean,
    onRoiChange: (com.traceapp.core.contracts.NormalizedRect) -> Unit,
    onTagChange: (String) -> Unit,
    onReset: () -> Unit,
    onAction: () -> Unit,
    onOpenFind: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CapturedImage(state = state, onRoiChange = onRoiChange)
        if (state.mode == ScanMode.TAG) {
            OutlinedTextField(
                value = state.tag,
                onValueChange = onTagChange,
                label = { Text("Đặt tên để dễ tìm") },
                placeholder = { Text("Ví dụ: Balô đen của tôi") },
                supportingText = { Text("Tên cụ thể sẽ giúp kết quả tìm kiếm rõ ràng hơn") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("object_tag"),
            )
        }
        state.message?.let { message ->
            Surface(
                color = if (state.isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                    Text(message, modifier = Modifier.weight(1f))
                }
            }
        }
        if (state.recognizedObjects.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().testTag("recognition_results")) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Đồ vật đã nhận diện",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    state.recognizedObjects.forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(result.tag, modifier = Modifier.weight(1f))
                            Text(
                                "${(result.similarity * 100).toInt()}%",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onReset,
                enabled = !state.busy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Text("Chụp lại")
            }
            Button(
                onClick = onAction,
                enabled = !state.busy &&
                    !state.enrollmentSaved &&
                    (state.mode == ScanMode.RECOGNIZE || state.tag.isNotBlank()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).height(52.dp).testTag("scan_action"),
            ) {
                if (state.busy) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        when {
                            state.enrollmentSaved -> "Đã lưu"
                            state.mode == ScanMode.TAG -> "Lưu đồ vật"
                            else -> "Nhận diện"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        if (state.mode == ScanMode.RECOGNIZE && !hasLocationPermission) {
            Text(
                "Vị trí là tùy chọn. Nếu bỏ qua quyền, TRACE vẫn nhận diện bình thường.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.canOpenFind) {
            FilledTonalButton(onClick = onOpenFind, modifier = Modifier.fillMaxWidth()) {
                Text("Xem vị trí vừa ghi nhận")
            }
        }
    }
}

@Composable
private fun LiveCamera(
    mode: ScanMode,
    onImage: (com.traceapp.core.contracts.ImageInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black),
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCaptureReady = { imageCapture = it },
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.64f),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    if (mode == ScanMode.TAG) {
                        "Đặt đồ vật rõ nét trong khung"
                    } else {
                        "Chụp nơi đồ vật có thể xuất hiện"
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        captureImage(context, capture, onImage, onError = {})
                    }
                },
                enabled = imageCapture != null,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(72.dp).testTag("capture_button"),
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
    val bitmap = remember(image.jpegBytes, image.rotationDegrees) {
        decodeCapturedBitmap(image.jpegBytes, image.rotationDegrees)?.asImageBitmap()
    }
    val ratio = if (image.height > 0) image.width.toFloat() / image.height else 1f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio.coerceIn(0.68f, 1.65f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black),
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
                modifier = Modifier.fillMaxSize().testTag("roi_overlay"),
            )
            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            ) {
                Text(
                    "Kéo góc để chỉnh • Kéo giữa để di chuyển",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun decodeCapturedBitmap(bytes: ByteArray, rotationDegrees: Int): Bitmap? {
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val degrees = ((rotationDegrees % 360) + 360) % 360
    if (degrees == 0) return decoded
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
}
