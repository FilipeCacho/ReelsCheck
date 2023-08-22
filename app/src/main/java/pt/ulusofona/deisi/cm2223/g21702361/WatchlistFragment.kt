package pt.ulusofona.deisi.cm2223.g21702361

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulusofona.deisi.cm2223.g21702361.AppDatabase
import pt.ulusofona.deisi.cm2223.g21702361.Movie
import pt.ulusofona.deisi.cm2223.g21702361.WatchlistAdapter
import pt.ulusofona.deisi.cm2223.g21702361.UserMovieDetails
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentWatchlistBinding
import pt.ulusofona.deisi.cm2223.g21702361.WatchlistFragmentDirections

class WatchlistFragment : Fragment() {

    private lateinit var binding: FragmentWatchlistBinding
    private lateinit var db: AppDatabase
    private lateinit var watchlistAdapter: WatchlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner // Set the lifecycle owner for data binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())

        watchlistAdapter = WatchlistAdapter(mutableListOf()) { movie ->
            navigateToMovieDetail(movie)
        }

        binding.watchlistAdapter = watchlistAdapter

        binding.watchlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.watchlistRecyclerView.adapter = watchlistAdapter

        // Check if savedInstanceState has data
        if (savedInstanceState != null && savedInstanceState.containsKey("MOVIE_LIST")) {
            val savedMovies: MutableList<Movie> = savedInstanceState.getParcelableArrayList("MOVIE_LIST") ?: mutableListOf()
            watchlistAdapter.addMovies(savedMovies)
        } else {
            loadMoviesBasedOnUserDetails()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onPause() {
        super.onPause()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun navigateToMovieDetail(movie: Movie) {
        val action = WatchlistFragmentDirections.actionWatchlistToMovieDetailFragment(
            imdbId = movie.imdbID,
            movieTitle = movie.title,
            posterPath = movie.poster
        )
        findNavController().navigate(action)
    }

    private fun loadMoviesBasedOnUserDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userMovieDetailsList = loadUserMovieDetailsFromDb()

            val movieList = mutableListOf<Movie>()
            for (userMovieDetails in userMovieDetailsList) {
                if (userMovieDetails.userRating != null) {
                    val movie = db.movieDao().getMovieByImdbId(userMovieDetails.imdbId)
                    movie?.let {
                        movieList.add(it)
                    }
                }
            }

            // Instead of reassigning watchlistAdapter, use its addMovies method
            watchlistAdapter.addMovies(movieList)
        }
    }

    private suspend fun loadUserMovieDetailsFromDb(): List<UserMovieDetails> {
        return withContext(Dispatchers.IO) {
            db.userMovieDetailsDao().getAllUserMovieDetails()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("MOVIE_LIST", ArrayList<Movie>(watchlistAdapter.movies))
    }
}
