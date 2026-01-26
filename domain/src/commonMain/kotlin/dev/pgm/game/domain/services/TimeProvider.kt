package dev.pgm.game.domain.services

/**
 * Interface for providing time information.
 * Allows for testable time-dependent logic by abstracting time source.
 *
 * Following Dependency Inversion Principle (SOLID)
 */
interface TimeProvider {
    /**
     * Returns the current time in milliseconds since epoch
     */
    fun currentTimeMillis(): Long
}

/**
 * Returns a SystemTimeProvider instance
 */
expect fun systemTimeProvider(): TimeProvider
