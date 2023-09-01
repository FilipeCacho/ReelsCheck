package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Room database class
@Database(entities = [Cinema::class, Movie::class, UserMovieDetails::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Define DAO
    abstract fun movieDao(): MovieDao
    abstract fun userMovieDetailsDao(): UserMovieDetailsDao
    abstract fun cinemaDao(): CinemaDao

    companion object {
        // Creates and locks db instance for sync
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()

        // singleton db instance fetch
        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(LOCK) {
                // Create a Room database instance with a given context, class, and name
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movies"
                )
                    // Last resort db strategy if migration happens is total destruction
                    .fallbackToDestructiveMigration()
                    .build()


                //Assigns and returns volatile db instance
                INSTANCE = instance
                return instance
            }
        }
    }
}
