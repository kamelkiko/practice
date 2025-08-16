package com.kamel.practice.util

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GameExceptionHandler {
    @ExceptionHandler(CarException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun onCarException(exception: CarException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "An error occurred",
            code = HttpStatus.BAD_GATEWAY.value()
        )
    }

    @ExceptionHandler(CarNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onCarNotFoundException(exception: CarNotFoundException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "Car not found",
            code = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(CarAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onCarAlreadyExistsException(exception: CarAlreadyExistsException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "Car already exists",
            code = HttpStatus.BAD_REQUEST.value()
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ServerResponse<String> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to it.defaultMessage
        }.entries.joinToString()
        return sendErrorResponse(errorMessage = errors, code = HttpStatus.BAD_REQUEST.value())
    }
}

open class CarException(message: String?) : RuntimeException(message)
class CarNotFoundException(message: String?) : CarException(message)
class CarAlreadyExistsException(message: String?) : CarException(message)