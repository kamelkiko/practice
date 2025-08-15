package com.kamel.practice.repo

import com.kamel.practice.service.Game
import org.springframework.stereotype.Repository

@Repository
class GameRepo {
    fun createGame(game: Game): Game? {
        return if (games.contains(game) || games.find { it.id == game.id } != null) {
            null
        } else {
            games.add(game).let { game }
        }
    }

    fun getGames() = games.toList()

    fun getGameById(id: Long) = games.firstOrNull { it.id == id }

    fun deleteGameById(id: Long) = games.removeIf { it.id == id }

    fun updateGameById(id: Long, game: Game): Game? {
        val index = games.indexOfFirst { it.id == id }
        return if (index != -1) {
            games[index] = game
            game
        } else {
            null
        }
    }

    fun searchGamesByName(name: String): List<Game> {
        return games.filter { it.name.contains(name, ignoreCase = true) }
    }

    companion object {
        private val games = mutableListOf<Game>()
    }
}