package com.traceapp.android.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.traceapp.core.contracts.NormalizedRect

@Composable
fun RoiOverlay(
    rect: NormalizedRect,
    onRectChange: (NormalizedRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentRect by rememberUpdatedState(rect)
    val currentOnRectChange by rememberUpdatedState(onRectChange)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val handleHitRadius = with(density) { HANDLE_TOUCH_TARGET.toPx() }
    val handleRadius = with(density) { HANDLE_RADIUS.toPx() }
    val outlineWidth = with(density) { OUTLINE_WIDTH.toPx() }

    Canvas(
        modifier = modifier
            .semantics { contentDescription = "Vùng chọn đồ vật" }
            // The gesture lifetime must not depend on rect. Re-keying with rect cancels an
            // in-progress drag every time the parent accepts a coordinate update.
            .pointerInput(Unit) {
            var session: RoiDragSession? = null
            detectDragGestures(
                onDragStart = { point ->
                    session = RoiTransform.begin(
                        rect = currentRect,
                        point = point,
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                        handleHitRadiusPx = handleHitRadius,
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDrag = { change, _ ->
                    change.consume()
                    session?.let {
                        currentOnRectChange(
                            RoiTransform.update(
                                session = it,
                                point = change.position,
                                canvasSize = Size(size.width.toFloat(), size.height.toFloat()),
                                minimumEdge = MINIMUM_EDGE,
                            ),
                        )
                    }
                },
                onDragEnd = { session = null },
                onDragCancel = { session = null },
            )
        },
    ) {
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height
        val shade = Color.Black.copy(alpha = 0.56f)
        drawRect(shade, size = Size(size.width, top))
        drawRect(shade, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))
        drawRect(shade, topLeft = Offset(0f, top), size = Size(left, bottom - top))
        drawRect(shade, topLeft = Offset(right, top), size = Size(size.width - right, bottom - top))

        drawRoundRect(
            color = ACCENT,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = outlineWidth),
        )
        listOf(
            Offset(left, top),
            Offset(right, top),
            Offset(left, bottom),
            Offset(right, bottom),
        ).forEach { center ->
            drawCircle(Color.Black.copy(alpha = 0.18f), radius = handleRadius + 3f, center = center)
            drawCircle(Color.White, radius = handleRadius, center = center)
            drawCircle(ACCENT, radius = handleRadius * 0.58f, center = center)
        }
    }
}

private const val MINIMUM_EDGE = 0.08f
private val HANDLE_TOUCH_TARGET = 28.dp
private val HANDLE_RADIUS = 10.dp
private val OUTLINE_WIDTH = 3.dp
private val ACCENT = Color(0xFF8B7CFF)
