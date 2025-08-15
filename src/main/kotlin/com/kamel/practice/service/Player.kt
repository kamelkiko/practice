package com.kamel.practice.service

data class Player(val id: String, val name: String, val sessionId: String, var hand: MutableList<Card>)