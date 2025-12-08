package dev.pgm.game.domain.factory

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.pgm.game.domain.services.RandomProvider
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
        SCORE,            // Particles when jumping over barrels (points popup)
        DEATH,            // Particles when player dies
        BARREL_DESTROYED  // Particles when barrel is destroyed by jumping
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
                count = 8,  // Más partículas para mejor efecto
                positionSpread = 15f,
                velocityXRange = 120f,  // Mayor velocidad horizontal
                velocityYRange = -100f,  // Mayor velocidad vertical
                baseSize = 2.5f,
                sizeRange = 4f,
                lifetime = 0.6f
            )
            ParticleType.DEATH -> ParticleConfig(
                count = 20,  // Muchas más partículas para explosión dramática
                positionSpread = 25f,
                velocityXRange = 200f,  // Explosión más amplia
                velocityYRange = -150f,  // Partículas vuelan más alto
                baseSize = 3f,
                sizeRange = 6f,
                lifetime = 0.8f
            )
            ParticleType.BARREL_DESTROYED -> ParticleConfig(
                count = 15,  // Buena cantidad de fragmentos de barril
                positionSpread = 20f,
                velocityXRange = 180f,  // Explosión amplia
                velocityYRange = -130f,  // Fragmentos vuelan hacia arriba
                baseSize = 2.5f,
                sizeRange = 5f,
                lifetime = 0.7f
            )
        }
    }

    /**
     * Gets color for a specific particle type
     */
    private fun getParticleColor(type: ParticleType): ULong {
        return when (type) {
            ParticleType.SCORE -> {
                // Random colorful bright particles for score
                val colors = listOf(
                    Color(0xFFFFD700),  // Dorado
                    Color(0xFFFFFF00),  // Amarillo
                    Color(0xFFFFA500),  // Naranja
                    Color(0xFFFF69B4)   // Rosa
                )
                colors[randomProvider.nextInt(0, colors.size)].value
            }
            ParticleType.DEATH -> {
                // Red/orange particles for player death - cartoon blood style
                val colors = listOf(
                    Color(0x50FF0000),  // Rojo puro brillante - ultra transparente
                    Color(0x60FF1744),  // Rojo carmesí vibrante - muy transparente
                    Color(0x55FF4444),  // Rojo coral brillante - muy transparente
                    Color(0x58FF0033),  // Rojo cereza - muy transparente
                    Color(0x48FF5252)   // Rojo-rosa brillante - máxima transparencia
                )
                colors[randomProvider.nextInt(0, colors.size)].value
            }
            ParticleType.BARREL_DESTROYED -> {
                // Brown/barrel colors for barrel destruction
                val barrelColors = listOf(
                    Color(0x805D4037),  // Marrón principal del barril - transparente
                    Color(0x753E2723),  // Marrón oscuro del barril - muy transparente
                    Color(0x85795548),  // Marrón claro highlight - transparente
                    Color(0x7A6D4C41),  // Marrón medio - muy transparente
                    Color(0x888D6E63)   // Marrón claro - transparente
                )
                barrelColors[randomProvider.nextInt(0, barrelColors.size)].value
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
