package hu.intellicode.popularmovies.Network_utils;

/**
 * Created by melinda.kostenszki on 2018.03.04.
 */

public class ApiUtil {
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    public static MovieEndpoints getMovieEndpoints() {
        return RetrofitClient.getApiCall(BASE_URL).create(MovieEndpoints.class);
    }
}
