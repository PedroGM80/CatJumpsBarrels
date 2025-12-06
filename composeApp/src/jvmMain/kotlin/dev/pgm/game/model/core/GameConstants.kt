package dev.pgm.game.model.core

/**
 * Constantes globales del juego Cat Jump Barrels.
 *
 * Este objeto centraliza todos los valores configurables del juego,
 * organizados por categorías para facilitar el ajuste del balance y comportamiento.
 */
object GameConstants {

    // ========== Geometría del Nivel ==========

    /** Ancho del área de juego en píxeles */
    const val LEVEL_WIDTH = 600f

    /** Altura de cada plataforma */
    const val PLATFORM_HEIGHT = 8f

    /** Separación vertical entre plataformas */
    const val PLATFORM_GAP = 100f

    /** Inclinación de las plataformas (0.03 = 3% de pendiente, más sutil) */
    const val PLATFORM_SLOPE = 0.03f

    /** Número de plataformas principales en el nivel */
    const val PLATFORM_COUNT = 6

    /** Offset desde la parte inferior de la pantalla para la primera plataforma */
    const val PLATFORM_BOTTOM_OFFSET = 60f

    /** Offset horizontal para plataformas en posición impar */
    const val PLATFORM_START_OFFSET = 40f

    /** Escala de ancho de la plataforma de Donkey Kong (60% del ancho total) */
    const val DK_PLATFORM_WIDTH_SCALE = 0.6f

    /** Ancho de la plataforma de la princesa */
    const val PRINCESS_PLATFORM_WIDTH = 120f

    /** Offset horizontal de la plataforma de la princesa desde el borde derecho de DK */
    const val PRINCESS_PLATFORM_OFFSET_X = 150f

    /** Offset vertical de la plataforma de la princesa desde la de DK */
    const val PRINCESS_PLATFORM_OFFSET_Y = 40f

    /** Distancia del borde de la plataforma donde se colocan las escaleras */
    const val LADDER_OFFSET_FROM_EDGE = 50f

    /** Offset de la escalera desde el borde izquierdo de la plataforma de la princesa */
    const val LADDER_OFFSET_FROM_PRINCESS = 20f

    /** Posición X inicial del jugador */
    const val PLAYER_START_X = 50f

    /** Posición X inicial de Donkey Kong */
    const val DK_START_X = 20f

    /** Offset X de la princesa dentro de su plataforma */
    const val PRINCESS_OFFSET_X = 40f

    // ========== Física y Colisiones ==========

    /** Aceleración de gravedad aplicada cada frame */
    const val GRAVITY = 0.45f

    /** Fuerza inicial del salto (negativa porque Y crece hacia abajo)
     * En Donkey Kong original el salto es bajo, solo para saltar barriles */
    const val JUMP_STRENGTH = -7.5f

    /** Velocidad de movimiento horizontal del jugador */
    const val MOVE_SPEED = 3.0f

    /** Velocidad de escalada en escaleras */
    const val CLIMB_SPEED = 2.5f

    /** Tolerancia para detectar colisión con plataformas */
    const val PLATFORM_COLLISION_TOLERANCE = 10f

    /** Tolerancia horizontal para agarrarse a una escalera */
    const val LADDER_HORIZONTAL_TOLERANCE = 15f

    /** Tolerancia superior para detectar saltos sobre barriles */
    const val BARREL_JUMP_TOLERANCE_TOP = 15f

    /** Tolerancia inferior para detectar saltos sobre barriles */
    const val BARREL_JUMP_TOLERANCE_BOTTOM = 8f

    // ========== Comportamiento de Barriles ==========

    /** Intervalo de tiempo entre spawns de barriles (en milisegundos) */
    const val BARREL_SPAWN_INTERVAL = 2500L

    /** Velocidad horizontal base de los barriles */
    const val BARREL_SPEED = 2.5f

    /** Velocidad de rotación visual de los barriles */
    const val BARREL_ROLL_SPEED = 0.12f

    /** Velocidad de caída de los barriles en escaleras */
    const val BARREL_LADDER_FALL_SPEED = 3.0f

    /** Velocidad de caída libre (cuando cae del borde) */
    const val BARREL_FREE_FALL_SPEED = 4.5f

    /** Offset desde el borde de la plataforma donde caen los barriles */
    const val BARREL_PLATFORM_EDGE_OFFSET = 10f

    /** Offset para buscar escaleras cerca del borde de plataformas */
    const val BARREL_LADDER_CHECK_OFFSET = 30f

    /** Tolerancia para detectar escaleras en los bordes */
    const val BARREL_LADDER_CHECK_TOLERANCE = 50f

    /** Offset adicional fuera de pantalla antes de eliminar barriles */
    const val BARREL_SCREEN_CLEANUP_OFFSET = 50f

    /** Probabilidad de que un barril baje por una escalera (0.0 a 1.0) */
    const val BARREL_LADDER_PROBABILITY = 0.35f

