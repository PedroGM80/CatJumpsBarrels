package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset

data class Boss(
    val position: Offset,
    val size: Float = 60f
)
