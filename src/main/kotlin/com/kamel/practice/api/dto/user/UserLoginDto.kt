package com.kamel.practice.api.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class UserLoginDto(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Length(max = 50, message = "Email must not exceed 50 characters")
    @field:Email(message = "Email should be valid")
    val email: String,
    @field:NotBlank(message = "Password cannot be blank")
    val password: String,
)