package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentMovieDetailBinding

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





        //mainBinding.userMovieRegisterTitle.visibility = View.GONE
        binding.watchDateTextView.visibility = View.GONE

        //hide yellow ticket and gray text items
        binding.cinemaNameTextView.visibility = View.GONE
        binding.userCityTextView.visibility = View.GONE
        binding.userScoreTextView.visibility = View.GONE
        binding.userRatingTextView.visibility = View.GONE
        binding.dateTextView.visibility = View.GONE
        binding.timesWatchedTextView.visibility = View.GONE
        binding.CommentsText.visibility = View.GONE
        binding.commentsTextView.visibility = View.GONE
        binding.watchDateTextView.visibility = View.GONE
        binding.yellowTicket.visibility = View.GONE
        binding.cinemaTextView.visibility = View.GONE

        binding.watchDateTextView.visibility= View.GONE

        //hide toolbar might be useful later
        // (activity as? AppCompatActivity)?.supportActionBar?.hide()
        return binding.root
    }

    private suspend fun getMovieByImdbId(imdbId: String): Movie? {
        Log.d(
            "MovieDetailFragment",
            "Fetching movie details from database for IMDb ID: $imdbId"
        )
        return db.movieDao().getMovieByImdbId(imdbId)
    }

    private fun updateUI(movie: Movie) {
        binding.genreValue.text = movie.genre
        Glide.with(requireContext())
            .load(movie.poster)
            .into(binding.posterImageView)
        Log.d("MovieDetailFragment", "Movie poster loaded: ${movie.poster}")
    }

    private fun loadUserMovieDetails(movieImdbId: String) {
        val userMovieDetailsDao: UserMovieDetailsDao = db.userMovieDetailsDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val userMovieDetails = userMovieDetailsDao.getUserMovieDetails(movieImdbId)
            if (userMovieDetails != null) {
                // Set the text of each text view with the appropriate value
                binding.userRatingTextView.text = "${userMovieDetails.userRating}/10"
                binding.timesWatchedTextView.text = userMovieDetails.timesWatched.toString()
                binding.cinemaNameTextView.text = userMovieDetails.cinemaName.toString()
                binding.watchDateTextView.text = userMovieDetails.watchDate.toString()
                binding.commentsTextView.text = userMovieDetails.comments.toString()
                binding.userCityTextView.text=userMovieDetails.county.toString()

                // Set the visibility of each text view to VISIBLE
                binding.userRatingTextView.visibility = View.VISIBLE
                binding.timesWatchedTextView.visibility = View.VISIBLE
                binding.cinemaNameTextView.visibility = View.VISIBLE
                //mainBinding.watchDateTextView.visibility = View.VISIBLE
                binding.commentsTextView.visibility = View.VISIBLE
                //mainBinding.userMovieRegisterTitle.visibility= View.VISIBLE
                binding.userCityTextView.visibility=View.VISIBLE


                //show yellow ticket and gray text items
                binding.cinemaNameTextView.visibility = View.VISIBLE
                binding.userCityTextView.visibility = View.VISIBLE
                binding.userScoreTextView.visibility = View.VISIBLE
                binding.userRatingTextView.visibility = View.VISIBLE
                binding.dateTextView.visibility = View.VISIBLE
                binding.timesWatchedTextView.visibility = View.VISIBLE

                //hide comment text if the user didnt fill the comments during registration
                if (userMovieDetails.comments.toString().isBlank()) binding.CommentsText.visibility = View.GONE
                else binding.CommentsText.visibility = View.VISIBLE

                binding.commentsTextView.visibility = View.VISIBLE
                binding.yellowTicket.visibility = View.VISIBLE
                binding.cinemaTextView.visibility = View.VISIBLE



            } else {
                // User details not available for this movie, you can handle this case as needed
                // For example, you might want to hide the text views again
                binding.userRatingTextView.visibility = View.GONE
                binding.timesWatchedTextView.visibility = View.GONE
                binding.cinemaNameTextView.visibility = View.GONE
                binding.watchDateTextView.visibility = View.GONE
                binding.commentsTextView.visibility = View.GONE
                binding.userCityTextView.visibility=View.GONE
                //mainBinding.userMovieRegisterTitle.visibility= View.GONE

                //hide yellow ticket and gray text items
                binding.cinemaNameTextView.visibility = View.GONE
                binding.userCityTextView.visibility = View.GONE
                binding.userScoreTextView.visibility = View.GONE
                binding.userRatingTextView.visibility = View.GONE
                binding.dateTextView.visibility = View.GONE
                binding.timesWatchedTextView.visibility = View.GONE
                binding.CommentsText.visibility = View.GONE
                binding.commentsTextView.visibility = View.GONE
                binding.watchDateTextView.visibility = View.GONE
                binding.yellowTicket.visibility = View.GONE
                binding.cinemaTextView.visibility = View.GONE





                binding.root.requestLayout()
            }
        }
    }

    fun triggerRegisterButtonClick() {
        binding.registerButton.performClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (SharedVariable.triggerButtonClick) {


            db = AppDatabase.getDatabase(requireContext())
            val movieImdbId = arguments?.getString("imdbId")


            val movieTitle = arguments?.getString("movieTitle")
            val posterPath = arguments?.getString("posterPath")


            if (movieImdbId != null) {
                Log.d("MovieDetailFragment", "Fetching movie details for IMDb ID: $movieImdbId")
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val fetchedMovie = getMovieByImdbId(movieImdbId)
                        if (fetchedMovie != null) {
                            binding.movie = fetchedMovie
                            updateUI(fetchedMovie)

                            val userMovieDetails =
                                db.userMovieDetailsDao().getUserMovieDetails(movieImdbId)
                            if (userMovieDetails?.userRating != null) {
                                // If user rating exists, hide the register button and load the user details
                                binding.registerButton.visibility = View.GONE
                                loadUserMovieDetails(movieImdbId)
                            } else {
                                // If user rating doesn't exist, keep the register button visible
                                binding.registerButton.visibility = View.VISIBLE
                                binding.registerButton.setOnClickListener {
                                    val bundle = Bundle().apply {
                                        putString("imdbId", fetchedMovie.imdbId)
                                        putString("movieTitle", fetchedMovie.title)
                                        putString("posterPath", fetchedMovie.poster)
                                    }
                                    findNavController().navigate(
                                        R.id.action_movieDetailFragment_to_movieRegistrationFragment,
                                        bundle
                                    )

                                }


                                binding.registerButton.performClick()

                            }
                        } else {
                            Log.d("MovieDetailFragment", "Movie not found in database")
                        }
                    } catch (e: Exception) {
                        Log.e("MovieDetailFragment", "Error fetching movie details: ${e.message}")
                    }
                }
            }


            SharedVariable.triggerButtonClick = false // Reset the value after triggering the click
        }







        Log.d("MovieDetailFragment", "onViewCreated called")

        db = AppDatabase.getDatabase(requireContext())
        val movieImdbId = arguments?.getString("imdbId")


        val movieTitle = arguments?.getString("movieTitle")
        val posterPath = arguments?.getString("posterPath")


        if (movieImdbId != null) {
            Log.d("MovieDetailFragment", "Fetching movie details for IMDb ID: $movieImdbId")
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val fetchedMovie = getMovieByImdbId(movieImdbId)
                    if (fetchedMovie != null) {
                        binding.movie = fetchedMovie
                        updateUI(fetchedMovie)

                        val userMovieDetails =
                            db.userMovieDetailsDao().getUserMovieDetails(movieImdbId)
                        if (userMovieDetails?.userRating != null) {
                            // If user rating exists, hide the register button and load the user details
                            binding.registerButton.visibility = View.GONE
                            loadUserMovieDetails(movieImdbId)
                        } else {
                            // If user rating doesn't exist, keep the register button visible
                            binding.registerButton.visibility = View.VISIBLE
                            binding.registerButton.setOnClickListener {
                                val bundle = Bundle().apply {
                                    putString("imdbId", fetchedMovie.imdbId)
                                    putString("movieTitle", fetchedMovie.title)
                                    putString("posterPath", fetchedMovie.poster)
                                }
                                findNavController().navigate(
                                    R.id.action_movieDetailFragment_to_movieRegistrationFragment,
                                    bundle
                                )
                            }
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
}
