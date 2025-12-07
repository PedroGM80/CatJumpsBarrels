package dev.pgm.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

object ImageLoader {
    fun loadResourceImage(path: String): ImageBitmap {
        return useResource(path) { loadImageBitmap(it) }
    }
}

