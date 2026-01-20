package dev.pgm.game.model.entities

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.core.resources.ImageLoader
import kotlinx.coroutines.*

object CatAnimation {
    val animations: Map<PlayerState, List<ImageBitmap>> by lazy {
        // Cargar todas las animaciones en paralelo usando coroutines
        runBlocking {
            val deferredAnimations = mapOf(
                PlayerState.IDLE to async(Dispatchers.Default) { loadAnimation("Idle", 10) },
                PlayerState.RUNNING to async(Dispatchers.Default) { loadAnimation("Run", 8) },
                PlayerState.JUMPING to async(Dispatchers.Default) { loadAnimation("Jump", 8) },
                PlayerState.FALLING to async(Dispatchers.Default) { loadAnimation("Fall", 8) },
                PlayerState.CLIMBING to async(Dispatchers.Default) { loadAnimation("Climb", 6) },
                PlayerState.DEAD to async(Dispatchers.Default) { loadAnimation("Dead", 10) },
                PlayerState.HURT to async(Dispatchers.Default) { loadAnimation("Hurt", 10) }
            )

            // Esperar a que todas las animaciones se carguen
            deferredAnimations.mapValues { it.value.await() }
        }
    }

    private fun loadAnimation(prefix: String, frameCount: Int): List<ImageBitmap> {
        return (1..frameCount).mapNotNull { i ->
            try {
                // Path simple - el ImageLoader probará múltiples ubicaciones
                val path = "drawable/cat/$prefix ($i).png"
                ImageLoader.loadResourceImage(path)
            } catch (e: Exception) {
                null
            }
        }
    }
}
