package dev.pgm.game.presentation.mapper

import dev.pgm.game.model.dto.HighScoreDto
import dev.pgm.game.model.entities.HighScore
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Convierte un DTO de la capa de datos a un modelo de la capa de presentación.
 */
fun HighScoreDto.toHighScore(): HighScore {
    val instant = Instant.fromEpochMilliseconds(this.timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDate = "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/" +
                      "${localDateTime.monthNumber.toString().padStart(2, '0')}/" +
                      "${localDateTime.year}"

    return HighScore(
        playerName = this.playerName,
        score = this.score,
        timestamp = this.timestamp,
        formattedDate = formattedDate
    )
}
