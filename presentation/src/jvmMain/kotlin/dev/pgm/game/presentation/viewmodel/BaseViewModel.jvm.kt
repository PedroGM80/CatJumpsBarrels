package dev.pgm.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * JVM implementation of BaseViewModel using androidx.lifecycle.ViewModel
 */
actual open class BaseViewModel : ViewModel() {
    protected val coroutineScope: CoroutineScope
        get() = viewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }
}
