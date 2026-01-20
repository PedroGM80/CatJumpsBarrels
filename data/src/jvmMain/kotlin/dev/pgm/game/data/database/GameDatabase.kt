package dev.pgm.game.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.pgm.game.data.database.dao.HighScoreDao
import dev.pgm.game.data.database.entity.HighScoreEntity
import java.io.File

/**
 * Base de datos Room para Cat Jump Barrels.
 * Almacena los high scores del juego.
 */
@Database(
    entities = [HighScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO de high scores.
     */
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        private const val DATABASE_NAME = "cat_jump_barrels.db"

        /**
         * Crea una instancia de la base de datos.
         * La base de datos se guarda en: ~/.catjumpbarrels/cat_jump_barrels.db
         */
        fun create(): GameDatabase {
            val dbFile = File(System.getProperty("user.home"))
                .resolve(".catjumpbarrels")
                .resolve(DATABASE_NAME)

            // Crear directorio si no existe
            dbFile.parentFile?.mkdirs()

            return Room.databaseBuilder<GameDatabase>(
                name = dbFile.absolutePath
            ).build()
        }
    }
}
