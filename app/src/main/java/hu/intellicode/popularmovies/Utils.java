package hu.intellicode.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by melinda.kostenszki on 2018.03.16.
 */

public class Utils {

    static public boolean isConnectedToInternet(Activity activity) {
        boolean isConnectedToInternet = false;
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        assert connMgr != null; //line suggested by Lint
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        isConnectedToInternet = networkInfo != null && networkInfo.isConnected();
        return isConnectedToInternet;
    }

    static public boolean haveDeletedAnItem;
    static public MovieAdapter movieAdapter;


}
