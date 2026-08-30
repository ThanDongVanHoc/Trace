package com.traceapp.android.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.traceapp.core.contracts.NormalizedRect
import kotlin.math.hypot

@Composable
fun RoiOverlay(
    rect: NormalizedRect,
    onRectChange: (NormalizedRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeCorner = 1
    Canvas(
        modifier = modifier.pointerInput(rect) {
            detectDragGestures(
                onDragStart = { point ->
                    val topLeft = Offset(rect.left * size.width, rect.top * size.height)
                    val bottomRight = Offset(rect.right * size.width, rect.bottom * size.height)
                    val firstDistance = hypot(point.x - topLeft.x, point.y - topLeft.y)
                    val secondDistance = hypot(point.x - bottomRight.x, point.y - bottomRight.y)
                    activeCorner = if (firstDistance < secondDistance) 0 else 1
                },
                onDrag = { change, _ ->
                    change.consume()
                    val x = (change.position.x / size.width).coerceIn(0f, 1f)
                    val y = (change.position.y / size.height).coerceIn(0f, 1f)
                    val next = if (activeCorner == 0) {
                        rect.copy(
                            left = x.coerceAtMost(rect.right - MINIMUM_EDGE),
                            top = y.coerceAtMost(rect.bottom - MINIMUM_EDGE),
                        )
                    } else {
                        rect.copy(
                            right = x.coerceAtLeast(rect.left + MINIMUM_EDGE),
                            bottom = y.coerceAtLeast(rect.top + MINIMUM_EDGE),
                        )
                    }
                    onRectChange(next)
                },
            )
        },
    ) {
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height
        val shade = Color.Black.copy(alpha = 0.5f)
        drawRect(shade, size = Size(size.width, top))
        drawRect(shade, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))
        drawRect(shade, topLeft = Offset(0f, top), size = Size(left, bottom - top))
        drawRect(shade, topLeft = Offset(right, top), size = Size(size.width - right, bottom - top))
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = 4f),
        )
        drawCircle(Color.White, radius = 12f, center = Offset(left, top))
        drawCircle(Color.White, radius = 12f, center = Offset(right, bottom))
    }
}

private const val MINIMUM_EDGE = 0.08f
