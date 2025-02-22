package pt.ulusofona.deisi.cm2223.g21702361

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.Locale
import pt.ulusofona.deisi.cm2223.g21702361.databinding.MovieItemBinding

class MovieAdapter(
    private var movies: MutableList<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(private val binding: MovieItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val posterImageView = binding.posterImageView
        val imdbIconImageView = binding.imdbIcon
        val imdbRatingTextView = binding.imdbRatingTextView
    }  // Closing for inner class MovieViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]

        // Load movie poster image using glide
        Glide.with(holder.itemView.context)
            .load(movie.poster)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.posterImageView)

        // Set IMDb rating icon and text
        try {
            holder.imdbRatingTextView.text =
                String.format(Locale.getDefault(), "%.1f", movie.imdbrating.toFloat())
        } catch (e: NumberFormatException) {
            holder.imdbRatingTextView.text = "N/A"
        }

        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }

   fun addMovies(movies: List<Movie>) {
        this.movies.clear()
        this.movies.addAll(movies)
        notifyDataSetChanged()
    }

    fun getMovieAtPosition(position: Int): Movie? {
        return if (position < 0 || position >= movies.size) null else movies[position]
    }

    // New function to get the item at a specific position
    fun getItemAtPosition(position: Int): Movie? {
        return if (position < 0 || position >= movies.size) null else movies[position]
    }


}

