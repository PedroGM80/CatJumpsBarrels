package dev.pgm.game.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar registros de high scores.
 */
@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
