package pt.ulusofona.deisi.cm2223.g21702361

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cinema")
data class Cinema(
    @PrimaryKey val cinema_id: String,
    val cinema_name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val county: String
)