    /** Tolerancia horizontal para detectar si el barril está sobre una escalera */
    const val BARREL_OVER_LADDER_TOLERANCE = 20f

    /** Factor de aceleración por pendiente */
    const val BARREL_SLOPE_ACCELERATION = 0.8f

    // ========== Sistema de Puntuación ==========

    /** Puntos otorgados por saltar sobre un barril */
    const val POINTS_JUMP_BARREL = 100

    /** Puntos bonus por completar el nivel */
    const val POINTS_WIN = 1000

    // ========== Animación y Efectos ==========

    /** Duración de cada frame de animación en segundos */
    const val ANIMATION_FRAME_DURATION = 0.1f

    /** Número total de frames en la animación del jugador */
    const val PLAYER_ANIMATION_FRAMES = 4

    /** Intervalo entre frames de animación de Donkey Kong (en milisegundos) */
    const val ENEMY_ANIMATION_INTERVAL_MS = 400L

    /** Tiempo de invencibilidad tras morir (en milisegundos) */
    const val INVINCIBILITY_TIME = 2000L

    /** Intervalo de parpadeo durante invencibilidad (en milisegundos) */
    const val INVINCIBILITY_BLINK_INTERVAL = 100L

    /** Tiempo de vida del popup de puntuación (en milisegundos) */
    const val SCORE_POPUP_LIFETIME_MS = 1000f

    /** Velocidad de subida del popup de puntuación */
    const val SCORE_POPUP_Y_SPEED = 0.05f

    // ========== Tamaños de Sprites ==========

    /** Tamaño del sprite del jugador */
    const val PLAYER_SIZE = 30f

    /** Tamaño del sprite de Donkey Kong */
    const val ENEMY_SIZE = 60f

    /** Tamaño del sprite de la princesa */
    const val PRINCESS_SIZE = 28f

    /** Tamaño del sprite de los barriles */
    const val BARREL_SIZE = 22f

    // ========== HUD e Interfaz ==========

    /** Padding del HUD desde los bordes de la pantalla */
    const val HUD_PADDING = 16f

    /** Tamaño de fuente para títulos del HUD (SCORE, HIGH) */
    const val HUD_SCORE_TITLE_SIZE = 12f

    /** Tamaño de fuente para el valor del score actual */
    const val HUD_SCORE_VALUE_SIZE = 24f

    /** Tamaño de fuente para el valor del high score */
    const val HUD_HIGH_SCORE_VALUE_SIZE = 18f

    /** Tamaño de fuente para el texto del nivel */
    const val HUD_LEVEL_TEXT_SIZE = 16f

    /** Tamaño de fuente para los corazones de vida */
    const val HUD_LIVES_TEXT_SIZE = 24f

    /** Altura del espaciador en el HUD */
    const val HUD_SPACER_HEIGHT = 8f

    /** Tamaño del título "GAME OVER" */
    const val GAMEOVER_TITLE_SIZE = 56f

    /** Tamaño del score final en pantalla de game over */
    const val GAMEOVER_SCORE_SIZE = 28f

    /** Tamaño del texto de reiniciar en game over */
    const val GAMEOVER_RESTART_SIZE = 18f

    /** Tamaño del título "YOU WIN!" */
    const val WIN_TITLE_SIZE = 56f

    /** Tamaño del score en pantalla de victoria */
    const val WIN_SCORE_SIZE = 32f

    /** Tamaño del título "PAUSED" */
    const val PAUSE_TITLE_SIZE = 48f

    // ========== Game Loop ==========

    /** Framerate objetivo del juego */
    const val TARGET_FPS = 60

    /** Tiempo objetivo por frame en nanosegundos */
    const val TARGET_FRAME_TIME_NS = 1_000_000_000L / TARGET_FPS

    /** Divisor para convertir nanosegundos a milisegundos */
    const val NS_TO_MS_DIVISOR = 1_000_000L

    /** Divisor para convertir nanosegundos a segundos */
    const val NS_TO_SEC_DIVISOR = 1_000_000_000f

    /** Delta time máximo para evitar saltos grandes en físicas */
    const val MAX_DELTA_TIME = 0.05f

    /** Delay mínimo entre frames (en milisegundos) */
    const val MIN_FRAME_DELAY_MS = 1L

    // ========== Proporciones de Dibujo (relativas al tamaño del sprite) ==========

    // Jugador
    /** Escala horizontal del cuerpo del jugador */
    const val PLAYER_BODY_H_SCALE = 0.65f

    /** Posición vertical del cuerpo del jugador */
    const val PLAYER_BODY_V_POS_SCALE = 0.35f

    /** Escala del radio de la cabeza del jugador */
    const val PLAYER_HEAD_RADIUS_SCALE = 0.28f

    /** Posición vertical de la cabeza del jugador */
    const val PLAYER_HEAD_V_POS_SCALE = 0.25f

