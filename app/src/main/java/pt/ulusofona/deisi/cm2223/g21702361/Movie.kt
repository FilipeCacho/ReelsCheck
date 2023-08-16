package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val imdbID: String,
    val title: String,
    val released: String,
    val plot: String,
    val poster: String,
    val imdbrating: String,
    val recyclerViewId: Int // Add this property
)
