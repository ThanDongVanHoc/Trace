package com.traceapp.android.ui.scan

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.traceapp.core.contracts.NormalizedRect
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal enum class RoiDragTarget {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    MOVE,
    CREATE,
}

internal data class RoiDragSession(
    val target: RoiDragTarget,
    val initialRect: NormalizedRect,
    val start: Offset,
)

internal object RoiTransform {
    fun begin(
        rect: NormalizedRect,
        point: Offset,
        canvasSize: Size,
        handleHitRadiusPx: Float,
    ): RoiDragSession {
        val corners = listOf(
            RoiDragTarget.TOP_LEFT to Offset(rect.left * canvasSize.width, rect.top * canvasSize.height),
            RoiDragTarget.TOP_RIGHT to Offset(rect.right * canvasSize.width, rect.top * canvasSize.height),
            RoiDragTarget.BOTTOM_LEFT to Offset(rect.left * canvasSize.width, rect.bottom * canvasSize.height),
            RoiDragTarget.BOTTOM_RIGHT to Offset(rect.right * canvasSize.width, rect.bottom * canvasSize.height),
        )
        val corner = corners.minByOrNull { (_, position) ->
            hypot(point.x - position.x, point.y - position.y)
        }?.takeIf { (_, position) ->
            hypot(point.x - position.x, point.y - position.y) <= handleHitRadiusPx
        }?.first

        val normalized = point.normalized(canvasSize)
        val inside = normalized.x in rect.left..rect.right && normalized.y in rect.top..rect.bottom
        return RoiDragSession(
            target = corner ?: if (inside) RoiDragTarget.MOVE else RoiDragTarget.CREATE,
            initialRect = rect,
            start = point,
        )
    }

    fun update(
        session: RoiDragSession,
        point: Offset,
        canvasSize: Size,
        minimumEdge: Float,
    ): NormalizedRect {
        val current = point.normalized(canvasSize)
        val start = session.start.normalized(canvasSize)
        val rect = session.initialRect
        return when (session.target) {
            RoiDragTarget.TOP_LEFT -> rect.copy(
                left = current.x.coerceIn(0f, rect.right - minimumEdge),
                top = current.y.coerceIn(0f, rect.bottom - minimumEdge),
            )
            RoiDragTarget.TOP_RIGHT -> rect.copy(
                right = current.x.coerceIn(rect.left + minimumEdge, 1f),
                top = current.y.coerceIn(0f, rect.bottom - minimumEdge),
            )
            RoiDragTarget.BOTTOM_LEFT -> rect.copy(
                left = current.x.coerceIn(0f, rect.right - minimumEdge),
                bottom = current.y.coerceIn(rect.top + minimumEdge, 1f),
            )
            RoiDragTarget.BOTTOM_RIGHT -> rect.copy(
                right = current.x.coerceIn(rect.left + minimumEdge, 1f),
                bottom = current.y.coerceIn(rect.top + minimumEdge, 1f),
            )
            RoiDragTarget.MOVE -> {
                val requestedX = current.x - start.x
                val requestedY = current.y - start.y
                val dx = requestedX.coerceIn(-rect.left, 1f - rect.right)
                val dy = requestedY.coerceIn(-rect.top, 1f - rect.bottom)
                NormalizedRect(rect.left + dx, rect.top + dy, rect.right + dx, rect.bottom + dy)
            }
            RoiDragTarget.CREATE -> createRect(start, current, minimumEdge)
        }
    }

    private fun createRect(anchor: Offset, current: Offset, minimumEdge: Float): NormalizedRect {
        var left = min(anchor.x, current.x)
        var right = max(anchor.x, current.x)
        var top = min(anchor.y, current.y)
        var bottom = max(anchor.y, current.y)

        if (right - left < minimumEdge) {
            if (current.x >= anchor.x) right = (left + minimumEdge).coerceAtMost(1f)
            else left = (right - minimumEdge).coerceAtLeast(0f)
        }
        if (bottom - top < minimumEdge) {
            if (current.y >= anchor.y) bottom = (top + minimumEdge).coerceAtMost(1f)
            else top = (bottom - minimumEdge).coerceAtLeast(0f)
        }
        return NormalizedRect(left, top, right, bottom)
    }

    private fun Offset.normalized(size: Size) = Offset(
        x = (x / size.width.coerceAtLeast(1f)).coerceIn(0f, 1f),
        y = (y / size.height.coerceAtLeast(1f)).coerceIn(0f, 1f),
    )
}
