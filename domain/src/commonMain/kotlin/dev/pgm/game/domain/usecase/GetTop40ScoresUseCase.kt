package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.dto.HighScoreDto

/**
 * Caso de uso para obtener los mejores 40 scores.
 */
class GetTop40ScoresUseCase(
    private val highScoreRepository: HighScoreRepository
) {
    suspend operator fun invoke(): List<HighScoreDto> {
        return highScoreRepository.getHighScores()
    }
}
