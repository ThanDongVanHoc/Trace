package com.trace.playground.server

import com.trace.playground.contracts.ApiError
import com.trace.playground.contracts.FindRequest
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.SealRequest
import com.trace.playground.contracts.SealedPayload
import com.trace.playground.enrollment.EnrollmentAlgorithm
import com.trace.playground.memory.MemoryAlgorithm
import com.trace.playground.recognition.RecognitionAlgorithm
import com.trace.playground.storage.SqliteTraceRepository
import com.trace.playground.vault.VaultAlgorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.plugins.swagger.swaggerUI
import java.nio.file.Path
import kotlinx.serialization.json.Json

fun Application.module() {
    val dataDirectory = Path.of(System.getProperty("trace.data.dir", "data"))
    tracePlayground(dataDirectory)
}

fun Application.tracePlayground(dataDirectory: Path) {
    val applicationLog = environment.log
    val repository = SqliteTraceRepository(dataDirectory.resolve("trace-dev.db")).also { it.initialize() }
    val blobs = BlobStore(dataDirectory.resolve("blobs"))
    val enrollment = EnrollmentAlgorithm()
    val recognition = RecognitionAlgorithm()
    val memory = MemoryAlgorithm(repository)
    val vault = VaultAlgorithm()

    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = false })
    }
    install(CallLogging)
    install(StatusPages) {
        exception<IllegalArgumentException> { call, failure ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError("INVALID_REQUEST", failure.message ?: "invalid request"),
            )
        }
        exception<Throwable> { call, failure ->
            applicationLog.error("Unhandled request failure", failure)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError("INTERNAL_ERROR", "The dev server could not process this request"),
            )
        }
    }

    routing {
        get("/") { call.respondRedirect("/docs") }
        get("/health") {
            call.respond(mapOf("status" to "ok", "database" to "data/trace-dev.db"))
        }
        swaggerUI(path = "docs", swaggerFile = "openapi/trace-dev.yaml")

        route("/dev") {
            post("/enrollments") {
                val input = call.receiveImageForm()
                val result = enrollment.enroll(input.toEnrollmentRequest())
                val assetPath = blobs.save(result.referenceId, input.imageBytes)
                repository.saveEnrollment(result, assetPath)
                call.respond(HttpStatusCode.Created, result)
            }

            post("/recognitions") {
                val input = call.receiveImageForm()
                val result = recognition.recognize(
                    input.toRecognitionRequest(repository.references()),
                )
                call.respond(result)
            }

            post("/memory/sightings") {
                call.respond(HttpStatusCode.Created, memory.record(call.receive<RecordSightingRequest>()))
            }

            post("/memory/find") {
                call.respond(memory.find(call.receive<FindRequest>().query))
            }

            get("/memory/objects/{objectId}/timeline") {
                val objectId = requireNotNull(call.parameters["objectId"]) { "objectId is required" }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                call.respond(memory.timeline(objectId, limit))
            }

            post("/vault/seal") {
                call.respond(vault.seal(call.receive<SealRequest>()))
            }

            post("/vault/open") {
                call.respond(vault.open(call.receive<SealedPayload>()))
            }
        }
    }
}
