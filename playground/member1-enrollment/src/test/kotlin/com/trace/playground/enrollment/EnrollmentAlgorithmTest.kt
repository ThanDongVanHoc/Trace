package com.trace.playground.enrollment

import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.Roi
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnrollmentAlgorithmTest {
    @Test
    fun `enrolls one tagged ROI`() = runBlocking {
        val result = EnrollmentAlgorithm().enroll(
            EnrollmentRequest("balo", ImageInput(testJpeg()), Roi(0f, 0f, 1f, 1f)),
        )
        assertEquals("balo", result.tag)
        assertEquals(64, result.embedding.values.size)
    }

    @Test
    fun `rejects invalid ROI`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            EnrollmentAlgorithm().enroll(
                EnrollmentRequest("balo", ImageInput(testJpeg()), Roi(0.9f, 0f, 0.2f, 1f)),
            )
        }
    }

    private fun testJpeg(): ByteArray {
        val image = BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB)
        repeat(128) { y -> repeat(128) { x -> image.setRGB(x, y, (x * 2 shl 16) or (y * 2 shl 8)) } }
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "jpg", output)
            output.toByteArray()
        }
    }
}
