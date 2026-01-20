package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.entities.HighScore

/**
 * UseCase para obtener los top 40 mejores scores.
 */
class GetTop40ScoresUseCase(
    private val repository: HighScoreRepository
) {
    suspend operator fun invoke(): List<HighScore> {
        return repository.getTop40Scores()
    }
}
