package dev.pgm.game.model.entities

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.ImageLoader
import dev.pgm.game.model.entities.PlayerState
import kotlinx.coroutines.*

object CatAnimation {
    val animations: Map<PlayerState, List<ImageBitmap>> by lazy {
        // Cargar todas las animaciones en paralelo usando coroutines
        runBlocking {
            val startTime = System.currentTimeMillis()
            println("🎬 Iniciando carga paralela de animaciones...")

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
            val result = deferredAnimations.mapValues { it.value.await() }

            val endTime = System.currentTimeMillis()
            println("✅ Carga paralela completada en ${endTime - startTime}ms")

            result
        }
    }

    private fun loadAnimation(prefix: String, frameCount: Int): List<ImageBitmap> {
        println("🎬 Cargando animación: $prefix ($frameCount frames) [Thread: ${Thread.currentThread().name}]")

        val frames = (1..frameCount).mapNotNull { i ->
            try {
                // Path simple - el ImageLoader probará múltiples ubicaciones
                val path = "drawable/cat/$prefix ($i).png"
                val image = ImageLoader.loadResourceImage(path)
                image
            } catch (e: Exception) {
                println("  ✗ Error en frame $i/$frameCount de $prefix: ${e.message}")
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
