package hu.intellicode.popularmovies.Network_utils;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by melinda.kostenszki on 2018.03.02.
 * Used guidelines for implementing Retrofit2: https://code.tutsplus.com/tutorials/getting-started-with-retrofit-2--cms-27792
 */

public interface MovieEndpoints {

    @GET("movie/{preference}")
    Call<MovieResponse> getMoviesByChosenPreference(@Path("preference") String orderBy, @Query("api_key") String apiKey, @Query("page") int pageIndex, @Query("language") String usedLang);

    @GET("movie/{id}/videos")
    Call<TrailerResponse> getTrailerData(@Path("id") int movieId, @Query("api_key") String apiKey, @Query("language") String usedLan);

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getReviewData(@Path("id") int movieId, @Query("api_key") String apiKey, @Query("language") String usedLan);
}
