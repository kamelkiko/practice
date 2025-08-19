package com.kamel.practice.domain.exception

import com.kamel.practice.api.dto.ServerResponse
import com.kamel.practice.api.dto.sendErrorResponse
import com.kamel.practice.api.dto.sendErrorResponseWithData
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException

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

    @ExceptionHandler(FileNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onFileNotFoundException(exception: FileNotFoundException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "File not found",
            code = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(IOException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onIOException(exception: IOException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "Something happen",
            code = HttpStatus.BAD_REQUEST.value()
        )
    }

    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onRunTimeException(exception: RuntimeException): ServerResponse<String> {
        return sendErrorResponse(
            errorMessage = exception.message ?: "An error occurred",
            code = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ServerResponse<Map<String, String>> {
        val errorMap = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.forEach { error ->
            errorMap[error.field] = error.defaultMessage ?: DEFAULT_VALIDATION_ERROR_MESSAGE
        }
        return sendErrorResponseWithData(
            data = errorMap,
            errorMessage = DEFAULT_VALIDATION_ERROR_MESSAGE,
            code = HttpStatus.BAD_REQUEST.value()
        )
    }

    companion object {
        private const val DEFAULT_VALIDATION_ERROR_MESSAGE = "Validation failed"
    }
}

open class CarException(message: String?) : RuntimeException(message)
class CarNotFoundException(message: String?) : CarException(message)
class CarAlreadyExistsException(message: String?) : CarException(message)
class FileNotFoundException(message: String?) : CarException(message)