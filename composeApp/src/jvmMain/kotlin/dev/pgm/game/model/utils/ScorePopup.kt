package dev.pgm.game.model.utils

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.core.GameConstants

data class ScorePopup(
    val position: Offset,
    val points: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() - createdAt > GameConstants.SCORE_POPUP_LIFETIME_MS
}
