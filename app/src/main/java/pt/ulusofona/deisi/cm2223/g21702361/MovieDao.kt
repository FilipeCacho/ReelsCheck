package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pt.ulusofona.deisi.cm2223.g21702361.Movie


@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg movies: Movie)

    @Query("SELECT * FROM movies WHERE recyclerViewId = :recyclerViewId")
    fun getMoviesForRecyclerView(recyclerViewId: Int): List<Movie>

    @Query("SELECT COUNT(*) FROM movies WHERE recyclerViewId = :id")
    suspend fun getCountForRecyclerViewId(id: Int): Int

    @Query("DELETE FROM movies WHERE recyclerViewId = :id")
    suspend fun deleteMoviesForRecyclerViewId(id: Int)

    @Query("DELETE FROM movies WHERE recyclerViewId = :recyclerViewId")
    fun deleteByRecyclerViewId(recyclerViewId: Int)

    @Query("SELECT * FROM movies WHERE recyclerViewId = :recyclerViewId")
    fun getMoviesByRecyclerViewId(recyclerViewId: Int): List<Movie>

    @Query("SELECT * FROM movies WHERE imdbID = :imdbId")
    suspend fun getMovieByImdbId(imdbId: String): Movie?

}

