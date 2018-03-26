package hu.intellicode.popularmovies.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by melinda.kostenszki on 2018.03.07.
 */

public class FavouritesContract {

    private FavouritesContract() {}

    public static final String CONTENT_AUTHORITY = "hu.intellicode.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVS = "favourites";

    //Inner class that defines constant values for the database table.
    public static final class FavouritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVS).build();

        //The MIME type of the #CONTENT_URI for a list of favourites
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVS;

        //The MIME type of the #CONTENT_URI for a single favourite
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVS;

        //details of favourites table
        public static final String TABLE_NAME_FAVS = "favourites";

        public static final String _ID = BaseColumns._ID;
        public static String COLUMN_FAV_MOVIE_ID = "movie_id";
        public static final String COLUMN_FAV_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_FAV_TITLE = "title";
        public static final String COLUMN_FAV_POSTER_PATH = "poster_path";
        public static final String COLUMN_FAV_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_FAV_RELEASE_DATE = "release_date";
        public static final String COLUMN_FAV_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_FAV_OVERVIEW = "overview";
    }
}
