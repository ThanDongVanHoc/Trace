package com.trace.playground.enrollment

import com.trace.playground.contracts.EnrollmentEngine
import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.ReferenceVector
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.math.floor

/**
 * Thành viên 1 chỉ thay đổi implementation này và test tương ứng.
 * HTTP, SQLite và Android không thuộc phạm vi của thuật toán enrollment.
 */
class EnrollmentAlgorithm : EnrollmentEngine {
    override suspend fun enroll(request: EnrollmentRequest): EnrollmentResult {
        require(request.tag.trim().length in 1..80) { "tag must contain 1 to 80 characters" }
        require(request.roi.isValid) { "ROI must be normalized and non-empty" }
        val image = decodeJpeg(request.image.jpegBytes)
        val crop = crop(image, request)
        require(crop.width >= 32 && crop.height >= 32) { "ROI must be at least 32 x 32 pixels" }

        val quality = qualityScore(crop)
        require(quality >= 0.08f) { "ROI is too dark or has too little visual information" }

        val objectId = UUID.randomUUID().toString()
        val referenceId = UUID.randomUUID().toString()
        val vector = encodePrototype(crop)
        val embedding = ReferenceVector(
            referenceId = referenceId,
            objectId = objectId,
            tag = request.tag.trim(),
            values = vector,
            modelName = "trace-prototype-grid",
            modelVersion = "1",
        )
        return EnrollmentResult(
            objectId = objectId,
            referenceId = referenceId,
            tag = request.tag.trim(),
            qualityScore = quality,
            embedding = embedding,
            warnings = listOf("Prototype encoder: replace with the member 1 solution"),
        )
    }

    private fun decodeJpeg(bytes: ByteArray): BufferedImage {
        require(bytes.size >= 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
            "image must be a valid JPEG"
        }
        return ImageIO.read(ByteArrayInputStream(bytes))
            ?: throw IllegalArgumentException("image cannot be decoded")
    }

    private fun crop(image: BufferedImage, request: EnrollmentRequest): BufferedImage {
        val left = floor(request.roi.left * image.width).toInt().coerceIn(0, image.width - 1)
        val top = floor(request.roi.top * image.height).toInt().coerceIn(0, image.height - 1)
        val right = floor(request.roi.right * image.width).toInt().coerceIn(left + 1, image.width)
        val bottom = floor(request.roi.bottom * image.height).toInt().coerceIn(top + 1, image.height)
        return image.getSubimage(left, top, right - left, bottom - top)
    }

    private fun qualityScore(image: BufferedImage): Float {
        val values = sampleGrid(image, 8).map { it * 255.0 }
        val mean = values.average()
        val variance = values.sumOf { value -> (value - mean) * (value - mean) } / values.size
        val exposure = (1.0 - kotlin.math.abs(mean - 127.5) / 127.5).coerceIn(0.0, 1.0)
        val detail = (variance / 4_000.0).coerceIn(0.0, 1.0)
        return (exposure * 0.4 + detail * 0.6).toFloat()
    }

    private fun encodePrototype(image: BufferedImage): List<Float> = sampleGrid(image, 8)

    private fun sampleGrid(image: BufferedImage, size: Int): List<Float> = buildList(size * size) {
        repeat(size) { gridY ->
            repeat(size) { gridX ->
                val x = ((gridX + 0.5) * image.width / size).toInt().coerceIn(0, image.width - 1)
                val y = ((gridY + 0.5) * image.height / size).toInt().coerceIn(0, image.height - 1)
                val rgb = image.getRGB(x, y)
                val red = rgb shr 16 and 0xFF
                val green = rgb shr 8 and 0xFF
                val blue = rgb and 0xFF
                add((red * 0.299f + green * 0.587f + blue * 0.114f) / 255f)
            }
        }
    }
}
