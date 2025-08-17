package com.kamel.practice.domain.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class FileStorageConfig(
    @Value("\${file.upload-dir}") val uploadDir: String
) {
    val storagePath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    init {
        Files.createDirectories(storagePath)
    }
}
