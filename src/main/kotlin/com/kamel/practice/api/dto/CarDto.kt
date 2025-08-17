package com.kamel.practice.api.dto

import com.kamel.practice.data.model.Car
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class CarDto(
    val id: String? = null,
    val code: String? = null,
    @field:NotBlank("brand can't be blank") val brand: String,
    @field:NotBlank("model can't be blank") val model: String,
    @field:Size(min = 1900, max = 2025, message = "year should be between 1900 and 2025")
    val year: Int,
    @field:Size(min = 0, message = "price should be a positive number")
    val price: Double,
    val color: String? = null,
    val pictureUrl: String? = null,
)

fun CarDto.toEntity(): Car {
    return Car(
        code = code ?: createRandomCode(model),
        brand = brand,
        model = model,
        year = year,
        price = price,
        color = color,
        pictureUrl = pictureUrl
    )
}

fun Car.toDto(): CarDto {
    return CarDto(
        id = id.toHexString(),
        code = code,
        brand = brand,
        model = model,
        year = year,
        price = price,
        color = color,
        pictureUrl = pictureUrl
    )
}

private fun createRandomCode(prefix: String): String {
    return prefix + "-" + UUID.randomUUID().toString().split("-").first()
}