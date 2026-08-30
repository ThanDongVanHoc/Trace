package com.traceapp.android.ui.scan

import android.content.Context
import android.graphics.BitmapFactory
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

    AndroidView(factory = { previewView }, modifier = modifier)
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
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runCatching {
                    val bytes = output.readBytes()
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    ImageInput(
                        jpegBytes = bytes,
                        width = bounds.outWidth,
                        height = bounds.outHeight,
                        rotationDegrees = 0,
                        capturedAtEpochMillis = System.currentTimeMillis(),
                    )
                }.onSuccess(onSuccess).onFailure { onError(it.message ?: "Không đọc được ảnh") }
                output.delete()
            }

            override fun onError(exception: ImageCaptureException) {
                output.delete()
                onError(exception.message ?: "Không chụp được ảnh")
            }
        },
    )
}
