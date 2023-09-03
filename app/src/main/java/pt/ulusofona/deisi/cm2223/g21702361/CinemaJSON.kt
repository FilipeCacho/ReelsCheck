package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object CinemaJSON {
    suspend fun readCinemasFromAssets(context: Context, cinemaDao: CinemaDao): List<Cinema> = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open("cinemas.json").bufferedReader().use { it.readText() }

        val cinemasList = mutableListOf<Cinema>()

        // Parse the string into a JSONObject first
        val jsonObject = JSONObject(jsonString)

        // get the "cinemas" JSONArray from the JSONObject
        val cinemasArray = jsonObject.getJSONArray("cinemas")

        for (index in 0 until cinemasArray.length()) {
            val cinemaObject = cinemasArray.getJSONObject(index)
            val cinema = Cinema(
                cinemaObject.getString("cinema_id"),
                cinemaObject.getString("cinema_name"),
                cinemaObject.getDouble("latitude"),
                cinemaObject.getDouble("longitude"),
                cinemaObject.getString("address"),
                cinemaObject.getString("county")
            )

            // Insert the cinema into the database
            cinemaDao.insertCinema(cinema)
            cinemasList.add(cinema)
        }
        cinemasList.toList()
    }
}
