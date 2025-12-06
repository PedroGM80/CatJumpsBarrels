package dev.pgm.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

/**
 * Sistema de carga de imagenes para el juego.
 * 
 * Actualmente el juego usa graficos vectoriales generados por codigo.
 * Este archivo proporciona la infraestructura para cargar sprites PNG
 * si decides agregar assets graficos en el futuro.
 * 
 * Para usar sprites PNG:
 * 1. Coloca las imagenes en: composeApp/src/jvmMain/resources/
 * 2. Llama a loadGameImages() al inicio
 * 3. Modifica GameRenderer para usar las imagenes cargadas
 */

data class GameImages(
    val player: ImageBitmap? = null,
    val playerWalk: List<ImageBitmap> = emptyList(),
    val playerJump: ImageBitmap? = null,
    val playerClimb: List<ImageBitmap> = emptyList(),
    val donkeyKong: ImageBitmap? = null,
    val donkeyKongThrow: ImageBitmap? = null,
    val princess: ImageBitmap? = null,
    val barrel: ImageBitmap? = null,
    val platform: ImageBitmap? = null,
    val ladder: ImageBitmap? = null,
    val background: ImageBitmap? = null
)

/**
 * Carga todas las imagenes del juego.
 * Retorna null para imagenes que no existen, permitiendo fallback a graficos vectoriales.
 */
fun loadGameImages(): GameImages {
    return GameImages(
        player = loadImageSafe("sprites/player.png"),
        playerWalk = listOf(
            loadImageSafe("sprites/player_walk1.png"),
            loadImageSafe("sprites/player_walk2.png"),
            loadImageSafe("sprites/player_walk3.png"),
            loadImageSafe("sprites/player_walk4.png")
        ).filterNotNull(),
        playerJump = loadImageSafe("sprites/player_jump.png"),
        playerClimb = listOf(
            loadImageSafe("sprites/player_climb1.png"),
            loadImageSafe("sprites/player_climb2.png")
        ).filterNotNull(),
        donkeyKong = loadImageSafe("sprites/donkeykong.png"),
        donkeyKongThrow = loadImageSafe("sprites/donkeykong_throw.png"),
        princess = loadImageSafe("sprites/princess.png"),
        barrel = loadImageSafe("sprites/barrel.png"),
        platform = loadImageSafe("sprites/platform.png"),
        ladder = loadImageSafe("sprites/ladder.png"),
        background = loadImageSafe("sprites/background.png")
    )
}

private fun loadImageSafe(path: String): ImageBitmap? {
    return try {
        useResource(path) { loadImageBitmap(it) }
    } catch (e: Exception) {
        // Imagen no encontrada - usar graficos vectoriales como fallback
        null
    }
}

/**
 * Clase para manejar sprites animados.
 */
data class AnimatedSprite(
    val frames: List<ImageBitmap>,
    val frameDuration: Long = 100L
) {
    fun getFrame(timeMillis: Long): ImageBitmap? {
        if (frames.isEmpty()) return null
        val frameIndex = ((timeMillis / frameDuration) % frames.size).toInt()
        return frames[frameIndex]
    }
}

/**
 * Extension para crear un AnimatedSprite desde una lista de imagenes.
 */
fun List<ImageBitmap>.toAnimatedSprite(frameDuration: Long = 100L): AnimatedSprite? {
    return if (isNotEmpty()) AnimatedSprite(this, frameDuration) else null
}
