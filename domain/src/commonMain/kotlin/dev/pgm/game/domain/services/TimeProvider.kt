package dev.pgm.game.domain.services

/**
 * Interface for providing time information.
 * Allows for testable time-dependent logic by abstracting System.currentTimeMillis()
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
 * Default implementation using System.currentTimeMillis()
 */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
