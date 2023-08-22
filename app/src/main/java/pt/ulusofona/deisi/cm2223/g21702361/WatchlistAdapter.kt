package pt.ulusofona.deisi.cm2223.g21702361

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.Locale
import pt.ulusofona.deisi.cm2223.g21702361.databinding.WatchlistItemBinding  // <-- Import the new binding
import pt.ulusofona.deisi.cm2223.g21702361.Movie


class WatchlistAdapter(
    internal var movies: MutableList<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {  // <-- Change here

    inner class WatchlistViewHolder(private val binding: WatchlistItemBinding) :  // <-- Change here
        RecyclerView.ViewHolder(binding.root) {
        val posterImageView = binding.posterImageView
        val imdbIconImageView = binding.imdbIcon
        val imdbRatingTextView = binding.imdbRatingTextView
        val titleTextView = binding.titleTextView

    }  // Closing for inner class WatchlistViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {  // <-- Change here
        val binding = WatchlistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)  // <-- Change here
        return WatchlistViewHolder(binding)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {  // <-- Change here
        val movie = movies[position]

        // Load movie poster image using glide
        Glide.with(holder.itemView.context)
            .load(movie.poster)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.posterImageView)

        //load title
        holder.titleTextView.text = movie.title


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
}
