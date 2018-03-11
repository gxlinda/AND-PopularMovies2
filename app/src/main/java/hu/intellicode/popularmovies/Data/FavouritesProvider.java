package hu.intellicode.popularmovies.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by melinda.kostenszki on 2018.03.07.
 */

public class FavouritesProvider extends ContentProvider {

    //Tag for the log messages
    public static final String LOG_TAG = FavouritesProvider.class.getSimpleName();

    // Use an int for each URI we will run, this represents the different queries (for UriMatcher)
    private static final int FAVOURITES = 100;
    private static final int FAVOURITE_ID = 101;
    private static final int FAVOURITE_FILTERED_BY_MOVIE_ID = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavouritesContract.CONTENT_AUTHORITY, FavouritesContract.PATH_FAVS, FAVOURITES);
        uriMatcher.addURI(FavouritesContract.CONTENT_AUTHORITY, FavouritesContract.PATH_FAVS + "/#", FAVOURITE_ID);

        return uriMatcher;
    }

    //Database helper object
    private FavouritesDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new FavouritesDbHelper(context);
        return true;
    }


    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case FAVOURITES:
                // Insert new values into the database
                // Inserting values into favourites table
                long id = db.insert(FavouritesContract.FavouritesEntry.TABLE_NAME_FAVS, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(FavouritesContract.FavouritesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }


    // Implement query to handle requests for data by URI
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the favourites directory and write a default case
        switch (match) {
            // Query for the favourites directory
            case FAVOURITES:
                retCursor = db.query(FavouritesContract.FavouritesEntry.TABLE_NAME_FAVS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

//            case FAVOURITE_FILTERED_BY_MOVIE_ID:
//                retCursor = db.query(FavouritesContract.FavouritesEntry.TABLE_NAME_FAVS,
//                        projection,
//                        "movie_id = ?",
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder);
//                break;

            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }


    // Implement delete to delete a single row of data
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted favourites
        int favMoviesDeleted; // starts as 0

        // Delete a single row of data
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case FAVOURITE_ID:
                // Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                favMoviesDeleted = db.delete(FavouritesContract.FavouritesEntry.TABLE_NAME_FAVS, "_id=?", new String[]{id});
                break;
            case FAVOURITES:
                // Use selections/selectionArgs to filter
                favMoviesDeleted = db.delete(FavouritesContract.FavouritesEntry.TABLE_NAME_FAVS, selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (favMoviesDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of favourites deleted
        return favMoviesDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
