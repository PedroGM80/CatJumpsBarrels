package dev.pgm.game.model.entities

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.utils.GameRect

data class Princess(
    val position: Offset,
    val size: Float = 28f
) {
    val hitbox: GameRect
        get() = GameRect(
            position.x,
            position.y,
            size,
            size
        )
}
