package pt.ulusofona.deisi.cm2223.g21702361

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CinemaMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var db: AppDatabase
    private var googleMap: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.setOnMarkerClickListener(this)
        // Enable the zoom controls
        googleMap?.uiSettings?.isZoomControlsEnabled = true


        enableUserLocation()
        loadCinemas()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                val marker = googleMap?.addMarker(markerOptions)
                marker?.tag = cinema.cinema_name
            }

            val firstLocation = LatLng(cinemas[0].latitude, cinemas[0].longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
        }
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
