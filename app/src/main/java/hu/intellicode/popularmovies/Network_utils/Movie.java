package hu.intellicode.popularmovies.Network_utils;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melinda.kostenszki on 2018.02.24.
 */

public class Movie implements Parcelable {

    @SerializedName("id")
    private int id;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("title")
    private String title;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("overview")
    private String overview;
    @SerializedName("release_date")
    private String releaseDate;

    //This empty constructor is needed by the Parceler library
    public Movie() {
    }

    public Movie(int id, String originalTitle, String title, String posterPath, String backdropPath, String releaseDate, double voteAverage, String overview) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getBackdropUriString() {
        String MOVIE_BACKDROP_URL = "http://image.tmdb.org/t/p/w780/";
        Uri baseUri = Uri.parse(MOVIE_BACKDROP_URL);
        Uri.Builder backdropUri = baseUri.buildUpon();
        backdropUri.appendEncodedPath(getBackdropPath());
        return backdropUri.toString();
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getImageUriString() {
        String MOVIE_POSTER_URL = "http://image.tmdb.org/t/p/w185/";
        Uri baseUri = Uri.parse(MOVIE_POSTER_URL);
        Uri.Builder imageUri = baseUri.buildUpon();
        imageUri.appendEncodedPath(getPosterPath());
        return imageUri.toString();
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(originalTitle);
        parcel.writeString(title);
        parcel.writeString(posterPath);
        parcel.writeString(backdropPath);
        parcel.writeString(releaseDate);
        parcel.writeDouble(voteAverage);
        parcel.writeString(overview);
    }

    /** Static field used to regenerate object, individually or as arrays */
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel pc) {
            return new Movie(pc);
        }
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /**Ctor from Parcel, reads back fields IN THE ORDER they were written */
    public Movie(Parcel pc){
        id = pc.readInt();
        originalTitle = pc.readString();
        title =  pc.readString();
        posterPath = pc.readString();
        backdropPath = pc.readString();
        releaseDate = pc.readString();
        voteAverage = pc.readDouble();
        overview = pc.readString();
    }

}
