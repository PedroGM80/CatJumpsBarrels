package dev.pgm.game.data.resources

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.io.File

object ImageLoader {
    /**
     * Carga una imagen desde los recursos de compose de forma moderna
     */
    fun loadResourceImage(resourcePath: String): ImageBitmap {
        // Intenta cargar desde múltiples ubicaciones posibles
        val possiblePaths = listOf(
            "catjumpsbarrels.composeapp.generated.resources/$resourcePath",
            "composeResources/catjumpsbarrels.composeapp.generated.resources/$resourcePath",
            resourcePath,
            "composeResources/$resourcePath"
        )

        for (path in possiblePaths) {
            try {
                val stream = this::class.java.classLoader.getResourceAsStream(path)
                if (stream != null) {
                    return stream.use { loadImageBitmap(it) }
                }
            } catch (e: Exception) {
                // Continuar con el siguiente path
            }
        }

        // Si no se encuentra, lanzar error descriptivo
        throw IllegalArgumentException(
            "No se pudo cargar la imagen: $resourcePath\n" +
            "Paths intentados: ${possiblePaths.joinToString()}"
        )
    }

    /**
     * Carga una imagen desde el sistema de archivos (para desarrollo)
     */
    fun loadFromFile(filePath: String): ImageBitmap {
        return File(filePath).inputStream().use { loadImageBitmap(it) }
    }
}

