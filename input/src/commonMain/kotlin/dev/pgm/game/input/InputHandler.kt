package dev.pgm.game.input

import androidx.compose.ui.input.key.*

/**
 * Handler for keyboard input events.
 * Separates input logic from UI layer, following Single Responsibility Principle (SOLID)
 *
 * Maps keyboard events to GameInput state
 */
class InputHandler {

    /**
     * Processes a keyboard event and returns updated GameInput
     *
     * @param event The keyboard event to process
     * @param currentInput The current input state
     * @param onRestart Callback when restart is requested (R key)
     * @param onPause Callback when pause is requested (P or Escape key)
     * @return Pair of (updated GameInput, was event consumed)
     */
    fun handleKeyEvent(
        event: KeyEvent,
        currentInput: GameInput,
        onRestart: () -> Unit = {},
        onPause: () -> Unit = {}
    ): Pair<GameInput, Boolean> {
        return when (event.type) {
            KeyEventType.KeyDown -> handleKeyDown(event, currentInput, onRestart, onPause)
            KeyEventType.KeyUp -> handleKeyUp(event, currentInput)
            else -> currentInput to false
        }
    }

    private fun handleKeyDown(
        event: KeyEvent,
        input: GameInput,
        onRestart: () -> Unit,
        onPause: () -> Unit
    ): Pair<GameInput, Boolean> {
        return when (event.key) {
            Key.R -> {
                onRestart()
                input to true
            }
            Key.P, Key.Escape -> {
                onPause()
                input to true
            }
            Key.DirectionLeft, Key.A -> input.copy(left = true) to true
            Key.DirectionRight, Key.D -> input.copy(right = true) to true
            Key.DirectionUp, Key.W -> input.copy(up = true) to true
            Key.DirectionDown, Key.S -> input.copy(down = true) to true
            Key.Spacebar -> input.copy(jump = true) to true
            else -> input to false
        }
    }

    private fun handleKeyUp(
        event: KeyEvent,
        input: GameInput
    ): Pair<GameInput, Boolean> {
        return when (event.key) {
            Key.DirectionLeft, Key.A -> input.copy(left = false) to true
            Key.DirectionRight, Key.D -> input.copy(right = false) to true
            Key.DirectionUp, Key.W -> input.copy(up = false) to true
            Key.DirectionDown, Key.S -> input.copy(down = false) to true
            Key.Spacebar -> input.copy(jump = false) to true
            else -> input to false
        }
    }
}
