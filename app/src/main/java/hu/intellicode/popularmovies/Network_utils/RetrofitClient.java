package hu.intellicode.popularmovies.Network_utils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by melinda.kostenszki on 2018.03.02.
 * Used guidelines for implementing Retrofit2: https://code.tutsplus.com/tutorials/getting-started-with-retrofit-2--cms-27792
 */

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getApiCall(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
