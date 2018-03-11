package hu.intellicode.popularmovies.Network_utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by melinda.kostenszki on 2018.03.03.
 */

public class ReviewResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("results")
    private List<Review> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}
