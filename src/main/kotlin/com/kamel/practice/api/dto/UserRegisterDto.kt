package com.kamel.practice.api.dto

data class UserRegisterDto(
    val username: String,
    val email: String,
    val password: String,
)