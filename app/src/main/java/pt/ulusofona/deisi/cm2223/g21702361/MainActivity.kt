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

        private lateinit var recyclerView: RecyclerView
        private lateinit var movieAdapter: MovieAdapter
        private lateinit var db: AppDatabase

        private val movieUrls = listOf(
            "https://www.omdbapi.com/?t=Spider-Man%3A+Across+the+Spider-Verse&apiKey=187966",
            "https://www.omdbapi.com/?t=Transformers%3A+Rise+of+the+Beasts&apiKey=187966",
            "https://www.omdbapi.com/?t=harry+potter+and+the+deathly+hallows%3A+part+2&apiKey=187966",
            "https://www.omdbapi.com/?t=The+Flash&apiKey=187966",
            "https://www.omdbapi.com/?t=Meg+2%3A+The+Trench&apiKey=187966",
            "https://www.omdbapi.com/?t=Fast+X&apiKey=187966",
            "https://www.omdbapi.com/?t=Resident+Evil%3A+Death+Island+&apiKey=187966",
            "https://www.omdbapi.com/?t=Guardians+of+the+Galaxy+Vol.+3+&apiKey=187966",
            "https://www.omdbapi.com/?t=Miraculous%3A+Ladybug+%26+Cat+Noir&apiKey=187966",
            "https://www.omdbapi.com/?t=John+Wick%3A+Chapter+4&apiKey=187966",
            "https://www.omdbapi.com/?t=Warhorse+One&apiKey=187966",
            "https://www.omdbapi.com/?t=Knights+of+the+Zodiac&apiKey=187966",
            "https://www.omdbapi.com/?t=Mission%3A+Impossible+-+Dead+Reckoning+Part+One&apiKey=187966",
            "https://www.omdbapi.com/?t=Sound+of+Freedom&apiKey=187966",
            "https://www.omdbapi.com/?t=Puss+in+Boots%3A+The+Last+Wish&apiKey=187966",
            "https://www.omdbapi.com/?t=Avatar%3A+The+Way+of+Water&apiKey=187966",
            "https://www.omdbapi.com/?t=San+Andreas&apiKey=187966",
            "https://www.omdbapi.com/?t=The+Darkest+Minds&apiKey=187966",
            "https://www.omdbapi.com/?t=Indiana+Jones+and+the+Dial+of+Destiny&apiKey=187966",
            "https://www.omdbapi.com/?t=Shazam!+Fury+of+the+Gods&apiKey=187966"
        )

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            recyclerView = findViewById(R.id.recyclerView1)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


            db = AppDatabase.getDatabase(applicationContext)

            movieAdapter = MovieAdapter(mutableListOf())
            recyclerView.adapter = movieAdapter

            movieUrls.forEach { url ->
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

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    db.movieDao().insertAll(movie)
                                    withContext(Dispatchers.Main) {
                                        movieAdapter.addMovie(movie)
                                    }
                                } catch (e: Exception) {
                                    // Handle exception.
                                }
                            }
                        }
                    }
                })
            }
        }

        fun onIconClicked(view: View) {
            when (view.id) {
                R.id.action -> Toast.makeText(this, "Action icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.history -> Toast.makeText(this, "History icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.western -> Toast.makeText(this, "Western icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.sports -> Toast.makeText(this, "Sports icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.love -> Toast.makeText(this, "Love icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.murder -> Toast.makeText(this, "Murder icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.horror -> Toast.makeText(this, "Horror icon clicked!", Toast.LENGTH_SHORT).show()
                R.id.war -> Toast.makeText(this, "War icon clicked!", Toast.LENGTH_SHORT).show()
                //... add other cases if needed ...
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            job.cancel()
        }
    }
