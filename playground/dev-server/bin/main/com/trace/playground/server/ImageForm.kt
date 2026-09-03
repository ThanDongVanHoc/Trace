package com.trace.playground.server

import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.Roi
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

data class ImageForm(
    val imageBytes: ByteArray,
    val fields: Map<String, String>,
) {
    fun toEnrollmentRequest(): EnrollmentRequest = EnrollmentRequest(
        tag = fields.required("tag"),
        image = ImageInput(imageBytes, fields["rotationDegrees"]?.toIntOrNull() ?: 0),
        roi = Roi(
            left = fields.requiredFloat("roiLeft"),
            top = fields.requiredFloat("roiTop"),
            right = fields.requiredFloat("roiRight"),
            bottom = fields.requiredFloat("roiBottom"),
        ),
    )

    fun toRecognitionRequest(references: List<ReferenceVector>): RecognitionRequest = RecognitionRequest(
        image = ImageInput(imageBytes, fields["rotationDegrees"]?.toIntOrNull() ?: 0),
        references = references,
        minimumSimilarity = fields["minimumSimilarity"]?.toFloatOrNull() ?: 0.75f,
        maximumResults = fields["maximumResults"]?.toIntOrNull() ?: 5,
    )
}

suspend fun ApplicationCall.receiveImageForm(): ImageForm {
    val fields = mutableMapOf<String, String>()
    var imageBytes: ByteArray? = null
    receiveMultipart(formFieldLimit = 10 * 1024 * 1024).forEachPart { part ->
        try {
            when (part) {
                is PartData.FormItem -> part.name?.let { fields[it] = part.value }
                is PartData.FileItem -> {
                    if (part.name == "image") {
                        require(part.contentType?.contentType == "image" &&
                            part.contentType?.contentSubtype == "jpeg") {
                            "image must use the image/jpeg content type"
                        }
                        require(imageBytes == null) { "only one image is accepted" }
                        imageBytes = part.provider().readRemaining().readByteArray()
                    }
                }
                else -> Unit
            }
        } finally {
            part.release()
        }
    }
    val image = requireNotNull(imageBytes) { "multipart field 'image' is required" }
    require(image.size <= 10 * 1024 * 1024) { "image must not exceed 10 MiB" }
    return ImageForm(image, fields)
}

private fun Map<String, String>.required(name: String): String =
    this[name]?.takeIf(String::isNotBlank) ?: throw IllegalArgumentException("$name is required")

private fun Map<String, String>.requiredFloat(name: String): Float =
    required(name).toFloatOrNull() ?: throw IllegalArgumentException("$name must be a number")
