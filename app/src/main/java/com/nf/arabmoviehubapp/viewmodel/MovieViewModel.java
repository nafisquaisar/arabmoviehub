package com.nf.arabmoviehubapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nf.arabmoviehubapp.data.model.Movie;
import com.nf.arabmoviehubapp.data.model.MovieResponse;
import com.nf.arabmoviehubapp.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieViewModel extends ViewModel {

    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private static final String API_KEY = "YOUR_TMDB_API_KEY";
    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    public LiveData<String> getError() {
        return error;
    }


    public void loadPopularMovies() {
        loading.setValue(true);
        RetrofitClient.getApi().getPopularMovies(API_KEY, "en-US", 1).enqueue(new Callback<MovieResponse>() {

            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    movies.setValue(response.body().getResults());
                } else {
                    error.setValue("Unable to load movies");
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                loading.setValue(false);
                error.setValue(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