    /** Posición vertical del cabello del jugador */
    const val PLAYER_HAIR_V_POS_SCALE = 0.15f

    /** Offset horizontal de los ojos según la dirección */
    const val PLAYER_EYE_OFFSET_X = 3f

    /** Escala de animación de las piernas */
    const val PLAYER_LEG_ANIM_SCALE = 1.5f

    // Donkey Kong
    /** Escala horizontal del cuerpo de DK */
    const val ENEMY_BODY_H_SCALE = 0.8f

    /** Posición vertical del cuerpo de DK */
    const val ENEMY_BODY_V_POS_SCALE = 0.3f

    /** Escala del pecho de DK */
    const val ENEMY_CHEST_H_SCALE = 0.5f

    /** Posición vertical del pecho de DK */
    const val ENEMY_CHEST_V_POS_SCALE = 0.45f

    /** Escala del radio de la cabeza de DK */
    const val ENEMY_HEAD_RADIUS_SCALE = 0.3f

    /** Posición vertical de la cabeza de DK */
    const val ENEMY_HEAD_V_POS_SCALE = 0.25f

    /** Escala horizontal de la cara de DK */
    const val ENEMY_FACE_H_SCALE = 0.4f

    /** Posición vertical de la cara de DK */
    const val ENEMY_FACE_V_POS_SCALE = 0.15f

    /** Offset de los brazos cuando DK lanza un barril */
    const val ENEMY_ARM_THROW_OFFSET = -10f

    // Princesa
    /** Posición vertical del vestido de la princesa */
    const val PRINCESS_DRESS_V_POS_SCALE = 0.35f

    /** Escala del radio de la cabeza de la princesa */
    const val PRINCESS_HEAD_RADIUS_SCALE = 0.25f

    /** Posición vertical de la cabeza de la princesa */
    const val PRINCESS_HEAD_V_POS_SCALE = 0.2f

    /** Escala horizontal del cabello de la princesa */
    const val PRINCESS_HAIR_H_SCALE = 0.7f

    /** Posición vertical del cabello de la princesa */
    const val PRINCESS_HAIR_V_POS_SCALE = 0.35f

    // ========== Configuración de Partículas ==========

    /** Gravedad aplicada a las partículas */
    const val PARTICLE_GRAVITY = 150f

    // Partículas de Spawn
    /** Cantidad de partículas al spawear un barril */
    const val SPAWN_PARTICLE_COUNT = 4

    /** Rango de velocidad X de partículas de spawn */
    const val SPAWN_PARTICLE_VEL_X_RANGE = 40f

    /** Rango de velocidad Y de partículas de spawn */
    const val SPAWN_PARTICLE_VEL_Y_RANGE = -30f

    /** Rango de tamaño de partículas de spawn */
    const val SPAWN_PARTICLE_SIZE_RANGE = 4f

    /** Tiempo de vida de partículas de spawn */
    const val SPAWN_PARTICLE_LIFETIME = 0.3f

    // Partículas de Puntuación
    /** Cantidad de partículas al obtener puntos */
    const val SCORE_PARTICLE_COUNT = 6

    /** Rango de velocidad X de partículas de puntuación */
    const val SCORE_PARTICLE_VEL_X_RANGE = 80f

    /** Rango de velocidad Y de partículas de puntuación */
    const val SCORE_PARTICLE_VEL_Y_RANGE = -80f

    /** Rango de tamaño de partículas de puntuación */
    const val SCORE_PARTICLE_SIZE_RANGE = 5f

    /** Tiempo de vida de partículas de puntuación */
    const val SCORE_PARTICLE_LIFETIME = 0.5f

    // Partículas de Muerte
    /** Cantidad de partículas al morir */
    const val DEATH_PARTICLE_COUNT = 12

    /** Rango de velocidad X de partículas de muerte */
    const val DEATH_PARTICLE_VEL_X_RANGE = 150f

    /** Rango de velocidad Y de partículas de muerte */
    const val DEATH_PARTICLE_VEL_Y_RANGE = -120f

    /** Rango de tamaño de partículas de muerte */
    const val DEATH_PARTICLE_SIZE_RANGE = 7f

    /** Tiempo de vida de partículas de muerte */
    const val DEATH_PARTICLE_LIFETIME = 0.7f

    // Partículas de Victoria
    /** Cantidad de partículas al ganar */
    const val WIN_PARTICLE_COUNT = 25

    /** Rango de velocidad X de partículas de victoria */
    const val WIN_PARTICLE_VEL_X_RANGE = 250f

    /** Rango de velocidad Y de partículas de victoria */
    const val WIN_PARTICLE_VEL_Y_RANGE = -180f

    /** Rango de tamaño de partículas de victoria */
    const val WIN_PARTICLE_SIZE_RANGE = 9f

    /** Tiempo de vida de partículas de victoria */
    const val WIN_PARTICLE_LIFETIME = 1.2f
}
