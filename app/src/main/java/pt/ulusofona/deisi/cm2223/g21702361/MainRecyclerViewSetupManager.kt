package pt.ulusofona.deisi.cm2223.g21702361

import androidx.recyclerview.widget.RecyclerView

class MainRecyclerViewSetupManager(
    private val activity: MainActivity,
    private val onMovieClick: (Movie) -> Unit
) {

    fun getRecyclerViews(): List<RecyclerView> {
        return listOf(
            activity.mainBinding.recyclerView1,
            activity.mainBinding.recyclerView2,
            activity.mainBinding.recyclerView3,
            activity.mainBinding.recyclerView4,
            activity.mainBinding.recyclerView5,
            activity.mainBinding.recyclerView6,
            activity.mainBinding.recyclerView7,
            activity.mainBinding.recyclerView8
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
