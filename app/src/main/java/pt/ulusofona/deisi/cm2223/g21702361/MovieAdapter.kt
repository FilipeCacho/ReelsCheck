package pt.ulusofona.deisi.cm2223.g21702361

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import java.util.Locale

class MovieAdapter(var movies: MutableList<Movie>) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val posterImageView: ImageView = view.findViewById(R.id.posterImageView)
        val imdbIconImageView: ImageView = view.findViewById(R.id.imdbIcon)
        val imdbRatingTextView: TextView = view.findViewById(R.id.imdbRatingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]

        //load movie posters using picasso
        //Picasso.get()
          //  .load(movie.poster)
            //.into(holder.posterImageView)

        // Load the movie poster image using glide
        Glide.with(holder.view.context)
            .load(movie.poster)
            .into(holder.posterImageView)

        // Set IMDb rating icon and text
        try {
            holder.imdbRatingTextView.text = String.format(Locale.getDefault(), "%.1f", movie.imdbrating.toFloat())
        } catch (e: NumberFormatException) {
            holder.imdbRatingTextView.text = "N/A"  // or some default value
        }
    }

    fun addMovies(movies: List<Movie>) {
        this.movies.addAll(movies)
        notifyDataSetChanged()
    }

}
