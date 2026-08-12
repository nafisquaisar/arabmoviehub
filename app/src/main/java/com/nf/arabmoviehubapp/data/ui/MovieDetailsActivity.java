package com.nf.arabmoviehubapp.data.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.nf.arabmoviehubapp.R;
import com.nf.arabmoviehubapp.data.model.Movie;

public class MovieDetailsActivity extends AppCompatActivity {

    private ImageView backdrop;
    private ImageView poster;

    private TextView title;
    private TextView rating;
    private TextView releaseDate;
    private TextView overview;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(
                R.layout.activity_movie_details
        );

        View root =
                findViewById(
                        R.id.movieDetailsRoot
                );

        if (root != null) {

            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (view, windowInsets) -> {

                        Insets insets =
                                windowInsets.getInsets(
                                        WindowInsetsCompat.Type.systemBars()
                                                | WindowInsetsCompat.Type.displayCutout()
                                );

                        view.setPadding(
                                insets.left,
                                insets.top,
                                insets.right,
                                insets.bottom
                        );

                        return windowInsets;
                    }
            );

            ViewCompat.requestApplyInsets(root);
        }

        initializeViews();

        Movie movie =
                (Movie) getIntent()
                        .getSerializableExtra("movie");

        if (movie != null) {

            displayMovie(movie);
        }
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        backdrop =
                findViewById(
                        R.id.movieBackdrop
                );

        poster =
                findViewById(
                        R.id.movieDetailPoster
                );

        title =
                findViewById(
                        R.id.movieDetailTitle
                );

        rating =
                findViewById(
                        R.id.movieDetailRating
                );

        releaseDate =
                findViewById(
                        R.id.movieReleaseDate
                );

        overview =
                findViewById(
                        R.id.movieOverview
                );
    }

    // =========================================================
    // DISPLAY MOVIE
    // =========================================================

    private void displayMovie(Movie movie) {

        // TITLE
        title.setText(
                movie.getTitle()
        );

        // RATING
        rating.setText(
                String.format(
                        "★ %.1f / 10",
                        movie.getVoteAverage()
                )
        );

        // RELEASE DATE
        String release =
                movie.getReleaseDate();

        if (release != null
                && !release.isEmpty()) {

            releaseDate.setText(
                    release
            );

        } else {

            releaseDate.setText(
                    "Release date unavailable"
            );
        }

        // OVERVIEW
        String movieOverview =
                movie.getOverview();

        if (movieOverview != null
                && !movieOverview.isEmpty()) {

            overview.setText(
                    movieOverview
            );

        } else {

            overview.setText(
                    "No overview available."
            );
        }

        // LOCAL POSTER
        loadLocalImage(
                movie.getPosterPath(),
                poster
        );

        // LOCAL BACKDROP
        loadLocalImage(
                movie.getBackdropPath(),
                backdrop
        );
    }

    // =========================================================
    // LOAD LOCAL DRAWABLE
    // =========================================================

    private void loadLocalImage(
            String imageName,
            ImageView imageView
    ) {

        if (imageName == null
                || imageName.trim().isEmpty()) {

            imageView.setImageResource(
                    R.drawable.placeholder_movie
            );

            return;
        }

        int resourceId =
                getResources()
                        .getIdentifier(
                                imageName,
                                "drawable",
                                getPackageName()
                        );

        if (resourceId != 0) {

            Glide.with(this)
                    .load(resourceId)
                    .placeholder(
                            R.drawable.placeholder_movie
                    )
                    .error(
                            R.drawable.placeholder_movie
                    )
                    .into(imageView);

        } else {

            imageView.setImageResource(
                    R.drawable.placeholder_movie
            );
        }
    }
}