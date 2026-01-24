package dev.pgm.game.data.storage

import java.io.File

/**
 * Implementación JVM del almacenamiento usando sistema de archivos.
 */
actual object DataStorage {
    
    private val storageDir = File(System.getProperty("user.home"), ".catjumpbarrels")
    
    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }
    
    actual fun read(key: String): String? {
        val file = File(storageDir, "$key.json")
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    actual fun write(key: String, data: String) {
        val file = File(storageDir, "$key.json")
        try {
            file.writeText(data)
        } catch (e: Exception) {
            // Ignorar errores de escritura
        }
    }
    
    actual fun delete(key: String) {
        val file = File(storageDir, "$key.json")
        if (file.exists()) {
            file.delete()
        }
    }
    
    actual fun exists(key: String): Boolean {
        val file = File(storageDir, "$key.json")
        return file.exists()
    }
}
