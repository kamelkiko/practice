package com.kamel.practice.util

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GameExceptionHandler {
    @ExceptionHandler(GameException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun onGameException(exception: GameException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "An error occurred",
            code = HttpStatus.BAD_GATEWAY.value()
        )
    }

    @ExceptionHandler(GameNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onGameNotFoundException(exception: GameNotFoundException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Game not found",
            code = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(GameAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onGameAlreadyExistsException(exception: GameAlreadyExistsException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Game already exists",
            code = HttpStatus.BAD_REQUEST.value()
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ServerResponse<String> {
        val errors = ex.bindingResult.fieldErrors.associate {
            it.field to it.defaultMessage
        }.entries.joinToString()
        return ServerResponse.error(errorMessage = errors, code = HttpStatus.BAD_REQUEST.value())
    }

//    @ExceptionHandler(InvalidGameDataException::class)
//    fun onInvalidGameDataException(exception: InvalidGameDataException): String {
//        return exception.message ?: "Invalid game data"
//    }
}


open class GameException(message: String?) : RuntimeException(message)
class GameNotFoundException(message: String?) : GameException(message)
class GameAlreadyExistsException(message: String?) : GameException(message)
class InvalidGameDataException(message: String?) : GameException(message)