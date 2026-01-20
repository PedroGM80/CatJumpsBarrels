package dev.pgm.game.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.pgm.game.data.database.entity.HighScoreEntity

/**
 * DAO para gestionar operaciones en la tabla high_scores.
 */
@Dao
interface HighScoreDao {

    /**
     * Obtiene los top 40 mejores scores ordenados de mayor a menor.
     */
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 40")
    suspend fun getTop40Scores(): List<HighScoreEntity>

    /**
     * Cuenta el número total de records en la base de datos.
     */
    @Query("SELECT COUNT(*) FROM high_scores")
    suspend fun getRecordCount(): Int

    /**
     * Obtiene el score mínimo de todos los records.
     * Retorna null si no hay records.
     */
    @Query("SELECT MIN(score) FROM high_scores")
    suspend fun getMinScore(): Int?

    /**
     * Inserta un nuevo score en la base de datos.
     * Retorna el ID del registro insertado.
     */
    @Insert
    suspend fun insertScore(score: HighScoreEntity): Long

    /**
     * Elimina el score más bajo (usado cuando hay más de 40 records).
     * Si hay empate en score, elimina el más antiguo.
     */
    @Query("DELETE FROM high_scores WHERE id = (SELECT id FROM high_scores ORDER BY score ASC, timestamp ASC LIMIT 1)")
    suspend fun deleteLowestScore()

    /**
     * Elimina todos los scores (útil para testing o resetear tabla).
     */
    @Query("DELETE FROM high_scores")
    suspend fun deleteAllScores()
}
