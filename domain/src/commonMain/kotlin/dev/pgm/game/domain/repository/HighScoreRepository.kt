package dev.pgm.game.domain.repository

import dev.pgm.game.model.dto.HighScoreDto

interface HighScoreRepository {
    suspend fun getHighScores(): List<HighScoreDto>
    suspend fun saveHighScores(scores: List<HighScoreDto>)
}
