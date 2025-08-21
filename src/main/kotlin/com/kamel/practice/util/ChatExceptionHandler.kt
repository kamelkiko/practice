package com.kamel.practice.util

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {
    @ExceptionHandler(ChatException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun onChatException(exception: ChatException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "An error occurred",
            code = HttpStatus.BAD_GATEWAY.value()
        )
    }

    @ExceptionHandler(ChatNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onChatNotFoundException(exception: ChatNotFoundException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Chat not found",
            code = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(ChatAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onChatAlreadyExistsException(exception: ChatAlreadyExistsException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Chat already exists",
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
}

@ControllerAdvice
class ChatWsExceptionHandler {
    @ExceptionHandler(ChatException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun onChatException(exception: ChatException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "An error occurred",
            code = HttpStatus.BAD_GATEWAY.value()
        )
    }

    @ExceptionHandler(ChatNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onChatNotFoundException(exception: ChatNotFoundException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Chat not found",
            code = HttpStatus.NOT_FOUND.value()
        )
    }

    @ExceptionHandler(ChatAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onChatAlreadyExistsException(exception: ChatAlreadyExistsException): ServerResponse<String> {
        return ServerResponse.error(
            errorMessage = exception.message ?: "Chat already exists",
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
}


open class ChatException(message: String?) : RuntimeException(message)
class ChatNotFoundException(message: String?) : ChatException(message)
class ChatAlreadyExistsException(message: String?) : ChatException(message)