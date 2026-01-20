package dev.pgm.game.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class HighScoreDto(
    val playerName: String,
    val score: Int,
    val timestamp: Long
)
