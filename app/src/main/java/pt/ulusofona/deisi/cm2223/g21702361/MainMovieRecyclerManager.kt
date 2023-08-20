package pt.ulusofona.deisi.cm2223.g21702361

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
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
            val recyclerViewId = index + 1
            fetchMoviesForRecyclerView(urls, adapters[index], recyclerViewId)
        }
    }



    private fun fetchMoviesForRecyclerView(urls: List<String>, adapter: MovieAdapter, recyclerViewId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val existingMovies = db.movieDao().getMoviesByRecyclerViewId(recyclerViewId)
            if (existingMovies.isNotEmpty()) {
                // If movies for this recyclerViewId already exist in the database, use them instead of fetching online.
                withContext(Dispatchers.Main) {
                    adapter.addMovies(existingMovies)
                    adapter.notifyDataSetChanged()
                }
            } else {
                fetchOnlineAndInsert(urls, adapter, recyclerViewId)
            }
        }
    }

    private fun fetchOnlineAndInsert(urls: List<String>, adapter: MovieAdapter, recyclerViewId: Int) {
        var moviesFetched = 0
        val moviesList = mutableListOf<Movie>()

        urls.forEach { url ->
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val context = contextReference.get()


                    if (isNetworkAvailable(context)) {
                        context?.runOnUiThread {
                            Toast.makeText(
                                context,
                                "Error fetching data for $url",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                fun isNetworkAvailable(context: Context?): Boolean {
                    val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val network = connectivityManager?.activeNetwork ?: return false
                        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
                        return when {
                            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    } else {
                        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
                        return activeNetworkInfo != null && activeNetworkInfo.isConnected
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
                        val genre = jsonResponse.optString("Genre", "N/A")
                        val imdbTotalVotes = jsonResponse.optString("imdbVotes", "N/A")

                        //remove comma from genre if it exists
                        val genreFormatted = genre.substringBefore(",")


                        val movie = Movie(imdbID, title, released, plot, poster, imdbRating, genreFormatted, imdbTotalVotes, recyclerViewId)
                        moviesList.add(movie)

                        moviesFetched++
                        if (moviesFetched == urls.size) {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.movieDao().deleteByRecyclerViewId(recyclerViewId)
                                insertMoviesToDbAndNotifyAdapter(moviesList, adapter)
                            }
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
                    // Clear the data for this recyclerViewId before inserting the new data.
                    db.movieDao().deleteByRecyclerViewId(movies[0].recyclerViewId) // assuming all movies have the same recyclerViewId
                    db.movieDao().insertAll(*movies.toTypedArray())

                    withContext(Dispatchers.Main) {
                        adapter.addMovies(movies)
                        adapter.notifyDataSetChanged() // Notify the adapter of changes
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