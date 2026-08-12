package com.nf.arabmoviehubapp.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nf.arabmoviehubapp.R;
import com.nf.arabmoviehubapp.data.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final List<Movie> movieList = new ArrayList<>();

    private final OnMovieClickListener listener;

    public MovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    public void setMovies(List<Movie> movies) {
        movieList.clear();
        if (movies != null) {
            movieList.addAll(movies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());

        holder.movieRating.setText(String.format("★ %.1f", movie.getVoteAverage()));
        String releaseDate = movie.getReleaseDate();

        if (releaseDate != null && releaseDate.length() >= 4) {
            holder.movieInfo.setText(releaseDate.substring(0, 4) + "  •  Movie");
        } else {
            holder.movieInfo.setText("Movie");
        }
        loadPoster(movie, holder.moviePoster);
        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    private void loadPoster(Movie movie, ImageView imageView) {

        String posterName = movie.getPosterPath();
        if (posterName == null || posterName.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.placeholder_movie);
            return;
        }
        int resourceId = imageView.getContext().getResources().getIdentifier(posterName, "drawable", imageView.getContext().getPackageName());
        if (resourceId != 0) {
            Glide.with(imageView.getContext()).load(resourceId).placeholder(R.drawable.placeholder_movie).error(R.drawable.placeholder_movie).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_movie);
        }
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView moviePoster;
        TextView movieRating;
        TextView movieTitle;
        TextView movieInfo;

        public MovieViewHolder(@NonNull View itemView) {

            super(itemView);
            moviePoster = itemView.findViewById(R.id.moviePoster);
            movieRating = itemView.findViewById(R.id.movieRating);
            movieTitle = itemView.findViewById(R.id.movieTitle);
            movieInfo = itemView.findViewById(R.id.movieInfo);
        }
    }
}