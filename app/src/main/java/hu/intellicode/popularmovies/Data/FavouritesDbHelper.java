package hu.intellicode.popularmovies.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import hu.intellicode.popularmovies.Data.FavouritesContract.FavouritesEntry;

/**
 * Created by melinda.kostenszki on 2018.03.07.
 */

public class FavouritesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favourites.db";
    private static final int DATABASE_VERSION = 1;

    public FavouritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        addFavouritesTable(db);
    }

    private void addFavouritesTable(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_FAVOURITES_TABLE =  "CREATE TABLE " + FavouritesEntry.TABLE_NAME_FAVS + " ("
                + FavouritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavouritesEntry.COLUMN_FAV_MOVIE_ID + " INTEGER NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_ORIGINAL_TITLE + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_TITLE + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_POSTER_PATH + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_BACKDROP_PATH + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_RELEASE_DATE + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_VOTE_AVERAGE + " TEXT NOT NULL, "
                + FavouritesEntry.COLUMN_FAV_OVERVIEW + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouritesEntry.TABLE_NAME_FAVS);
    }
}