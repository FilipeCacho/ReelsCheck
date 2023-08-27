package pt.ulusofona.deisi.cm2223.g21702361

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CinemaMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    private lateinit var db: AppDatabase
    private var googleMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var locationManager: LocationManager
    private var userMarker: Marker? = null // New member variable for user marker
    private var isCameraInitialized = false


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableUserLocation()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cinema_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnMarkerClickListener(this)
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        // Disable the default MyLocation layer; comment out this line
        // googleMap?.isMyLocationEnabled = true

        enableUserLocation()
        loadCinemas()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Comment out or remove this line to disable the default blue dot
            // googleMap?.isMyLocationEnabled = true

            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this, Looper.getMainLooper())
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    override fun onLocationChanged(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)

        // If marker is null, add new marker
        if (userMarker == null) {
            val markerOptions = MarkerOptions().position(userLocation)
            userMarker = googleMap?.addMarker(markerOptions)
        } else {
            // Update the position of the existing marker
            userMarker?.position = userLocation
        }

        // Initialize the camera position only once
        if (!isCameraInitialized) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
            isCameraInitialized = true
        }
    }


    private fun loadCinemas() {
        viewLifecycleOwner.lifecycleScope.launch {
            val cinemas = loadCinemasFromDb()

            if (cinemas.isEmpty()) {
                return@launch
            }

            for (cinema in cinemas) {
                val location = LatLng(cinema.latitude, cinema.longitude)
                val markerOptions = MarkerOptions().position(location).title(cinema.cinema_name)

                val userRating = getUserRatingForCinema(cinema.cinema_name)
                markerOptions.icon(getPinIconBasedOnScore(userRating))

                val marker = googleMap?.addMarker(markerOptions)
                marker?.tag = cinema.cinema_name
            }

            val firstLocation = LatLng(cinemas[0].latitude, cinemas[0].longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
        }
    }

    private suspend fun getUserRatingForCinema(cinemaName: String): Int {
        return withContext(Dispatchers.IO) {
            val userMovieDetailsList = db.userMovieDetailsDao().getUserMovieDetailsByCinema(cinemaName)
            userMovieDetailsList?.firstOrNull()?.userRating ?: 0
        }
    }



    // This function will return the bitmap descriptor for the marker icon based on the user's score
    private fun getPinIconBasedOnScore(score: Int): BitmapDescriptor {
        return when (score) {
            in 1..2 -> createCustomMarkerIcon(Color.parseColor("#FF5733")) // Red for range 1-2
            in 3..4 -> createCustomMarkerIcon(Color.parseColor("#FFA500")) // Orange for range 3-4
            in 5..6 -> createCustomMarkerIcon(Color.parseColor("#FFFF00")) // Yellow for range 5-6
            in 7..8 -> createCustomMarkerIcon(Color.parseColor("#ADFF2F")) // Green-Yellow for range 7-8
            in 9..10 -> createCustomMarkerIcon(Color.parseColor("#00FF00")) // Green for range 9-10
            else -> createCustomMarkerIcon(Color.parseColor("#808080")) // Default color if something goes wrong
        }
    }

    private fun createCustomMarkerIcon(color: Int): BitmapDescriptor {
        val width = 48 // Change as needed
        val height = 48 // Change as needed

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }

        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    override fun onMarkerClick(marker: Marker): Boolean {
        viewLifecycleOwner.lifecycleScope.launch {
            val cinemaName = marker.tag as? String ?: return@launch
            val movie = loadMovieByCinemaName(cinemaName)
            movie?.let {
                val action = CinemaMapFragmentDirections.actionCinemaMapToMovieDetailFragment(
                    imdbId = it.imdbId,
                    movieTitle = it.title,
                    posterPath = it.poster
                )
                findNavController().navigate(action)
            }
        }
        return true
    }

    private suspend fun loadCinemasFromDb(): List<Cinema> {
        return withContext(Dispatchers.IO) {
            db.cinemaDao().getAllCinemas()
        }
    }

    private suspend fun loadMovieByCinemaName(cinemaName: String): Movie? {
        return withContext(Dispatchers.IO) {
            val userMovieDetailsList = db.userMovieDetailsDao().getUserMovieDetailsByCinema(cinemaName)
            userMovieDetailsList?.firstOrNull()?.let { userMovieDetails ->
                return@withContext db.movieDao().getMovieByImdbId(userMovieDetails.imdbId)
            }
            return@withContext null
        }
    }
}
