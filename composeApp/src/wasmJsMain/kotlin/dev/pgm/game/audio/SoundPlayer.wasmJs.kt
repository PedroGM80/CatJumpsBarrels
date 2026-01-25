package dev.pgm.game.audio

/**
 * Implementación Web del reproductor de sonidos.
 * En wasmJs, la Web Audio API no es completamente soportada.
 * Esta es una implementación sin operaciones (no-op).
 */
actual object SoundPlayer {

    actual fun setSoundEnabled(enabled: Boolean) {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playJump() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playScore() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playDeath() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playWin() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playPause() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playMenuSelect() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playMenuConfirm() {
        // Audio no disponible en wasmJs - implementación vacía
    }

    actual fun playBarrelThrow() {
        // Audio no disponible en wasmJs - implementación vacía
    }
}
