package pt.ulusofona.deisi.cm2223.g21702361

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.Locale
import pt.ulusofona.deisi.cm2223.g21702361.databinding.WatchlistItemBinding

class WatchlistAdapter(
    internal var movies: MutableList<Movie>,
    internal var userMovieDetailsMap: Map<String, UserMovieDetails>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    inner class WatchlistViewHolder(private val watchListBinder: WatchlistItemBinding) :
        RecyclerView.ViewHolder(watchListBinder.root) {
        fun loadMoviePosters(movie: Movie) {
            Glide.with(itemView.context)
                .load(movie.poster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(watchListBinder.posterImageView)

            watchListBinder.titleTextView.text = movie.title

            val userMovieDetails = userMovieDetailsMap[movie.imdbId]
            watchListBinder.userRatingTextView.text = userMovieDetails?.userRating?.toString() ?: "N/A"
            watchListBinder.countyTextView.text = userMovieDetails?.county ?: "N/A"

            try {
                watchListBinder.imdbRatingTextView.text = String.format(Locale.getDefault(), "%.1f", movie.imdbrating.toFloat())
            } catch (e: NumberFormatException) {
                watchListBinder.imdbRatingTextView.text = "N/A"
            }

            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val binding = WatchlistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WatchlistViewHolder(binding)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.loadMoviePosters(movies[position])
    }

    fun updateMovies(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }
}
