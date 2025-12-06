package dev.pgm.game.input

data class GameInput(
    val left: Boolean = false,
    val right: Boolean = false,
    val up: Boolean = false,
    val down: Boolean = false,
    val jump: Boolean = false
)
