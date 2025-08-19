package com.kamel.practice.data.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Document("cars")
data class Car(
    @Id
    val id: ObjectId = ObjectId.get(),
    val code: String,
    val brand: String,
    val model: String,
    val year: Int,
    val price: Double,
    val color: String? = null,
    val pictureUrl: String? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}