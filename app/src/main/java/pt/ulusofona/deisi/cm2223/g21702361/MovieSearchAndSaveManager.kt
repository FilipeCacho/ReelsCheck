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
    private val contextReference: WeakReference<MainActivity>,
    private val db: AppDatabase
) {

    private val client = OkHttpClient()

    suspend fun searchAndSaveMovie(query: String): String? {
        Log.d("MovieSearch", "searchAndSaveMovie called with query: $query")
        val formattedQuery = query.replace(" ", "+")
        val apiKey = "187966"
        val apiUrl = "https://www.omdbapi.com/?t=$formattedQuery&apiKey=$apiKey"

        // Check if the movie title already exists in the database
        val existingMovie = db.movieDao().getMovieByTitle(query)

        if (existingMovie != null) {
            // Movie already exists in the database
            val context = contextReference.get()
            context?.runOnUiThread {
                Toast.makeText(context, "Movie already in the database!", Toast.LENGTH_SHORT).show()
            }
            Log.d("MovieSearch", "Movie $query found in the database")
            return existingMovie.imdbID // return the IMDb ID of the found movie
        }

        // If movie is not found in the database, proceed to search in the API
        val request = Request.Builder()
            .url(apiUrl)
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                if (jsonResponse.optString("Response", "True") == "False") {
                    val context = contextReference.get()
                    context?.runOnUiThread {
                        Toast.makeText(context, "Movie not found in the API!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("MovieSearch", "Movie $query not found in the API")
                } else {
                    val title = jsonResponse.optString("Title", "N/A")
                    val imdbID = jsonResponse.optString("imdbID", "N/A")
                    val released = jsonResponse.optString("Released", "N/A")
                    val plot = jsonResponse.optString("Plot", "N/A")
                    val poster = jsonResponse.optString("Poster", "N/A")
                    val imdbRating = jsonResponse.optString("imdbRating", "N/A")
                    val genre = jsonResponse.optString("Genre", "N/A")
                    val imdbTotalVotes = jsonResponse.optString("imdbVotes", "N/A")

                    // remove comma from genre if it exists
                    val genreFormatted = genre.substringBefore(",")

                    if (title != "N/A") {
                        val movie = Movie(imdbID, title, released, plot, poster, imdbRating, genreFormatted, imdbTotalVotes, 0)
                        saveMovieToDb(movie)
                        return imdbID // return the IMDb ID of the saved movie
                    }
                }
            } else {
                Log.d("MovieSearch", "API response unsuccessful for query: $query")
            }
        } catch (e: IOException) {
            val context = contextReference.get()
            context?.runOnUiThread {
                Toast.makeText(context, "Error fetching data for $query", Toast.LENGTH_SHORT).show()
            }
            Log.e("MovieSearch", "IOException while searching for $query", e)
        }
        return null
    }

    private suspend fun saveMovieToDb(movie: Movie) {
        db.movieDao().insertAll(movie)
        Log.d("MovieSearch", "Movie saved to database: ${movie.title}")
    }
}
