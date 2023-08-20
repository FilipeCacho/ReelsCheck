package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentMovieDetailBinding

// This is the binding adapter
@BindingAdapter("imageUrl")

fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        Glide.with(imgView.context)
            .load(imgUrl)
            .into(imgView)
    }
}

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
                    val fetchedMovie = getMovieByImdbId(movieImdbId)
                    if (fetchedMovie != null) {
                        binding.movie = fetchedMovie
                        updateUI(fetchedMovie)

                        binding.registerButton.setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("imdbId", fetchedMovie.imdbID)
                                putString("movieTitle", fetchedMovie.title)
                                putString("posterPath", fetchedMovie.poster)
                            }
                            findNavController().navigate(R.id.action_movieDetailFragment_to_movieRegistrationFragment, bundle)
                        }

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

    private fun updateUI(movie: Movie) {
        // Since you're using Data Binding, you won't need to set every UI element here.
        // But for any UI manipulations that aren't directly linked through Data Binding,
        // you can still set them here. For instance:


        binding.genreValue.text = movie.genre

        // For loading images, since they might not be handled directly by Data Binding:
        Glide.with(requireContext())
            .load(movie.poster)
            .into(binding.posterImageView)
        Log.d("MovieDetailFragment", "Movie poster loaded: ${movie.poster}")
    }

    private suspend fun getMovieByImdbId(imdbId: String): Movie? {
        Log.d("MovieDetailFragment", "Fetching movie details from database for IMDb ID: $imdbId")
        return db.movieDao().getMovieByImdbId(imdbId)
    }
}
