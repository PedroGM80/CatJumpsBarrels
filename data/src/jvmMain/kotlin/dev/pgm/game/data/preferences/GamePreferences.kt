package dev.pgm.game.data.preferences

import java.util.prefs.Preferences

/**
 * Manages game preferences persistently.
 * Uses Java Preferences API for configuration storage.
 */
object GamePreferences {
    private val prefs = Preferences.userRoot().node("dev/pgm/game/catjumpsbarrels")

    // Keys
    private const val KEY_SOUND_VOLUME = "sound_volume"
    private const val KEY_MUSIC_VOLUME = "music_volume"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_MUSIC_ENABLED = "music_enabled"

    // Default values
    private const val DEFAULT_SOUND_VOLUME = 70
    private const val DEFAULT_MUSIC_VOLUME = 50
    private const val DEFAULT_SOUND_ENABLED = true
    private const val DEFAULT_MUSIC_ENABLED = true

    /**
     * Sound effects volume (0-100)
     */
    var soundVolume: Int
        get() = prefs.getInt(KEY_SOUND_VOLUME, DEFAULT_SOUND_VOLUME)
        set(value) {
            prefs.putInt(KEY_SOUND_VOLUME, value.coerceIn(0, 100))
        }

    /**
     * Music volume (0-100)
     */
    var musicVolume: Int
        get() = prefs.getInt(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)
        set(value) {
            prefs.putInt(KEY_MUSIC_VOLUME, value.coerceIn(0, 100))
        }

    /**
     * Sound enabled
     */
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
        set(value) {
            prefs.putBoolean(KEY_SOUND_ENABLED, value)
        }

    /**
     * Music enabled
     */
    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, DEFAULT_MUSIC_ENABLED)
        set(value) {
            prefs.putBoolean(KEY_MUSIC_ENABLED, value)
        }

    /**
     * Normalizes volume from 0-100 to 0.0-1.0
     */
    fun getNormalizedSoundVolume(): Float = soundVolume / 100f

    /**
     * Normalizes volume from 0-100 to 0.0-1.0
     */
    fun getNormalizedMusicVolume(): Float = musicVolume / 100f

    /**
     * Resets to default values
     */
    fun resetToDefaults() {
        soundVolume = DEFAULT_SOUND_VOLUME
        musicVolume = DEFAULT_MUSIC_VOLUME
        soundEnabled = DEFAULT_SOUND_ENABLED
        musicEnabled = DEFAULT_MUSIC_ENABLED
    }
}
