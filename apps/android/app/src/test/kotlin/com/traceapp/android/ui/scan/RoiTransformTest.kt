package com.traceapp.android.ui.scan

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.google.common.truth.Truth.assertThat
import com.traceapp.core.contracts.NormalizedRect
import org.junit.Test

class RoiTransformTest {
    private val canvas = Size(1_000f, 800f)
    private val rect = NormalizedRect(0.2f, 0.25f, 0.8f, 0.75f)

    @Test
    fun `all four handles can resize the selection`() {
        val targets = mapOf(
            Offset(200f, 200f) to RoiDragTarget.TOP_LEFT,
            Offset(800f, 200f) to RoiDragTarget.TOP_RIGHT,
            Offset(200f, 600f) to RoiDragTarget.BOTTOM_LEFT,
            Offset(800f, 600f) to RoiDragTarget.BOTTOM_RIGHT,
        )

        targets.forEach { (point, expected) ->
            assertThat(RoiTransform.begin(rect, point, canvas, 40f).target).isEqualTo(expected)
        }
    }

    @Test
    fun `dragging inside moves the whole selection without resizing`() {
        val session = RoiTransform.begin(rect, Offset(500f, 400f), canvas, 40f)
        val moved = RoiTransform.update(session, Offset(600f, 480f), canvas, 0.08f)

        assertThat(session.target).isEqualTo(RoiDragTarget.MOVE)
        assertThat(moved.left).isWithin(0.0001f).of(0.3f)
        assertThat(moved.top).isWithin(0.0001f).of(0.35f)
        assertThat(moved.right - moved.left).isWithin(0.0001f).of(0.6f)
        assertThat(moved.bottom - moved.top).isWithin(0.0001f).of(0.5f)
    }

    @Test
    fun `moving clamps the selection to the canvas`() {
        val session = RoiTransform.begin(rect, Offset(500f, 400f), canvas, 40f)
        val moved = RoiTransform.update(session, Offset(2_000f, 2_000f), canvas, 0.08f)

        assertThat(moved.right).isWithin(0.0001f).of(1f)
        assertThat(moved.bottom).isWithin(0.0001f).of(1f)
        assertThat(moved.left).isWithin(0.0001f).of(0.4f)
        assertThat(moved.top).isWithin(0.0001f).of(0.5f)
    }

    @Test
    fun `dragging outside creates a new selection in either direction`() {
        val session = RoiTransform.begin(rect, Offset(50f, 40f), canvas, 40f)
        val created = RoiTransform.update(session, Offset(400f, 320f), canvas, 0.08f)

        assertThat(session.target).isEqualTo(RoiDragTarget.CREATE)
        assertThat(created).isEqualTo(NormalizedRect(0.05f, 0.05f, 0.4f, 0.4f))
    }

    @Test
    fun `resize never crosses the opposite corner`() {
        val session = RoiTransform.begin(rect, Offset(200f, 200f), canvas, 40f)
        val resized = RoiTransform.update(session, Offset(900f, 700f), canvas, 0.08f)

        assertThat(resized.left).isWithin(0.0001f).of(0.72f)
        assertThat(resized.top).isWithin(0.0001f).of(0.67f)
        assertThat(resized.isValid).isTrue()
    }
}
