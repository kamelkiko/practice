package com.kamel.practice.controller

data class MessageRequestDto(
    val content: String,
    val sender: String,
    val roomId: String,
)