package dev.pgm.game.presentation.viewmodel

/**
 * Base class for all ViewModels that is platform-independent.
 * Concrete implementations will differ between JVM (using androidx.lifecycle.ViewModel)
 * and wasmJs (using a simple state holder).
 */
expect open class BaseViewModel() {
    open fun onCleared()
}
