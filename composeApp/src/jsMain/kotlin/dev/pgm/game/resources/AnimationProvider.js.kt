package dev.pgm.game.resources

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import catjumpsbarrels.composeapp.generated.resources.Res
import dev.pgm.game.model.entities.BossState
import dev.pgm.game.model.entities.PlayerState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.Image

/**
 * Implementación JS del AnimationProvider.
 * Usa Skia-JS para cargar imágenes.
 */
actual object AnimationProvider {

    private var catAnimations: Map<PlayerState, List<ImageBitmap>>? = null
    private var bossAnimations: Map<BossState, List<ImageBitmap>>? = null
    private var barrelEmptyBitmap: ImageBitmap? = null

    @OptIn(ExperimentalResourceApi::class)
    actual suspend fun loadAllAnimations() {
        coroutineScope {
            val idleDeferred = async { loadCatFrames("idle", 10) }
            val runDeferred = async { loadCatFrames("run", 8) }
            val jumpDeferred = async { loadCatFrames("jump", 8) }
            val fallDeferred = async { loadCatFrames("fall", 8) }
            val climbDeferred = async { loadCatFrames("climb", 6) }
            val deadDeferred = async { loadCatFrames("dead", 10) }
            val hurtDeferred = async { loadCatFrames("hurt", 10) }

            val bossIdleDeferred = async { loadBossFrames("idle", 3) }
            val bossThrowDeferred = async { loadBossFrames("throw", 3) }

            val barrelDeferred = async { loadImage("drawable/barrel_empty.png") }

            catAnimations = mapOf(
                PlayerState.IDLE to idleDeferred.await(),
                PlayerState.RUNNING to runDeferred.await(),
                PlayerState.JUMPING to jumpDeferred.await(),
                PlayerState.FALLING to fallDeferred.await(),
                PlayerState.CLIMBING to climbDeferred.await(),
                PlayerState.DEAD to deadDeferred.await(),
                PlayerState.HURT to hurtDeferred.await()
            )

            bossAnimations = mapOf(
                BossState.IDLE to bossIdleDeferred.await(),
                BossState.THROWING to bossThrowDeferred.await()
            )

            barrelEmptyBitmap = barrelDeferred.await()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadCatFrames(animName: String, frameCount: Int): List<ImageBitmap> {
        return (1..frameCount).mapNotNull { i ->
            try {
                val paddedNum = i.toString().padStart(2, '0')
                val path = "drawable/cat_${animName}_$paddedNum.png"
                loadImage(path)
            } catch (e: Exception) {
                println("Error loading cat frame: cat_${animName}_$i - ${e.message}")
                null
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadBossFrames(animName: String, frameCount: Int): List<ImageBitmap> {
        return (1..frameCount).mapNotNull { i ->
            try {
                val paddedNum = i.toString().padStart(2, '0')
                val path = "drawable/dog_${animName}_$paddedNum.png"
                loadImage(path)
            } catch (e: Exception) {
                println("Error loading boss frame: dog_${animName}_$i - ${e.message}")
                null
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadImage(path: String): ImageBitmap {
        val bytes = Res.readBytes(path)
        return Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }

    actual fun getCatAnimations(): Map<PlayerState, List<ImageBitmap>> {
        return catAnimations ?: error("Animations not loaded. Call loadAllAnimations() first.")
    }

    actual fun getBossAnimations(): Map<BossState, List<ImageBitmap>> {
        return bossAnimations ?: error("Animations not loaded. Call loadAllAnimations() first.")
    }

    actual fun getBarrelEmptyBitmap(): ImageBitmap {
        return barrelEmptyBitmap ?: error("Animations not loaded. Call loadAllAnimations() first.")
    }

    actual fun isLoaded(): Boolean = catAnimations != null && bossAnimations != null && barrelEmptyBitmap != null
}
