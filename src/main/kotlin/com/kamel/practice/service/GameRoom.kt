package com.kamel.practice.service

data class GameRoom(
    val id: String,
    val players: MutableList<Player> = mutableListOf(),
    var phase: Phase = Phase.LOBBY,
    val deck: MutableList<Card> = newShuffledDeck(),
    val pile: MutableList<Card> = mutableListOf(),
    var hostId: String? = null,
    var lastPlay: Play? = null,
    var turnIndex: Int = 0,
    var nextRequiredRank: String = "A", // or pick random
    var currentClaim: String? = null
) {
    fun currentPlayerId(): String = players[turnIndex].id
}

fun newShuffledDeck(): MutableList<Card> {
    val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")
    val suits = listOf("♣", "♦", "♥", "♠")
    return suits.flatMap { s -> ranks.map { r -> Card(r, s) } }.shuffled().toMutableList()
}