package dev.pgm.game.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

actual open class BaseViewModel actual constructor() {
    actual val coroutineScope: CoroutineScope = MainScope()

    actual open fun onCleared() {
        coroutineScope.cancel()
    }
}
