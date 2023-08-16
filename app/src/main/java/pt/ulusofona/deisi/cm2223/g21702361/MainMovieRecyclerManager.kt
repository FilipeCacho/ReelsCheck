package pt.ulusofona.deisi.cm2223.g21702361

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.lang.ref.WeakReference

class MainMovieRecyclerManager(
    private val contextReference: WeakReference<MainActivity>,
    private val db: AppDatabase
) {

    private val client = OkHttpClient()
    private val activeCalls = mutableListOf<Call>()

    fun setupAllRecyclerViews(vararg recyclerViews: RecyclerView) {
        recyclerViews.forEach { recyclerView ->
            val context = contextReference.get()
            context?.let {
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }

    fun fetchAllMoviesForRecyclerViews(urlsList: List<List<String>>, vararg adapters: MovieAdapter) {
        urlsList.forEachIndexed { index, urls ->
            fetchMoviesForRecyclerView(urls, adapters[index])
        }
    }

    private fun fetchMoviesForRecyclerView(urls: List<String>, adapter: MovieAdapter) {
        var moviesFetched = 0
        val moviesList = mutableListOf<Movie>()

        urls.forEach { url ->
            val request = Request.Builder().url(url).build()
            val call = client.newCall(request)
            activeCalls.add(call)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val context = contextReference.get()
                    context?.runOnUiThread {
                        Toast.makeText(context, "Error fetching data for $url", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val jsonResponse = JSONObject(response.body?.string() ?: "")

                        val title = jsonResponse.optString("Title", "N/A")
                        val imdbID = jsonResponse.optString("imdbID", "N/A")
                        val released = jsonResponse.optString("Released", "N/A")
                        val plot = jsonResponse.optString("Plot", "N/A")
                        val poster = jsonResponse.optString("Poster", "N/A")
                        val imdbRating = jsonResponse.optString("imdbRating", "N/A")

                        val movie = Movie(imdbID, title, released, plot, poster, imdbRating)
                        moviesList.add(movie)

                        moviesFetched++
                        if (moviesFetched == urls.size) {
                            insertMoviesToDbAndNotifyAdapter(moviesList, adapter)
                        }
                    }
                }
            })
        }
    }

    private fun insertMoviesToDbAndNotifyAdapter(movies: List<Movie>, adapter: MovieAdapter) {
        val context = contextReference.get()
        context?.let {
            val coroutineScope = CoroutineScope(Dispatchers.Main + it.job)
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    db.movieDao().insertAll(*movies.toTypedArray()) // insert all movies in the database
                    withContext(Dispatchers.Main) {
                        adapter.addMovies(movies) // update the adapter with all movies
                    }
                } catch (e: Exception) {
                    // Handle the exception.
                }
            }
        }
    }

    fun cancelAllActiveCalls() {
        for (call in activeCalls) {
            if (!call.isCanceled()) {
                call.cancel()
            }
        }
        activeCalls.clear()
    }
}