package dev.pgm.game.domain.services

import kotlinx.datetime.Clock

/**
 * Interface for providing time information.
 * Allows for testable time-dependent logic by abstracting Clock.System
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
 * Default implementation using Clock.System (multiplatform)
 */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
