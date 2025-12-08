package dev.pgm.game.domain.services

import kotlin.random.Random

/**
 * Interface for providing random number generation.
 * Allows for deterministic testing by abstracting Random
 *
 * Following Dependency Inversion Principle (SOLID)
 */
interface RandomProvider {
    /**
     * Returns a random float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    fun nextFloat(): Float

    /**
     * Returns a random int in the specified range
     */
    fun nextInt(from: Int, until: Int): Int

    /**
     * Returns a random boolean
     */
    fun nextBoolean(): Boolean
}

/**
 * Default implementation using kotlin.random.Random
 */
class DefaultRandomProvider(
    private val random: Random = Random.Default
) : RandomProvider {
    override fun nextFloat(): Float = random.nextFloat()

    override fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)

    override fun nextBoolean(): Boolean = random.nextBoolean()
}
