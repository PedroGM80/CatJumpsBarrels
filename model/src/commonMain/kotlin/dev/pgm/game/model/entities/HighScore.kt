package dev.pgm.game.model.entities

/**
 * Modelo de dominio para un registro de high score.
 * Representa un record con el nombre del jugador, puntuación y fecha.
 */
data class HighScore(
    val id: Int = 0,
    val playerName: String,
    val score: Int,
    val timestamp: Long,
    val formattedDate: String = ""
)
