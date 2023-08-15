package pt.ulusofona.deisi.cm2223.g21702361

import androidx.recyclerview.widget.RecyclerView

class MainRecyclerViewSetupManager(private val activity: MainActivity) {

    fun getRecyclerViews(): List<RecyclerView> {
        return listOf(
            activity.findViewById(R.id.recyclerView1),
            activity.findViewById(R.id.recyclerView2),
            activity.findViewById(R.id.recyclerView3),
            activity.findViewById(R.id.recyclerView4),
            activity.findViewById(R.id.recyclerView5),
            activity.findViewById(R.id.recyclerView6),
            activity.findViewById(R.id.recyclerView7),
            activity.findViewById(R.id.recyclerView8)
        )
    }

    fun getMovieAdapters(): List<MovieAdapter> {
        return listOf(
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf()),
            MovieAdapter(mutableListOf())
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
