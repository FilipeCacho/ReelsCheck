package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var recyclerView3: RecyclerView
    private lateinit var recyclerView4: RecyclerView
    private lateinit var recyclerView5: RecyclerView
    private lateinit var recyclerView6: RecyclerView
    private lateinit var recyclerView7: RecyclerView
    private lateinit var recyclerView8: RecyclerView
    private lateinit var movieAdapter1: MovieAdapter
    private lateinit var movieAdapter2: MovieAdapter
    private lateinit var movieAdapter3: MovieAdapter
    private lateinit var movieAdapter4: MovieAdapter
    private lateinit var movieAdapter5: MovieAdapter
    private lateinit var movieAdapter6: MovieAdapter
    private lateinit var movieAdapter7: MovieAdapter
    private lateinit var movieAdapter8: MovieAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView1 = findViewById(R.id.recyclerView1)
        recyclerView2 = findViewById(R.id.recyclerView2)
        recyclerView3 = findViewById(R.id.recyclerView3)
        recyclerView4 = findViewById(R.id.recyclerView4)
        recyclerView5 = findViewById(R.id.recyclerView5)
        recyclerView6 = findViewById(R.id.recyclerView6)
        recyclerView7 = findViewById(R.id.recyclerView7)
        recyclerView8 = findViewById(R.id.recyclerView8)

        setupRecyclerView(recyclerView1)
        setupRecyclerView(recyclerView2)
        setupRecyclerView(recyclerView3)
        setupRecyclerView(recyclerView4)
        setupRecyclerView(recyclerView5)
        setupRecyclerView(recyclerView6)
        setupRecyclerView(recyclerView7)
        setupRecyclerView(recyclerView8)

        db = AppDatabase.getDatabase(applicationContext)

        movieAdapter1 = MovieAdapter(mutableListOf())
        movieAdapter2 = MovieAdapter(mutableListOf())
        movieAdapter3 = MovieAdapter(mutableListOf())
        movieAdapter4 = MovieAdapter(mutableListOf())
        movieAdapter5 = MovieAdapter(mutableListOf())
        movieAdapter6 = MovieAdapter(mutableListOf())
        movieAdapter7 = MovieAdapter(mutableListOf())
        movieAdapter8 = MovieAdapter(mutableListOf())

        recyclerView1.adapter = movieAdapter1
        recyclerView2.adapter = movieAdapter2
        recyclerView3.adapter = movieAdapter3
        recyclerView4.adapter = movieAdapter4
        recyclerView5.adapter = movieAdapter5
        recyclerView6.adapter = movieAdapter6
        recyclerView7.adapter = movieAdapter7
        recyclerView8.adapter = movieAdapter8

        fetchAllMovies()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun fetchAllMovies() {
        val movieUrls1 = MovieUrlDataSource.getMovieUrlsForRecyclerView1()
        val movieUrls2 = MovieUrlDataSource.getMovieUrlsForRecyclerView2()
        val movieUrls3 = MovieUrlDataSource.getMovieUrlsForRecyclerView3()
        val movieUrls4 = MovieUrlDataSource.getMovieUrlsForRecyclerView4()
        val movieUrls5 = MovieUrlDataSource.getMovieUrlsForRecyclerView5()
        val movieUrls6 = MovieUrlDataSource.getMovieUrlsForRecyclerView6()
        val movieUrls7 = MovieUrlDataSource.getMovieUrlsForRecyclerView7()
        val movieUrls8 = MovieUrlDataSource.getMovieUrlsForRecyclerView8()
        // ... Do this for all 8 adapters ...

        fetchMoviesForRecyclerView(movieUrls1, movieAdapter1)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter2)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter2)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter3)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter4)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter5)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter6)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter7)
        fetchMoviesForRecyclerView(movieUrls2, movieAdapter8)
        // ... Continue for all adapters ...
    }

    private fun fetchMoviesForRecyclerView(urls: List<String>, adapter: MovieAdapter) {
        var moviesFetched = 0
        val moviesList = mutableListOf<Movie>()

        urls.forEach { url ->
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error fetching data for $url", Toast.LENGTH_SHORT).show()
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

    fun onIconClicked(view: View) {
        // Handle the click event here
        when (view.id) {
            R.id.action -> {
                // Replace the toast with the specific action you want to perform for 'action'
                Toast.makeText(this, "Action icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.history -> {
                // Replace the toast with the specific action you want to perform for 'history'
                Toast.makeText(this, "History icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.western -> {
                // Replace the toast with the specific action you want to perform for 'western'
                Toast.makeText(this, "Western icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.sports -> {
                // Replace the toast with the specific action you want to perform for 'sports'
                Toast.makeText(this, "Sports icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.love -> {
                // Replace the toast with the specific action you want to perform for 'love'
                Toast.makeText(this, "Love icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.murder -> {
                // Replace the toast with the specific action you want to perform for 'murder'
                Toast.makeText(this, "Murder icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.horror -> {
                // Replace the toast with the specific action you want to perform for 'horror'
                Toast.makeText(this, "Horror icon clicked!", Toast.LENGTH_SHORT).show()
            }

            R.id.war -> {
                // Replace the toast with the specific action you want to perform for 'war'
                Toast.makeText(this, "War icon clicked!", Toast.LENGTH_SHORT).show()
            }


        }



    }

    fun onAddClicked(view: View?) {
        Toast.makeText(this, "Add icon clicked", Toast.LENGTH_SHORT).show()
        // Handle the click for the "Add" icon in the bottom bar
    }

    fun onEditClicked(view: View?) {
        Toast.makeText(this, "Edit icon clicked", Toast.LENGTH_SHORT).show()
        // Handle the click for the "Edit" icon in the bottom bar
    }

    fun onDeleteClicked(view: View?) {
        Toast.makeText(this, "Delete icon clicked", Toast.LENGTH_SHORT).show()
        // Handle the click for the "Delete" icon in the bottom bar
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()  // cancels all coroutines under this scope
    }

}
