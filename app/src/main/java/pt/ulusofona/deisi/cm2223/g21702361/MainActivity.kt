package pt.ulusofona.deisi.cm2223.g21702361

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulusofona.deisi.cm2223.g21702361.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val job = Job()
    private lateinit var movieRecyclerManager: MainMovieRecyclerManager
    private lateinit var db: AppDatabase
    private lateinit var recyclerViewSetupManager: MainRecyclerViewSetupManager
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.statusBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Make the NavHostFragment visible
        navHostFragment.view?.isVisible = true

        db = AppDatabase.getDatabase(applicationContext)
        movieRecyclerManager = MainMovieRecyclerManager(WeakReference(this), db)
        recyclerViewSetupManager = MainRecyclerViewSetupManager(this) { movie ->
            onMovieClicked(movie)
        }

        val recyclerViews = recyclerViewSetupManager.getRecyclerViews()
        val movieAdapters = recyclerViewSetupManager.getMovieAdapters()

        recyclerViews.forEachIndexed { index, recyclerView ->
            recyclerView.adapter = movieAdapters[index]
        }

        lifecycleScope.launch {
            val moviesFromDb = db.movieDao().getAllMovies()
            val totalMoviesInDb = moviesFromDb.size
            movieAdapters.forEachIndexed { index, adapter ->
                val recyclerViewId = index + 1
                val moviesForAdapter = moviesFromDb.filter { it.recyclerViewId == recyclerViewId }
                adapter.addMovies(moviesForAdapter)
            }

            movieRecyclerManager.setupAllRecyclerViews(*recyclerViews.toTypedArray())
            movieRecyclerManager.fetchAllMoviesForRecyclerViews(recyclerViewSetupManager.getUrlsList(), *movieAdapters.toTypedArray())
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.popBackStack()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)




        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))


        //change search bar items to white, needs to be done with findviewbyid
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.WHITE) // Change to the desired text color
        searchText.setHintTextColor(Color.WHITE) // Change to the desired hint text color
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(Color.WHITE) // Change to the desired close button color
        val searchButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchButton.setColorFilter(Color.WHITE) // Change to the desired search icon color
        val searchPlateView = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlateView.background = ContextCompat.getDrawable(this, R.drawable.search_underline_text_background)
        val searchButtonView = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchButtonView?.setColorFilter(Color.WHITE) // Replace with your desired color



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("SearchDebug", "onQueryTextSubmit called with query: $query")


                searchView.clearFocus()
                closeKeyboard()
                searchView.onActionViewCollapsed()


                handleSearchQuery(query)


                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Implement real-time filtering here if desired
                return false
            }
        })

        return true
    }

    fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun handleSearchQuery(query: String) {

        // Hide the keyboard
        hideKeyboard(this)

        // Initialize MovieSearchAndSaveManager with the necessary dependencies
        val movieSearchAndSaveManager = MovieSearchAndSaveManager(WeakReference(this), db)

        // Search for the movie in the local database by title
        lifecycleScope.launch {
            val movieFromDb = db.movieDao().getMovieByTitle(query)
            if (movieFromDb != null) {
                // Movie found in local DB, navigate to movie detail
                navigateToMovieDetail(movieFromDb.imdbID)
            } else {
                // Movie not found in local DB, proceed to API search and save
                val imdbID = movieSearchAndSaveManager.searchAndSaveMovie(query)
                if (imdbID != null) {
                    // Movie found in API, navigate to movie detail
                    navigateToMovieDetail(imdbID)
                }
            }
        }
    }

    private fun navigateToMovieDetail(imdbID: String) {
        val destination = R.id.fragment_movie_detail
        val bundle = Bundle()
        bundle.putString("imdbId", imdbID)
        navController.navigate(destination, bundle)
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }





    private suspend fun searchApiForMovie(movieTitle: String) {
        // Replace spaces in the movie title with "+"
        val formattedMovieTitle = movieTitle.replace(" ", "+")

        // Construct the API URL
        val apiKey = "187966"
        val apiUrl = "https://www.omdbapi.com/?t=$formattedMovieTitle&apiKey=$apiKey"

        // Perform the API request and handle the response
        val response = withContext(Dispatchers.IO) {
            // Perform the API request using libraries like Retrofit or OkHttpClient
            // Parse the response and check if the movie was found
            // If the movie is found, update the local database with the movie details
            // If the movie is not found, show an error message to the user
            // Handle cases where there's no internet connection
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
    override fun onResume() {
        super.onResume()
        setSupportActionBar(binding.statusBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.show()
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
            else -> Toast.makeText(this, "Unknown icon clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToPosition(recyclerView: RecyclerView) {
        binding.mainScrollView.post {
            val scrollTo = recyclerView.top
            binding.mainScrollView.smoothScrollTo(0, scrollTo)
        }
    }

    fun onMovieClicked(movie: Movie) {
        Log.d("MainActivity", "Movie clicked: ${movie.imdbID}")
        val destination = R.id.fragment_movie_detail

        val bundle = Bundle()
        bundle.putString("imdbId", movie.imdbID)
        navController.navigate(destination, bundle)
    }

    fun onWatchlistClicked(view: View) {
        Log.d("MainActivity", "onWatchlistClicked called")
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.navigate(R.id.fragment_watchlist)
    }



    override fun onDestroy() {
        super.onDestroy()
        movieRecyclerManager.cancelAllActiveCalls()
        job.cancel()
    }
}
