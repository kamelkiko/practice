package com.kamel.practice.util

import com.kamel.practice.util.ServerResponse.ResponseStatus

data class ServerResponse<T>(
    val data: T?,
    val isSuccess: Boolean,
    val status: ResponseStatus,
) {
    data class ResponseStatus(
        val messageError: String? = null,
        val messageSuccess: String? = null,
        val code: Int?,
    )
}

fun sendErrorResponse(errorMessage: String?, code: Int): ServerResponse<String> {
    return ServerResponse(
        data = null,
        isSuccess = false,
        status = ResponseStatus(messageError = errorMessage, code = code)
    )
}

inline fun <reified T> sendSuccessResponse(data: T, successMessage: String?, code: Int = 200): ServerResponse<T> {
    return ServerResponse(
        data = data,
        isSuccess = true,
        status = ResponseStatus(code = code, messageSuccess = successMessage)
    )
}