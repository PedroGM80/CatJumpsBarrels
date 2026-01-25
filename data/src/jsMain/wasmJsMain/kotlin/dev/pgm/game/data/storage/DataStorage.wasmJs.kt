package dev.pgm.game.data.storage

import kotlinx.browser.localStorage

/**
 * Implementación Web del almacenamiento usando LocalStorage del navegador.
 */
actual object DataStorage {
    
    private const val PREFIX = "catjumpbarrels_"
    
    actual fun read(key: String): String? {
        return try {
            localStorage.getItem(PREFIX + key)
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun write(key: String, data: String) {
        try {
            localStorage.setItem(PREFIX + key, data)
        } catch (e: Exception) {
            // LocalStorage puede fallar si está lleno o deshabilitado
        }
    }
    
    actual fun delete(key: String) {
        try {
            localStorage.removeItem(PREFIX + key)
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
    
    actual fun exists(key: String): Boolean {
        return try {
            localStorage.getItem(PREFIX + key) != null
        } catch (e: Exception) {
            false
        }
    }
}
