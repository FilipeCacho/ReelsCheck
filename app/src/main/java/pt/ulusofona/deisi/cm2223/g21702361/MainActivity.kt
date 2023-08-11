package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import okhttp3.Call
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val movieTitles = listOf("Inception", "Interstellar", "The Matrix") // Add more titles as needed

        movieTitles.forEach { title ->
            val request = Request.Builder()
                //.url("https://www.omdbapi.com/?t=${title}&apiKey=187966")
                .url("https://www.omdbapi.com/?apikey=187966&t=${title}")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // For simplicity, let's just show a Toast.
                    // Remember Toast should be shown on the main thread.
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error fetching data for $title", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val jsonResponse = JSONObject(response.body?.string() ?: "")
                        // Extract details from jsonResponse
                        val title = jsonResponse.optString("Title", "N/A")
                        val year = jsonResponse.optString("Year", "N/A")
                        val imdbID = jsonResponse.optString("imdbID", "N/A")
                        response.body?.close()

                        // ... extract more fields as required

                        // Construct a Movie object
                        val movie = Movie(imdbID, title) // This assumes you have such a constructor in your Movie data class.

                        // Save the movie data to Room
                        val db = AppDatabase.getDatabase(applicationContext)
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                db.movieDao().insertAll(movie)
                            } catch (e: Exception) {
                                // Handle exception, maybe log it or show a message to the user.
                            }
                        }

                    }
                }

            })

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


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()  // cancels all coroutines under this scope
    }

}