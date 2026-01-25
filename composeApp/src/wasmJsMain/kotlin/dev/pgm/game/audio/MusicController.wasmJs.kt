package dev.pgm.game.audio

/**
 * Implementación Web del controlador de música.
 * En wasmJs, la Web Audio API no es completamente soportada.
 * Esta es una implementación sin operaciones (no-op).
 */
actual object MusicController {

    actual fun playMenuMusic() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun stopMusic() {
        // Audio no disponible en wasmJs - implementación vacía
    }
}
