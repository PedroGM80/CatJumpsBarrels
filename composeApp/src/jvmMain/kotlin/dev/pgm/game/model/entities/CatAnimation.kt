package dev.pgm.game.model.entities

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.ImageLoader
import dev.pgm.game.model.entities.PlayerState

object CatAnimation {
    private const val BASE_PATH = "composeResources/catjumpsbarrels.composeapp.generated.resources/drawable/cat/"

    val animations: Map<PlayerState, List<ImageBitmap>> = mapOf(
        PlayerState.IDLE to loadAnimation("Idle", 10),
        PlayerState.RUNNING to loadAnimation("Run", 8),
        PlayerState.JUMPING to loadAnimation("Jump", 3),
        PlayerState.FALLING to loadAnimation("Fall", 8),
        PlayerState.DEAD to loadAnimation("Dead", 10),
        PlayerState.HURT to loadAnimation("Hurt", 10)
    )

    private fun loadAnimation(prefix: String, frameCount: Int): List<ImageBitmap> {
        return (1..frameCount).map { i ->
            ImageLoader.loadResourceImage("$BASE_PATH$prefix ($i).png")
        }
    }
}
