package pt.ulusofona.deisi.cm2223.g21702361

import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.lang.ref.WeakReference

class MovieSearchAndSaveManager(
    private val referenceMainActivity: WeakReference<MainActivity>,
    private val db: AppDatabase
) {

    private val client = OkHttpClient()

    suspend fun searchAndSaveMovie(userQuery: String): String? {
        Log.d("MovieSearch", "searchAndSaveMovie called with query: $userQuery")
        //replaces each letter of the every word in the movie to make the offline search work, ex Lawless works but lawless would not without this
        val formattedQuery = userQuery.split(" ").joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
        val apiKey = "187966"
        val apiUrl = "https://www.omdbapi.com/?t=$formattedQuery&apiKey=$apiKey"

        // Check if the movie title already exists in the database
        val checkMovieInBd = db.movieDao().getMovieByTitle(formattedQuery)

        if (checkMovieInBd != null) {
            // Movie already exists in the database
            val context = referenceMainActivity.get()
            context?.runOnUiThread {
                Toast.makeText(context, "Movie already in the database!", Toast.LENGTH_SHORT).show()
            }
            Log.d("MovieSearch", "Movie $formattedQuery found in the database")
            return checkMovieInBd.imdbId // return the IMDb ID of the found movie
        }

        // If movie is not found in the database, proceed to search in the API
        val requestAPI = Request.Builder()
            .url(apiUrl)
            .build()

        try {
            val apiResponse = withContext(Dispatchers.IO) {
                client.newCall(requestAPI).execute()
            }

            if (apiResponse.isSuccessful) {
                val jsonParseResponse = JSONObject(apiResponse.body?.string() ?: "")
                if (jsonParseResponse.optString("Response", "True") == "False") {
                    val context = referenceMainActivity.get()
                    context?.runOnUiThread {
                        Toast.makeText(context, "Movie not found in the API!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("MovieSearch", "Movie $formattedQuery not found in the API")
                } else {
                    val title = jsonParseResponse.optString("Title", "N/A")
                    val imdbId = jsonParseResponse.optString("imdbID", "N/A")
                    val released = jsonParseResponse.optString("Released", "N/A")
                    val plot = jsonParseResponse.optString("Plot", "N/A")
                    val poster = jsonParseResponse.optString("Poster", "N/A")
                    val imdbRating = jsonParseResponse.optString("imdbRating", "N/A")
                    val genre = jsonParseResponse.optString("Genre", "N/A")
                    val imdbTotalVotes = jsonParseResponse.optString("imdbVotes", "N/A")

                    // remove comma from genre if it exists, UI only displays the first genre
                    val genreFormatted = genre.substringBefore(",")

                    if (title != "N/A") {
                        val movie = Movie(imdbId, title, released, plot, poster, imdbRating, genreFormatted, imdbTotalVotes, 0)
                        saveMovieToDb(movie)
                        return imdbId // return the IMDb ID of the saved movie
                    }
                }
            } else {
                Log.d("MovieSearch", "API response unsuccessful for query: $formattedQuery")
            }
        } catch (e: IOException) {
            val context = referenceMainActivity.get()
            context?.runOnUiThread {
                Toast.makeText(context, "Error fetching data for $formattedQuery", Toast.LENGTH_SHORT).show()
            }
            Log.e("MovieSearch", "IOException while searching for $formattedQuery", e)
        }
        return null
    }

    private suspend fun saveMovieToDb(movie: Movie) {
        db.movieDao().insertAll(movie)
        Log.d("MovieSearch", "Movie saved to database: ${movie.title}")
    }
}
