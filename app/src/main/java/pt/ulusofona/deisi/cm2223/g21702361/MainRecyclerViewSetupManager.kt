package pt.ulusofona.deisi.cm2223.g21702361

import androidx.recyclerview.widget.RecyclerView

class MainRecyclerViewSetupManager(
    private val activity: MainActivity,
    private val onMovieClick: (Movie) -> Unit
) {

    fun getRecyclerViews(): List<RecyclerView> {
        return listOf(
            activity.binding.recyclerView1,
            activity.binding.recyclerView2,
            activity.binding.recyclerView3,
            activity.binding.recyclerView4,
            activity.binding.recyclerView5,
            activity.binding.recyclerView6,
            activity.binding.recyclerView7,
            activity.binding.recyclerView8
        )
    }

    fun getMovieAdapters(): List<MovieAdapter> {
        return listOf(
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick),
            MovieAdapter(mutableListOf(), onMovieClick)
        )
    }

    fun getUrlsList(): List<List<String>> {
        return listOf(
            MovieUrlDataSource.getMovieUrlsForRecyclerView1(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView2(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView3(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView4(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView5(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView6(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView7(),
            MovieUrlDataSource.getMovieUrlsForRecyclerView8()
        )
    }
}
