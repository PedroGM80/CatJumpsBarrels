package dev.pgm.game.domain.repository

import dev.pgm.game.model.entities.HighScore

/**
 * Repositorio para gestionar operaciones de high scores.
 * Define el contrato para acceder a la persistencia de records.
 */
interface HighScoreRepository {

    /**
     * Obtiene los top 40 mejores scores.
     * @return Lista de high scores ordenada de mayor a menor puntaje
     */
    suspend fun getTop40Scores(): List<HighScore>

    /**
     * Verifica si un score califica para entrar en el top 40.
     * @param score El puntaje a verificar
     * @return true si el score está en el top 40, false en caso contrario
     */
    suspend fun isHighScore(score: Int): Boolean

    /**
     * Guarda un nuevo high score.
     * Mantiene solo los top 40, eliminando el más bajo si es necesario.
     * @param playerName Nombre del jugador
     * @param score Puntaje obtenido
     * @param timestamp Timestamp del momento en que se logró el score
     * @return Result con Unit si tuvo éxito, o Exception si falló
     */
    suspend fun saveScore(playerName: String, score: Int, timestamp: Long): Result<Unit>
}
