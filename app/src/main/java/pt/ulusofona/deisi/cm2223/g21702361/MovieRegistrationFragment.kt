package pt.ulusofona.deisi.cm2223.g21702361

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import pt.ulusofona.deisi.cm2223.g21702361.databinding.FragmentMovieRegistrationBinding
import com.bumptech.glide.Glide

class MovieRegistrationFragment : Fragment() {

    private var imdbId: String? = null
    private var movieTitle: String? = null
    private var posterPath: String? = null

    private lateinit var binding: FragmentMovieRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie_registration, container, false)

        arguments?.let {
            imdbId = it.getString("imdbId")
            movieTitle = it.getString("movieTitle")
            posterPath = it.getString("posterPath")
        }

        // Bind the data to the XML using the binding object
        binding.movieTitle = movieTitle
        binding.posterPath = posterPath // note: this doesn't load the image but sets the data
        binding.imdbId = imdbId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Even though we bound the `posterPath`, Glide is still necessary to load the image into the ImageView.
        posterPath?.let {
            Glide.with(requireContext())
                .load(it)
                .into(binding.posterImageViewRegistration)
        }
    }
}
