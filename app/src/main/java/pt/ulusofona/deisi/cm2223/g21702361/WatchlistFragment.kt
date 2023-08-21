package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        loadMoviesBasedOnUserDetails()
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
}
