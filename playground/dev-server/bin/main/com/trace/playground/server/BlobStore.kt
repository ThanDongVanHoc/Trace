package com.trace.playground.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class BlobStore(
    private val directory: Path,
) {
    fun save(referenceId: String, bytes: ByteArray): String {
        Files.createDirectories(directory)
        val target = directory.resolve("$referenceId.jpg").normalize()
        require(target.parent == directory.normalize()) { "invalid referenceId" }
        Files.write(target, bytes, StandardOpenOption.CREATE_NEW)
        return target.toString()
    }
}
