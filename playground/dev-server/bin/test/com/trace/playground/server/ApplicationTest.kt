package com.trace.playground.server

import com.trace.playground.contracts.EnrollmentResult
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.createTempDirectory
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `health endpoint starts with an embedded database`() = testApplication {
        application { tracePlayground(createTempDirectory()) }
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }

    @Test
    fun `enrollment then memory search works through HTTP and SQLite`() = testApplication {
        application { tracePlayground(createTempDirectory()) }
        val enrolledResponse = client.post("/dev/enrollments") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("tag", "balo")
                        append("roiLeft", "0")
                        append("roiTop", "0")
                        append("roiRight", "1")
                        append("roiBottom", "1")
                        append(
                            "image",
                            testJpeg(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=test.jpg")
                            },
                        )
                    },
                ),
            )
        }
        assertEquals(HttpStatusCode.Created, enrolledResponse.status)
        val enrolled = Json.decodeFromString<EnrollmentResult>(enrolledResponse.bodyAsText())

        val sightingResponse = client.post("/dev/memory/sightings") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"objectId":"${enrolled.objectId}","detectedAtEpochMillis":1000,"confidence":0.9}""",
            )
        }
        assertEquals(HttpStatusCode.Created, sightingResponse.status)

        val findResponse = client.post("/dev/memory/find") {
            contentType(ContentType.Application.Json)
            setBody("""{"query":"bal"}""")
        }
        assertEquals(HttpStatusCode.OK, findResponse.status)
        assertContains(findResponse.bodyAsText(), enrolled.objectId)
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
