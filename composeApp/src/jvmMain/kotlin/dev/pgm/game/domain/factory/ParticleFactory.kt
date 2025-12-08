package dev.pgm.game.domain.factory

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.pgm.game.domain.services.RandomProvider
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.utils.Particle

/**
 * Factory for creating particles with different configurations.
 * Eliminates code duplication and centralizes particle creation logic.
 *
 * Following Factory Pattern and Single Responsibility Principle (SOLID)
 */
class ParticleFactory(
    private val randomProvider: RandomProvider
) {

    /**
     * Types of particles that can be created
     */
    enum class ParticleType {
        SCORE,    // Particles when jumping over barrels
        DEATH     // Particles when player dies
    }

    /**
     * Creates a list of particles at the specified position
     */
    fun createParticles(
        x: Float,
        y: Float,
        type: ParticleType
    ): List<Particle> {
        val config = getConfig(type)

        return List(config.count) {
            Particle(
                position = Offset(
                    x + randomProvider.nextFloat() * config.positionSpread,
                    y + randomProvider.nextFloat() * config.positionSpread
                ),
                velocity = Offset(
                    (randomProvider.nextFloat() - 0.5f) * config.velocityXRange,
                    randomProvider.nextFloat() * config.velocityYRange
                ),
                color = getParticleColor(type),
                size = config.baseSize + randomProvider.nextFloat() * config.sizeRange,
                lifetime = config.lifetime,
                age = 0f
            )
        }
    }

    /**
     * Gets configuration for a specific particle type
     */
    private fun getConfig(type: ParticleType): ParticleConfig {
        return when (type) {
            ParticleType.SCORE -> ParticleConfig(
                count = GameConstants.SCORE_PARTICLE_COUNT,
                positionSpread = 20f,
                velocityXRange = GameConstants.SCORE_PARTICLE_VEL_X_RANGE,
                velocityYRange = GameConstants.SCORE_PARTICLE_VEL_Y_RANGE,
                baseSize = 2f,
                sizeRange = GameConstants.SCORE_PARTICLE_SIZE_RANGE,
                lifetime = GameConstants.SCORE_PARTICLE_LIFETIME
            )
            ParticleType.DEATH -> ParticleConfig(
                count = GameConstants.DEATH_PARTICLE_COUNT,
                positionSpread = 30f,
                velocityXRange = GameConstants.DEATH_PARTICLE_VEL_X_RANGE,
                velocityYRange = GameConstants.DEATH_PARTICLE_VEL_Y_RANGE,
                baseSize = 3f,
                sizeRange = GameConstants.DEATH_PARTICLE_SIZE_RANGE,
                lifetime = GameConstants.DEATH_PARTICLE_LIFETIME
            )
        }
    }

    /**
     * Gets color for a specific particle type
     */
    private fun getParticleColor(type: ParticleType): Long {
        return when (type) {
            ParticleType.SCORE -> {
                // Random colorful particles for score
                Color(
                    randomProvider.nextFloat(),
                    randomProvider.nextFloat(),
                    randomProvider.nextFloat(),
                    1f
                ).value.toLong()
            }
            ParticleType.DEATH -> {
                // Red particles for death
                Color.Red.value.toLong()
            }
        }
    }

    /**
     * Internal configuration for particle generation
     */
    private data class ParticleConfig(
        val count: Int,
        val positionSpread: Float,
        val velocityXRange: Float,
        val velocityYRange: Float,
        val baseSize: Float,
        val sizeRange: Float,
        val lifetime: Float
    )
}
