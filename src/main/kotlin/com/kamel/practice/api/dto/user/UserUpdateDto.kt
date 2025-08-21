package com.kamel.practice.api.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class UserUpdateDto(
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String? = null,
    @field:NotBlank(message = "Email cannot be blank")
    @field:Length(max = 50, message = "Email must not exceed 50 characters")
    @field:Email(message = "Email should be valid")
    val email: String? = null,
    @field:NotBlank(message = "Password cannot be blank")
    @field:Length(min = 6, message = "Password must be between 6 and 100 characters")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,100}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    val password: String? = null,
)