package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import androidx.lifecycle.lifecycleScope
import java.lang.ref.WeakReference
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2223.g21702361.databinding.ActivityMainBinding // Import the generated binding class

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding  // Declare the binding variable
    val job = Job()
    private lateinit var movieRecyclerManager: MainMovieRecyclerManager
    private lateinit var db: AppDatabase
    private lateinit var recyclerViewSetupManager: MainRecyclerViewSetupManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        db = AppDatabase.getDatabase(applicationContext)
        movieRecyclerManager = MainMovieRecyclerManager(WeakReference(this), db)
        recyclerViewSetupManager = MainRecyclerViewSetupManager(this)

        val recyclerViews = recyclerViewSetupManager.getRecyclerViews()
        val movieAdapters = recyclerViewSetupManager.getMovieAdapters()

        recyclerViews.forEachIndexed { index, recyclerView ->
            recyclerView.adapter = movieAdapters[index]
        }

        lifecycleScope.launch {
            val moviesFromDb = db.movieDao().getAllMovies()

            // Log the total number of movies in the database
            val totalMoviesInDb = moviesFromDb.size
            Log.d("Total Movies in DB", "Total movies: $totalMoviesInDb")

            // Distribute movies to the correct adapters based on recyclerViewId
            movieAdapters.forEachIndexed { index, adapter ->
                val recyclerViewId = index + 1
                val moviesForAdapter = moviesFromDb.filter { it.recyclerViewId == recyclerViewId }
                adapter.addMovies(moviesForAdapter)
            }

            // Continue with other setup
            movieRecyclerManager.setupAllRecyclerViews(*recyclerViews.toTypedArray())
            movieRecyclerManager.fetchAllMoviesForRecyclerViews(recyclerViewSetupManager.getUrlsList(), *movieAdapters.toTypedArray())
        }
    }

    fun onIconClicked(view: View) {
        val recyclerViews = recyclerViewSetupManager.getRecyclerViews()
        when (view.id) {
            R.id.action -> scrollToPosition(recyclerViews[0])
            R.id.history -> scrollToPosition(recyclerViews[1])
            R.id.western -> scrollToPosition(recyclerViews[2])
            R.id.sports -> scrollToPosition(recyclerViews[3])
            R.id.love -> scrollToPosition(recyclerViews[4])
            R.id.murder -> scrollToPosition(recyclerViews[5])
            R.id.horror -> scrollToPosition(recyclerViews[6])
            R.id.war -> scrollToPosition(recyclerViews[7])
            else -> {
                // This will execute for any other case, if there's any.
                Toast.makeText(this, "Unknown icon clicked!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Replace mainScrollView with binding.mainScrollView

    private fun scrollToPosition(recyclerView: RecyclerView) {
        binding.mainScrollView.post {
            val scrollTo = recyclerView.top
            binding.mainScrollView.smoothScrollTo(0, scrollTo)
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
        movieRecyclerManager.cancelAllActiveCalls()
        job.cancel()  // cancels all coroutines under this scope
    }

}