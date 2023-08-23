package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val imdbId: String, // Note the change here
    val title: String,
    val released: String,
    val plot: String,
    val poster: String,
    val imdbrating: String,
    val genre: String,
    val imdbTotalVotes: String,
    val recyclerViewId: Int
) : Parcelable
