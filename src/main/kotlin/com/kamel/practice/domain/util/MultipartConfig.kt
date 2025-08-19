package com.kamel.practice.domain.util

import jakarta.servlet.MultipartConfigElement
import org.springframework.boot.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.support.StandardServletMultipartResolver

@Configuration
class MultipartConfig {

    @Bean
    fun multipartResolver(): MultipartResolver {
        val resolver = StandardServletMultipartResolver()
        return resolver
    }

    @Bean
    fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        factory.setMaxFileSize(DataSize.ofMegabytes(5)) // 10MB max file size
        // factory.setMaxRequestSize(DataSize.ofMegabytes(15)) // 15MB max request size
        return factory.createMultipartConfig()
    }
}