package dev.pgm.game.model.utils

import androidx.compose.ui.geometry.Offset
import kotlinx.datetime.Clock

data class ScorePopup(
    val position: Offset,
    val points: Int,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
