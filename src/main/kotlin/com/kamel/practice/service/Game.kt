package com.kamel.practice.service

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class Game(
    @field:Min(1, "Game ID must be greater than 0")
    val id: Long,
    @field:NotBlank("Game name cannot be blank")
    val name: String,
)