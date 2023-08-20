package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object CinemaJSON {

    suspend fun readCinemasFromAssets(context: Context): List<Cinema> = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open("cinemas.json").bufferedReader().use { it.readText() }

        // Parse the string into a JSONObject first
        val jsonObject = JSONObject(jsonString)

        // Then get the "cinemas" JSONArray from the JSONObject
        val cinemasArray = jsonObject.getJSONArray("cinemas")

        List(cinemasArray.length()) { index ->
            with(cinemasArray.getJSONObject(index)) {
                Cinema(
                    getString("cinema_id"),
                    getString("cinema_name"),
                    getDouble("latitude"),
                    getDouble("longitude"),
                    getString("address"),
                    getString("county")
                )
            }
        }
    }
}
