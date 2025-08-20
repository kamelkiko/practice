//package com.kamel.practice.util
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.core.convert.converter.Converter
//import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
//import org.springframework.data.mongodb.core.convert.MongoCustomConversions
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
//import java.time.LocalDateTime
//import java.time.ZoneOffset
//import java.util.*
//
///**
// * MongoDB configuration with custom converters for LocalDateTime
// */
//@Configuration
//@EnableMongoRepositories(basePackages = ["com.kamel.practice.repository"])
//class MongoConfig : AbstractMongoClientConfiguration() {
//
//    override fun getDatabaseName(): String = "Food"
//
//    /**
//     * Configure custom converters for LocalDateTime handling
//     */
//    @Bean
//    override fun customConversions(): MongoCustomConversions {
//        return MongoCustomConversions(
//            listOf(
//                LocalDateTimeToDateConverter(),
//                DateToLocalDateTimeConverter()
//            )
//        )
//    }
//
//    /**
//     * Converter from LocalDateTime to Date for MongoDB storage
//     */
//    class LocalDateTimeToDateConverter : Converter<LocalDateTime, Date> {
//        override fun convert(source: LocalDateTime): Date {
//            return Date.from(source.toInstant(ZoneOffset.UTC))
//        }
//    }
//
//    /**
//     * Converter from Date to LocalDateTime for MongoDB retrieval
//     */
//    class DateToLocalDateTimeConverter : Converter<Date, LocalDateTime> {
//        override fun convert(source: Date): LocalDateTime {
//            return source.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()
//        }
//    }
//}