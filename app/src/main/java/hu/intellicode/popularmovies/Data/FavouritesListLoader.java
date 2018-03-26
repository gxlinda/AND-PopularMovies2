package hu.intellicode.popularmovies.Data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import hu.intellicode.popularmovies.Data.FavouritesContract.FavouritesEntry;
import hu.intellicode.popularmovies.Network_utils.Movie;

/**
 * Created by melinda.kostenszki on 2018.03.11.
 */

public class FavouritesListLoader extends AsyncTaskLoader<List<Movie>> {

    //Tag for log messages
    private static final String LOG_TAG = FavouritesListLoader.class.getName();
    Cursor movieData = null;

    //Query URL
    private Uri uri;

    public FavouritesListLoader(Context context, Uri uri) {
        super(context);
        this.uri = uri;
    }

    @Override
    protected void onStartLoading() {
            forceLoad();
        }

    @Override
    public List<Movie> loadInBackground() {

        // Perform the db request, and extract a list of favourite movies
        movieData = getContext().getContentResolver().query(FavouritesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        return transformCursorToMovies(movieData);
    }

    private List<Movie> transformCursorToMovies(Cursor cursor) {
        ArrayList<Movie> movies = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_MOVIE_ID));
                String originalTitle = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_ORIGINAL_TITLE));
                String title = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_TITLE));
                String posterPath = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_POSTER_PATH));
                String backdropPath = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_BACKDROP_PATH));
                String releaseDate = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_RELEASE_DATE));
                double voteAverage = cursor.getDouble(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_VOTE_AVERAGE));
                String overview = cursor.getString(cursor.getColumnIndex(FavouritesEntry.COLUMN_FAV_OVERVIEW));

                Movie movie = new Movie(id, originalTitle, title, posterPath, backdropPath, releaseDate, voteAverage, overview);

                movies.add(movie);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return movies;
    }
}
