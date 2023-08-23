package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cinema::class, Movie::class, UserMovieDetails::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun userMovieDetailsDao(): UserMovieDetailsDao

    abstract fun cinemaDao(): CinemaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(LOCK) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movies"
                )
                    .fallbackToDestructiveMigration() // Handling migration by destroying and recreating the DB
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
