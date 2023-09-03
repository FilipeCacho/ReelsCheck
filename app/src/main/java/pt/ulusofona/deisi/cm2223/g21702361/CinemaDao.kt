package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface CinemaDao {
    @Query("SELECT * FROM cinema")
    fun getAllCinemas(): List<Cinema>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCinema(cinema: Cinema)

    @Query("SELECT COUNT(*) FROM cinema")
    suspend fun getCinemaCount(): Int
}
