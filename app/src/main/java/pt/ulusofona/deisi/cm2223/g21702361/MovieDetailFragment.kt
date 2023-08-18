package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentMovieDetailBinding
import pt.ulusofona.deisi.cm2223.g21702361.AppDatabase // Import the AppDatabase class
import pt.ulusofona.deisi.cm2223.g21702361.Movie // Import the Movie entity

class MovieDetailFragment : Fragment() {
    private lateinit var binding: FragmentMovieDetailBinding
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MovieDetailFragment", "onCreateView called")
        binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MovieDetailFragment", "onViewCreated called")

        db = AppDatabase.getDatabase(requireContext())

        val movieImdbId = arguments?.getString("movieImdbId")
        if (movieImdbId != null) {
            Log.d("MovieDetailFragment", "Fetching movie details for IMDb ID: $movieImdbId")
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val movie = getMovieByImdbId(movieImdbId)
                    if (movie != null) {
                        Log.d("MovieDetailFragment", "Movie details fetched successfully: $movie")
                        binding.titleTextView.text = movie.title

                        //Some movies have multiple genres, this would break the layout, so i keep only the first genre
                        val genre = movie.genre
                        val formattedGenre = if ("," in genre) {
                            genre.substringBefore(",")
                        } else {
                            genre
                        }
                        binding.genreValue.text = formattedGenre


                        binding.calendarValue.text = movie.released
                        binding.ratingValue.text = movie.imdbrating
                        Log.d("MovieDetailFragment", "Movie title set: ${movie.title}")
                        Glide.with(requireContext())
                            .load(movie.poster)
                            .into(binding.posterImageView)
                        Log.d("MovieDetailFragment", "Movie poster loaded: ${movie.poster}")
                    } else {
                        Log.d("MovieDetailFragment", "Movie not found in database")
                    }
                } catch (e: Exception) {
                    Log.e("MovieDetailFragment", "Error fetching movie details: ${e.message}")
                }
            }
        } else {
            Log.d("MovieDetailFragment", "No movie IMDb ID provided")
        }
    }

    private suspend fun getMovieByImdbId(imdbId: String): Movie? {
        Log.d("MovieDetailFragment", "Fetching movie details from database for IMDb ID: $imdbId")
        return db.movieDao().getMovieByImdbId(imdbId)
    }
}
