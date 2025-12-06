package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset

data class Boss(
    val position: Offset,
    val size: Float = 60f,
    val animationFrame: Int = 0,
    val isThrowingBarrel: Boolean = false
)
