package hu.intellicode.popularmovies.Network_utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by melinda.kostenszki on 2018.03.03.
 */

public class TrailerResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("results")
    private List<Trailer> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }
}
