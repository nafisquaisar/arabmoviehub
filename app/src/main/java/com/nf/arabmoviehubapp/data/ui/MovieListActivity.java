package com.nf.arabmoviehubapp.data.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nf.arabmoviehubapp.R;
import com.nf.arabmoviehubapp.data.adapter.MovieAdapter;
import com.nf.arabmoviehubapp.data.model.Movie;
import com.nf.arabmoviehubapp.viewmodel.MovieViewModel;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private MovieAdapter adapter;
    private MovieViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_movie_list);

        View root = findViewById(R.id.movieListRoot);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {

            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(root);

        recyclerView = findViewById(R.id.movieRecyclerView);

        progressBar = findViewById(R.id.progressBar);

        setupRecyclerView();

        setupViewModel();


        adapter.setMovies(getDummyMovies());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(this::openMovieDetails);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MovieViewModel.class);
        viewModel.getMovies().observe(this, movies -> {
            if (movies != null) {
                adapter.setMovies(movies);
            }
        });
        viewModel.getLoading().observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMovieDetails(Movie movie) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    private List<Movie> getDummyMovies() {
        List<Movie> movies = new ArrayList<>();
        movies.add(createMovie(1, "Awarapan 2", "awarapan2", "2026-01-01", 7.2, "A romantic action drama movie."));
        movies.add(createMovie(2, "Bhoot Bangla", "bhootbangla", "2026-01-01", 6.8, "A horror comedy movie."));
        movies.add(createMovie(3, "Dhamal 4", "dhamal4", "2026-01-01", 7.0, "A comedy adventure movie."));
        movies.add(createMovie(4, "Dhurandhar", "dhurendar", "2026-01-01", 8.0, "An action thriller movie."));
        movies.add(createMovie(5, "Love Again", "loveagain", "2026-01-01", 6.9, "A romantic drama movie."));
        movies.add(createMovie(6, "Pathaan", "pathan", "2023-01-25", 7.0, "An action thriller movie."));
        return movies;
    }

    private Movie createMovie(int id, String title, String posterPath, String releaseDate, double rating, String overview) {
        return new Movie(id, title, posterPath, null, releaseDate, rating, overview);
    }
}