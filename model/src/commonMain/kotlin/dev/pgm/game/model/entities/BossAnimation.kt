package dev.pgm.game.model.entities

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.data.resources.ImageLoader
import kotlinx.coroutines.*

object BossAnimation {
    val animations: Map<BossState, List<ImageBitmap>> by lazy {
        runBlocking {
            val deferredAnimations = mapOf(
                BossState.IDLE to async(Dispatchers.Default) { loadIdleAnimation() },
                BossState.THROWING to async(Dispatchers.Default) { loadThrowAnimation() }
            )

            deferredAnimations.mapValues { it.value.await() }
        }
    }

    private fun loadIdleAnimation(): List<ImageBitmap> {
        val frame0 = try {
            ImageLoader.loadResourceImage("drawable/dog/dogIde0.png")
        } catch (e: Exception) {
            null
        }

        val frame1 = try {
            ImageLoader.loadResourceImage("drawable/dog/dogIde1.png")
        } catch (e: Exception) {
            null
        }

        val frame2 = try {
            ImageLoader.loadResourceImage("drawable/dog/dogIde2.png")
        } catch (e: Exception) {
            null
        }

        // Secuencia de respiración más lenta - repetir cada frame 3 veces
        return listOfNotNull(
            frame0, frame0, frame0,  // Frame 0 (3 veces)
            frame1, frame1, frame1,  // Frame 1 (3 veces)
            frame2, frame2, frame2,  // Frame 2 (3 veces)
            frame1, frame1, frame1   // Frame 1 de nuevo (3 veces)
        ).takeIf { it.isNotEmpty() } ?: emptyList()
    }

    private fun loadThrowAnimation(): List<ImageBitmap> {
        val frameTake = try {
            ImageLoader.loadResourceImage("drawable/dog/dogTakeBarrel.png")
        } catch (e: Exception) {
            null
        }

        val frameLaunch0 = try {
            ImageLoader.loadResourceImage("drawable/dog/dogLaunchBarrel0.png")
        } catch (e: Exception) {
            null
        }

        val frameLaunch2 = try {
            ImageLoader.loadResourceImage("drawable/dog/dogLaunchBarrel2.png")
        } catch (e: Exception) {
            null
        }

        // Secuencia rápida sin dogLaunchBarrel1 (9 frames = 0.9 segundos)
        return listOfNotNull(
            // Fase 1: Tomar el barril (3 frames)
            frameTake, frameTake, frameTake,

            // Fase 2: Preparar el lanzamiento (3 frames)
            frameLaunch0, frameLaunch0, frameLaunch0,

            // Fase 3: Después de lanzar, sin barril (3 frames) - El barril se crea en el frame 6
            frameLaunch2, frameLaunch2, frameLaunch2
        ).takeIf { it.isNotEmpty() } ?: emptyList()
    }
}
