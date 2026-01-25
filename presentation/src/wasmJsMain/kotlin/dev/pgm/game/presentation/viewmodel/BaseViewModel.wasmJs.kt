package dev.pgm.game.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * wasmJs implementation of BaseViewModel (without androidx.lifecycle dependency)
 * Provides a simple CoroutineScope for state management
 */
actual open class BaseViewModel {
    private val job = SupervisorJob()
    val coroutineScope: CoroutineScope = CoroutineScope(job)

    actual open fun onCleared() {
        job.cancel()
    }
}
