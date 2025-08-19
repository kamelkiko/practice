package com.kamel.practice.domain.util

import com.kamel.practice.api.dto.CarDto
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.springframework.stereotype.Component

@Component
class CarDtoValidator {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    fun validate(carDto: CarDto) {
        val violations = validator.validate(carDto)
        if (violations.isNotEmpty()) {
            val errors = violations.map { "${it.propertyPath}: ${it.message}" }
            throw ConstraintViolationException("Validation failed: ${errors.joinToString(", ")}", violations)
        }
    }
}