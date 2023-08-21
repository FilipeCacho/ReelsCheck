package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query

@Dao
interface UserMovieDetailsDao {
    @Insert
    suspend fun insert(userMovieDetails: UserMovieDetails)

    @Update
    suspend fun update(userMovieDetails: UserMovieDetails)

    @Query("SELECT * FROM user_movie_details WHERE imdb_id = :imdbId")
    suspend fun getUserMovieDetails(imdbId: String): UserMovieDetails?

    @Query("SELECT * FROM user_movie_details")
    fun getAllUserMovieDetails(): List<UserMovieDetails>
}
