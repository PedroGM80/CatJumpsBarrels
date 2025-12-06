package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset

data class Ladder(
    val position: Offset,
    val width: Float = 28f,
    val height: Float,
    val topPlatformIndex: Int,
    val bottomPlatformIndex: Int
) {
    val centerX: Float get() = position.x + width / 2
    val top: Float get() = position.y
    val bottom: Float get() = position.y + height
}
