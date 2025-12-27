package dev.pgm.game.model.utils

import androidx.compose.ui.geometry.Offset

data class ScorePopup(
    val position: Offset,
    val points: Int,
    val createdAt: Long = System.currentTimeMillis()
)
