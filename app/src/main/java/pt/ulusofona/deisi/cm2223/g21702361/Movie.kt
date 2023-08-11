package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movie(
    @PrimaryKey val imdbID: String,
    val title: String
    // ... add other fields as necessary (year, director, etc.).
)
