package com.kamel.practice.api.dto

import com.kamel.practice.data.model.Car
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.util.*

data class CarDto(
    val id: String? = null,
    val code: String? = null,
    @field:NotBlank("brand can't be blank") val brand: String,
    @field:NotBlank("model can't be blank") val model: String,
    @field:Min(value = 1886, message = "year must be greater than or equal to 1886")
    @field:Max(value = 2025, message = "year must be less than or equal to 2025")
    val year: Int,
    @field:Min(value = 0, message = "price must be greater than or equal to 0")
    val price: Double,
    val color: String? = null,
    val pictureUrl: String? = null,
) : java.io.Serializable

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