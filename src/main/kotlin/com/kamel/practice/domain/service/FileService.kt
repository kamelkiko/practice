package com.kamel.practice.domain.service

import com.kamel.practice.domain.exception.FileNotFoundException
import com.kamel.practice.domain.util.FileStorageConfig
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class FileService(
    private val config: FileStorageConfig
) {
    fun saveFile(file: MultipartFile): String {
        val fileName = System.currentTimeMillis().toString() + "_" + file.originalFilename
        val target: Path = config.storagePath.resolve(fileName)
        Files.copy(file.inputStream, target, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }

    fun loadFile(fileName: String): Resource {
        val file: Path = config.storagePath.resolve(fileName).normalize()
        val resource = UrlResource(file.toUri())
        if (resource.exists()) return resource
        else throw FileNotFoundException("File not found: $fileName")
    }
}