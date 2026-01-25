package dev.pgm.game.resources

import androidx.compose.ui.graphics.ImageBitmap
import dev.pgm.game.model.entities.BossState
import dev.pgm.game.model.entities.PlayerState

/**
 * Provee las animaciones del juego usando Compose Multiplatform Resources.
 * Implementación expect/actual para soportar JVM y JS.
 */
expect object AnimationProvider {
    suspend fun loadAllAnimations()
    fun getCatAnimations(): Map<PlayerState, List<ImageBitmap>>
    fun getBossAnimations(): Map<BossState, List<ImageBitmap>>
    fun getBarrelEmptyBitmap(): ImageBitmap
    fun isLoaded(): Boolean
}
