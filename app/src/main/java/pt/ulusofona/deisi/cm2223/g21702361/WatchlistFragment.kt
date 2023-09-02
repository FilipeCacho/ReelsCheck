package pt.ulusofona.deisi.cm2223.g21702361

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentWatchlistBinding


class WatchlistFragment : Fragment() {

    private lateinit var fragmentWatchlistBinding: FragmentWatchlistBinding
    private lateinit var db: AppDatabase
    private lateinit var watchlistAdapter: WatchlistAdapter
    private var locationFilterMode = 0  // 0: No filter, 1: 500m, 2: 1000m
    private var ratingOrderMode = 0     // 0: No order, 1: Ascending, 2: Descending
    private var userMovieDetailsList: List<UserMovieDetails> = listOf()
    private var userMovieDetailsMap: Map<String, UserMovieDetails> = mapOf()

    private var currentUserLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("WatchlistFragment", "onCreateView called")
        fragmentWatchlistBinding = FragmentWatchlistBinding.inflate(inflater, container, false)
        fragmentWatchlistBinding.lifecycleOwner = viewLifecycleOwner
        fragmentWatchlistBinding.ratingtext0.visibility = View.VISIBLE // By default, show the initial text view
        fragmentWatchlistBinding.distancetext0.visibility = View.VISIBLE // By default, show the initial text view
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            currentUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            // Log for current user latitude and longitude
            Log.d("WatchlistFragment", "Current user latitude: ${currentUserLocation?.latitude}, longitude: ${currentUserLocation?.longitude}")
        }

        fetchCurrentLocation()

        return fragmentWatchlistBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("WatchlistFragment", "onViewCreated called")
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            locationFilterMode = it.getInt("locationFilterMode", 0)
            ratingOrderMode = it.getInt("ratingOrderMode", 0)
        }

        db = AppDatabase.getDatabase(requireContext())
        fragmentWatchlistBinding.watchlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        fragmentWatchlistBinding.iconOrder.setOnClickListener {
            ratingOrderMode = (ratingOrderMode + 1) % 3
            updateMovieList()
        }

        fragmentWatchlistBinding.iconLocation.setOnClickListener {
            locationFilterMode = (locationFilterMode + 1) % 3
            updateMovieList()
        }

        loadMoviesBasedOnUserDetails()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("WatchlistFragment", "onSaveInstanceState called")
        super.onSaveInstanceState(outState)
        outState.putInt("locationFilterMode", locationFilterMode)
        outState.putInt("ratingOrderMode", ratingOrderMode)
    }

    override fun onResume() {
        Log.d("WatchlistFragment", "onResume called")
        fetchCurrentLocation()
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            currentUserLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            // Log for current user latitude and longitude
            Log.d("WatchlistFragment", "Current user latitude: ${currentUserLocation?.latitude}, longitude: ${currentUserLocation?.longitude}")
        }

        fetchCurrentLocation()

        updateMovieList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Invalidate the cached location
        currentUserLocation = null
    }

    override fun onPause() {
        Log.d("WatchlistFragment", "onPause called")
        super.onPause()

    }

    private fun navigateToMovieDetail(movie: Movie) {
        Log.d("WatchlistFragment", "navigateToMovieDetail called for movie: ${movie.title}")
        val action = WatchlistFragmentDirections.actionWatchlistToMovieDetailFragment(
            imdbId = movie.imdbId,
            movieTitle = movie.title,
            posterPath = movie.poster
        )
        findNavController().navigate(action)
    }

    private fun loadMoviesBasedOnUserDetails() {
        Log.d("WatchlistFragment", "loadMoviesBasedOnUserDetails called")
        viewLifecycleOwner.lifecycleScope.launch {
            userMovieDetailsList = loadUserMovieDetailsFromDb()
            userMovieDetailsMap = userMovieDetailsList.associateBy { it.imdbId }
            val movieList = mutableListOf<Movie>()
            for (userMovieDetails in userMovieDetailsList) {
                val movie = db.movieDao().getMovieByImdbId(userMovieDetails.imdbId)
                movie?.let {
                    movieList.add(it)
                }
            }
            watchlistAdapter = WatchlistAdapter(movieList, userMovieDetailsMap) { movie ->
                navigateToMovieDetail(movie)
            }
            fragmentWatchlistBinding.watchlistRecyclerView.adapter = watchlistAdapter
            updateMovieList()
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("WatchlistFragment", "Location changed: $location")
            currentUserLocation = location
            updateMovieList()
        }

        //empty functions need to be here to avoid app crash
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Implement your logic here if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Implement your logic here if needed
        }

        override fun onProviderDisabled(provider: String) {
            // Implement your logic here if needed
        }
    }


    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        }
    }

    private fun updateMovieList() {
        Log.d("WatchlistFragment", "updateMovieList called")
        if (view == null || !::watchlistAdapter.isInitialized) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            if (!::watchlistAdapter.isInitialized) return@launch

            val filteredList = if (currentUserLocation != null) {
                userMovieDetailsList.filter { userMovieDetails ->
                    val movieLocation = Location("").apply {
                        latitude = userMovieDetails.latitude
                        longitude = userMovieDetails.longitude
                    }
                    val distance = currentUserLocation!!.distanceTo(movieLocation)
                    Log.d("WatchlistFragment", "Distance to movie: ${distance}m")

                    when (locationFilterMode) {
                        1 -> distance <= 500  // Movies within 500 meters
                        2 -> distance <= 1000 // Movies within 1000 meters
                        else -> true          // No filter
                    }
                }
            } else {
                userMovieDetailsList
            }

            Log.d("WatchlistFragment", "Filtered list size: ${filteredList.size}")

            // Print latitude and longitude of each movie
            for (movieDetails in filteredList) {
                Log.d("WatchlistFragment", "Movie Latitude: ${movieDetails.latitude}, Longitude: ${movieDetails.longitude}")
            }

            val sortedList = when (ratingOrderMode) {
                1 -> filteredList.sortedBy { it.userRating }
                2 -> filteredList.sortedByDescending { it.userRating }
                else -> filteredList
            }

            val sortedMovieList = sortedList.map { userMovieDetails ->
                withContext(Dispatchers.IO) {
                    db.movieDao().getMovieByImdbId(userMovieDetails.imdbId)
                }!!
            }

            Log.d("WatchlistFragment", "Sorted movie list size: ${sortedMovieList.size}")

            watchlistAdapter.updateMovies(sortedMovieList)

            when (ratingOrderMode) {
                0 -> fragmentWatchlistBinding.iconOrder.setImageResource(R.drawable.unsorted)
                1 -> fragmentWatchlistBinding.iconOrder.setImageResource(R.drawable.arrow_1_to_9)
                2 -> fragmentWatchlistBinding.iconOrder.setImageResource(R.drawable.arrow_9_to_1)
            }

            when (locationFilterMode) {
                0 -> fragmentWatchlistBinding.iconLocation.setImageResource(R.drawable.no_location)
                1 -> fragmentWatchlistBinding.iconLocation.setImageResource(R.drawable.location_near)
                2 -> fragmentWatchlistBinding.iconLocation.setImageResource(R.drawable.location_far)
            }

            fragmentWatchlistBinding.ratingtext0.visibility = if (ratingOrderMode == 0) View.VISIBLE else View.GONE
            fragmentWatchlistBinding.ratingtext1.visibility = if (ratingOrderMode == 1) View.VISIBLE else View.GONE
            fragmentWatchlistBinding.ratingtext2.visibility = if (ratingOrderMode == 2) View.VISIBLE else View.GONE

            fragmentWatchlistBinding.distancetext0.visibility = if (locationFilterMode == 0) View.VISIBLE else View.GONE
            fragmentWatchlistBinding.distancetext1.visibility = if (locationFilterMode == 1) View.VISIBLE else View.GONE
            fragmentWatchlistBinding.distancetext2.visibility = if (locationFilterMode == 2) View.VISIBLE else View.GONE
        }
    }


    private suspend fun loadUserMovieDetailsFromDb(): List<UserMovieDetails> {
        Log.d("WatchlistFragment", "loadUserMovieDetailsFromDb called")
        return withContext(Dispatchers.IO) {
            db.userMovieDetailsDao().getAllUserMovieDetails()
        }
    }
}
