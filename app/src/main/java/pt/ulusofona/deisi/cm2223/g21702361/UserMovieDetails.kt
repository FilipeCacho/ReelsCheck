package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "user_movie_details")
data class UserMovieDetails(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "imdb_id") val imdbId: String,
    @ColumnInfo(name = "user_rating") val userRating: Int,
    @ColumnInfo(name = "times_watched") val timesWatched: String,
    @ColumnInfo(name = "cinema_location") val cinemaLocation: String,
    @ColumnInfo(name = "watch_date") val watchDate: Long,
    @ColumnInfo(name = "comments") val comments: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "cinema_name") val cinemaName: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "county") val county: String

    // Add other user-specific details here
)