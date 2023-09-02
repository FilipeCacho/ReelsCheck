package pt.ulusofona.deisi.cm2223.g21702361

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pt.ulusofona.deisi.cm2223.g21702361.databinding.ActivityMainBinding
import java.lang.ref.WeakReference
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
    }

    private val supportedLocales =

        setOf(
            Locale("en"), // English
            Locale("pt", "PT"), // Portuguese (Portugal)
            Locale("es", "ES") // Spanish (Spain)
        )

    private var sensorWatcher: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var coordinateX: Float = 0.0f
    private var coordinateY: Float = 0.0f
    private var coordinateZ: Float = 0.0f
    private val shakeThreshold = 800
    private var moviesClicked = 0
    private var skipToRegistration = 0

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateLanguage(base))
    }

    private fun updateLanguage(context: Context): Context {
        val local =
            getPreferedLanguage(context) // Define this function to determine preferred locale
        val config = Configuration(context.resources.configuration)
        Locale.setDefault(local)
        config.setLocale(local)
        return context.createConfigurationContext(config)
    }

    private fun getPreferedLanguage(context: Context): Locale {
        val systemLanguage = Locale.getDefault()
        // Check if the user-selected locale is supported, and fallback to default if not
        return when {
            supportedLocales.contains(systemLanguage) -> systemLanguage
            else -> Locale.getDefault()
        }
    }

    lateinit var mainBinding: ActivityMainBinding
    val job = Job()
    private lateinit var movieRecyclerManager: MainMovieRecyclerManager
    private lateinit var db: AppDatabase
    private lateinit var recyclerViewSetupManager: MainRecyclerViewSetupManager
    private lateinit var navController: NavController
    private var searchView: SearchView? = null

    // Sensor shake listener
    private val sensorListener: SensorEventListener =
        object : SensorEventListener {
            // Phone shaken event
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {
                // Check accelerometer sensor
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastUpdate > 100) {
                        val diffTime = currentTime - lastUpdate
                        lastUpdate = currentTime

                        // Calculate the speed of the phone being shaken
                        val phoneSpeed =
                            Math.abs(x + y + z - coordinateX - coordinateY - coordinateZ) /
                                    diffTime * 10000

                        // If shake speed exceeds limit, trigger action
                        if (phoneSpeed > shakeThreshold) {
                            deviceShake()
                        }

                        coordinateX = x
                        coordinateY = y
                        coordinateZ = z
                    }
                }
            }
        }

    //Text to speech callback
    private val speechInit =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val voiceListener =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                Log.d("VoiceInputDebug", "Voice results: $voiceListener")
                val query = voiceListener?.get(0)
                searchView?.setQuery(query, true) // Submit the query
            } else {
                Log.d("VoiceInputDebug", "Result not OK or data is null")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainBinding.activity = this

        setSupportActionBar(mainBinding.statusBar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        //Initialize accelerometer sensor
        sensorWatcher = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        accelerometer = sensorWatcher?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorWatcher?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        // Initialize NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Make the NavHostFragment visible
        navHostFragment.view?.isVisible = true

        // Populate cinema data if the cinema table is empty
        db = AppDatabase.getDatabase(applicationContext)

        // Check if the cinema table is empty or doesn't exist
        val cinemasExist = runBlocking {
            val count = db.cinemaDao().getCinemaCount()
            count > 0
        }

        // If the cinema table is empty or doesn't exist, populate cinemas data into the database
        if (!cinemasExist) {
            lifecycleScope.launch {
                CinemaJSON.readCinemasFromAssets(applicationContext, db.cinemaDao())
            }
        }

        movieRecyclerManager = MainMovieRecyclerManager(WeakReference(this), db)
        recyclerViewSetupManager =
            MainRecyclerViewSetupManager(this) { movie -> onMovieClicked(movie) }

        // load cinema list to the app

        val mainRecyclerViews = recyclerViewSetupManager.getRecyclerViews()
        val movieAdapters = recyclerViewSetupManager.getMovieAdapters()

        mainRecyclerViews.forEachIndexed { index, recyclerView ->
            recyclerView.adapter = movieAdapters[index]
        }

        lifecycleScope.launch {
            val moviesFromDb = db.movieDao().getAllMovies()
            val totalMoviesInDb = moviesFromDb.size
            movieAdapters.forEachIndexed { index, adapter ->
                val recyclerViewId = index + 1
                val moviesFromAdapter = moviesFromDb.filter { it.recyclerViewId == recyclerViewId }
                adapter.addMovies(moviesFromAdapter)
            }

            movieRecyclerManager.setupAllRecyclerViews(*mainRecyclerViews.toTypedArray())
            movieRecyclerManager.fetchAllMoviesForRecyclerViews(
                recyclerViewSetupManager.getUrlsList(),
                *movieAdapters.toTypedArray()
            )
        }

        val callback =
            object : OnBackPressedCallback(true) {
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

        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem?.actionView as SearchView

        // change search bar items to white, needs to be done with findviewbyid
        val searchText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText?.setTextColor(Color.WHITE) // Change to the desired text color
        searchText?.setHintTextColor(Color.WHITE) // Change to the desired hint text color
        val closeButton =
            searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton?.setColorFilter(Color.WHITE) // Change to the desired close button color
        val searchButton =
            searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchButton?.setColorFilter(Color.WHITE) // Change to the desired search icon color
        val searchPlateView = searchView?.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlateView?.background =
            ContextCompat.getDrawable(this, R.drawable.search_underline_text_background)
        val searchButtonView =
            searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchButtonView?.setColorFilter(Color.WHITE) // Replace with your desired color

        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d("SearchDebug", "onQueryTextSubmit called with query: $query")

                    searchView?.clearFocus()
                    searchView?.onActionViewCollapsed()

                    handleSearchQuery(query)

                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    // Implement real-time filtering here if desired
                    return false
                }
            }
        )

        val micIcon = mainBinding.microphoneIcon
        micIcon.setOnClickListener {
            promptVoiceInput()

            true
        }

        return true
    }

    private fun deviceShake() {
        // Reset moviesClicked at the beginning of onShake
        moviesClicked = 0

        Log.d("MainActivity", "Shake detected!")
        val screenX = resources.displayMetrics.widthPixels / 2
        val screenY = resources.displayMetrics.heightPixels / 2

        val offsets = arrayOf(Pair(0, 0), Pair(50, 0), Pair(-50, 0), Pair(0, 50), Pair(0, -50))

        for (offset in offsets) {
            // If moviesClicked is equal to 1, stop the onShake completely
            if (moviesClicked == 1) {
                skipToRegistration = 1
                break
            }

            val (offsetX, offsetY) = offset
            val clickX = screenX + offsetX
            val clickY = screenY + offsetY

            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.fragment_movie_detail)

            if (fragment == null) {
                val motionEventDown =
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN,
                        clickX.toFloat(),
                        clickY.toFloat(),
                        0
                    )

                val motionEventUp =
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        clickX.toFloat(),
                        clickY.toFloat(),
                        0
                    )

                dispatchTouchEvent(motionEventDown)
                dispatchTouchEvent(motionEventUp)

                // Increment moviesClicked after a successful click
                moviesClicked = 1
            } else {
                // Fragment found, no need to continue clicking
                break
            }

            // Additional check after sleep to break the loop if fragment is found
            val lookForFragmentAfterDelay =
                fragmentManager.findFragmentById(R.id.fragment_movie_detail)
            if (lookForFragmentAfterDelay != null) {
                // Fragment found, no need to continue clicking
                break
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorWatcher?.unregisterListener(sensorListener)
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
                navigateToMovieDetail(movieFromDb.imdbId)
            } else {
                // Movie not found in local DB, proceed to API search and save
                val imdbId = movieSearchAndSaveManager.searchAndSaveMovie(query)
                if (imdbId != null) {
                    // Movie found in API, navigate to movie detail
                    navigateToMovieDetail(imdbId)
                }
                else {
                    //i have to force the activity to redraw itself because of the searchview :(
                    recreate()
                }
            }
        }
    }

    private fun navigateToMovieDetail(imdbId: String) {
        val destination = R.id.fragment_movie_detail
        val bundle = Bundle()
        bundle.putString("imdbId", imdbId)
        navController.navigate(destination, bundle)
    }

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun promptVoiceInput() {
        val userVoice = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        userVoice.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        userVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        userVoice.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a movie name")
        speechInit.launch(userVoice)
    }

    private suspend fun searchApiForMovie(movieTitle: String) {
        // Replace spaces in the movie title with "+"
        val formattedMovieTitle = movieTitle.replace(" ", "+")

        // Construct the API URL
        val apiKey = "187966"
        val apiUrl = "https://www.omdbapi.com/?t=$formattedMovieTitle&apiKey=$apiKey"

        // Perform the API request and handle the response
        val response = withContext(Dispatchers.IO) {}
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()
        setSupportActionBar(mainBinding.statusBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        sensorWatcher?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

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
        mainBinding.mainScrollView.post {
            val scrollTo = recyclerView.top
            mainBinding.mainScrollView.smoothScrollTo(0, scrollTo)
        }
    }

    fun onMovieClicked(movie: Movie) {
        moviesClicked = 1

        if (moviesClicked == 1 && skipToRegistration == 1) {

            Log.d("MainActivity", "Movie clicked: ${movie.imdbId}")
            val destination = R.id.fragment_movie_detail

            val bundle = Bundle()
            bundle.putString("imdbId", movie.imdbId)
            SharedVariable.triggerButtonClick = true
            navController.navigate(destination, bundle)

            moviesClicked = 0
        }

        if (moviesClicked == 1 && skipToRegistration == 0) {

            Log.d("MainActivity", "Movie clicked: ${movie.imdbId}")
            val destination = R.id.fragment_movie_detail

            val bundle = Bundle()
            bundle.putString("imdbId", movie.imdbId)
            SharedVariable.triggerButtonClick = false
            navController.navigate(destination, bundle)

            moviesClicked = 0
        }

        skipToRegistration = 0
    }

    fun onWatchlistClicked(view: View) {
        Log.d("MainActivity", "onWatchlistClicked called")
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.navigate(R.id.fragment_watchlist)
    }

    fun onMapClicked(view: View) {
        Log.d("MainActivity", "onMapClicked called")
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.navigate(R.id.fragment_cinema_map)
    }

    override fun onDestroy() {
        super.onDestroy()
        movieRecyclerManager.cancelAllActiveCalls()
        job.cancel()
    }
}