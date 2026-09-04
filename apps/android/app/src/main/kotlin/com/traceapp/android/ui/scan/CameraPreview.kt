package com.traceapp.android.ui.scan

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.traceapp.core.contracts.ImageInput
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCaptureReady: (ImageCapture) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
    }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = providerFuture.get()
            imageCapture.targetRotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
                onCaptureReady(imageCapture)
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            if (providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
        }
    }

    AndroidView(
        factory = { previewView },
        update = { view ->
            view.post {
                view.display?.rotation?.let { imageCapture.targetRotation = it }
            }
        },
        modifier = modifier,
    )
}

fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onSuccess: (ImageInput) -> Unit,
    onError: (String) -> Unit,
) {
    val output = File.createTempFile("trace_capture_", ".jpg", context.cacheDir)
    val options = ImageCapture.OutputFileOptions.Builder(output).build()
    imageCapture.takePicture(
        options,
        cameraIoExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runCatching {
                    val bytes = output.readBytes()
                    val orientation = ExifInterface(output.absolutePath).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL,
                    )
                    val rotationDegrees = rotationDegreesForExif(orientation)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    val swapDimensions = rotationDegrees == 90 || rotationDegrees == 270
                    ImageInput(
                        jpegBytes = bytes,
                        width = if (swapDimensions) bounds.outHeight else bounds.outWidth,
                        height = if (swapDimensions) bounds.outWidth else bounds.outHeight,
                        rotationDegrees = rotationDegrees,
                        capturedAtEpochMillis = System.currentTimeMillis(),
                    )
                }.onSuccess { image ->
                    ContextCompat.getMainExecutor(context).execute { onSuccess(image) }
                }.onFailure { failure ->
                    ContextCompat.getMainExecutor(context).execute {
                        onError(failure.message ?: "Không đọc được ảnh")
                    }
                }
                output.delete()
            }

            override fun onError(exception: ImageCaptureException) {
                output.delete()
                ContextCompat.getMainExecutor(context).execute {
                    onError(exception.message ?: "Không chụp được ảnh")
                }
            }
        },
    )
}

internal fun rotationDegreesForExif(orientation: Int): Int = when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90,
    ExifInterface.ORIENTATION_TRANSPOSE,
    -> 90
    ExifInterface.ORIENTATION_ROTATE_180,
    ExifInterface.ORIENTATION_FLIP_VERTICAL,
    -> 180
    ExifInterface.ORIENTATION_ROTATE_270,
    ExifInterface.ORIENTATION_TRANSVERSE,
    -> 270
    else -> 0
}

private val cameraIoExecutor = Executors.newSingleThreadExecutor { runnable ->
    Thread(runnable, "trace-camera-io")
}
