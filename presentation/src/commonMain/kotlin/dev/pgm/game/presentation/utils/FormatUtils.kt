package dev.pgm.game.presentation.utils

/**
 * Format a number with thousands separators.
 * Works on all platforms (JVM, wasmJs, iOS, etc.)
 */
fun Int.formatWithCommas(): String {
    return this.toString().reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

/**
 * Format a number with thousands separators.
 * Works on all platforms (JVM, wasmJs, iOS, etc.)
 */
fun Long.formatWithCommas(): String {
    return this.toString().reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}
