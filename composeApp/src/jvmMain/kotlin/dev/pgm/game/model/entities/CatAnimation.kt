package dev.pgm.game.model.entities

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.ImageLoader
import dev.pgm.game.model.entities.PlayerState

object CatAnimation {
    val animations: Map<PlayerState, List<ImageBitmap>> by lazy {
        mapOf(
            PlayerState.IDLE to loadAnimation("Idle", 10),
            PlayerState.RUNNING to loadAnimation("Run", 8),
            PlayerState.JUMPING to loadAnimation("Jump", 8),
            PlayerState.FALLING to loadAnimation("Fall", 8),
            PlayerState.CLIMBING to loadAnimation("Climb", 6),
            PlayerState.DEAD to loadAnimation("Dead", 10),
            PlayerState.HURT to loadAnimation("Hurt", 10)
        )
    }

    private fun loadAnimation(prefix: String, frameCount: Int): List<ImageBitmap> {
        println("🎬 Cargando animación: $prefix ($frameCount frames)")

        val frames = (1..frameCount).mapNotNull { i ->
            try {
                // Path simple - el ImageLoader probará múltiples ubicaciones
                val path = "drawable/cat/$prefix ($i).png"
                val image = ImageLoader.loadResourceImage(path)
                println("  ✓ Frame $i/$frameCount cargado")
                image
            } catch (e: Exception) {
                println("  ✗ Error en frame $i/$frameCount: ${e.message}")
                null
            }
        }

        if (frames.isEmpty()) {
            println("  ⚠️  ADVERTENCIA: No se cargó ningún frame para $prefix")
        } else {
            println("  ✓ $prefix: ${frames.size}/$frameCount frames cargados exitosamente")
        }

        return frames
    }
}
