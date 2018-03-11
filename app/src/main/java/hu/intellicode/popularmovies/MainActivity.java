package hu.intellicode.popularmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.intellicode.popularmovies.Data.FavouritesContract;
import hu.intellicode.popularmovies.Data.MovieListLoader;
import hu.intellicode.popularmovies.Network_utils.ApiUtil;
import hu.intellicode.popularmovies.Network_utils.Movie;
import hu.intellicode.popularmovies.Network_utils.MovieEndpoints;
import hu.intellicode.popularmovies.Network_utils.MovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    In the making of this app I used materials from Udacity lessons
    Used guidelines for implementing Retrofit2: https://code.tutsplus.com/tutorials/getting-started-with-retrofit-2--cms-27792
 */

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String TAG = "MainActivity";
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final int ID_MOVIE_LIST_LOADER = 1;
    private List<Movie> movies = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private final String ORDER_BY_POPULAR = "popular";
    private final String ORDER_BY_TOP_RATED = "top_rated";
    private final String ORDER_BY_NOW_PLAYING = "now_playing";
    private final String ORDER_BY_FAVOURITES = "favourites";
    private String orderBy = ORDER_BY_POPULAR;
    private String subtitle;
    static final String MOVIE_DETAILS = "movie_details";
    private EndlessRecyclerViewScrollListener scrollListener;
    private MovieEndpoints movieEndpoints;
    private boolean isConnectedToInternet = false;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    Parcelable listState;
    GridLayoutManager layoutManager;
    private static ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setSubtitle(R.string.most_popular);

        subtitle = getResources().getString(R.string.most_popular);

        //Assign the views
        recyclerView = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.tv_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        int spanCount;

        //At portrait mode there will be 3 columns in the grid, in landscape mode there will be 5.
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 3;
        } else {
            spanCount = 5;
        }

        layoutManager = new GridLayoutManager(this, spanCount);

        recyclerView.setLayoutManager(layoutManager);
        movieAdapter = new MovieAdapter(movies);
        movieAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(movieAdapter);

        //we restore state after phone rotation
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("orderBy")) {
                orderBy = savedInstanceState.getString("orderBy");
                if (orderBy == ORDER_BY_FAVOURITES) {
                    getSupportLoaderManager().initLoader(ID_MOVIE_LIST_LOADER, null, MainActivity.this);
                }
            }
            if (savedInstanceState.containsKey("subtitle")) {
                subtitle = savedInstanceState.getString("subtitle");
                getSupportActionBar().setSubtitle(subtitle);
            }
        }

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                isLoading = true;
                currentPage = page + 1;
                loadNextDataFromApi(currentPage);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        initRetrofitAndLoadFirstData();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("orderBy", orderBy);
        outState.putString("subtitle", subtitle);
        // Save list scrolled state
        listState = layoutManager.onSaveInstanceState();
        outState.putParcelable("State", listState);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

    // Retrieve list state and list/item positions
        if (state != null)
    listState = state.getParcelable("State");
}

    private void initRetrofitAndLoadFirstData() {
        //init Retrofit service and load data if there is internet
        movieEndpoints = ApiUtil.getMovieEndpoints();
        if (!isConnectedToInternet()) {
            View progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

            // Update empty state with no connection error message
            movieAdapter.setMovieList(null);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setTextColor(getResources().getColor(R.color.colorWhite));
            emptyStateTextView.setText(R.string.no_internet_connection);
        } else loadFirstMoviesByChosenPreference();
    }

    public boolean isConnectedToInternet() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        assert connMgr != null; //line suggested by Lint
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        //hide empty state view if it was there earlier and load data
//emptyStateTextView.setVisibility(View.GONE);
//loadFirstMoviesByChosenPreference();
// Otherwise, display error
// First, hide loading indicator so error message will be visible
        isConnectedToInternet = networkInfo != null && networkInfo.isConnected();
        return isConnectedToInternet;
    }

    public void loadFirstMoviesByChosenPreference() {

        //Gets the used language of the phone
        String lang = Locale.getDefault().getLanguage();
        String usedLang;
        //if language is Hungarian, the app will use Hungarian, else it uses English
        if (lang.equals("hu")) {
            usedLang = "hu";
        } else usedLang = "en";

        scrollListener.resetState();
        currentPage = 1;
        isLastPage = false;
        movieAdapter.clear();

        movieEndpoints.getMoviesByChosenPreference(orderBy, API_KEY, currentPage, usedLang).enqueue(new Callback<MovieResponse>() {
                                                                                                            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {

                if (response.isSuccessful()) {
                    List<Movie> movies = (response.body().getResults());
                    TOTAL_PAGES = response.body().getTotalPages();
                    Log.d("MainActivity", "posts loaded from API");
                    progressBar.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.GONE);
                    try {
                        movieAdapter.setMovieList(movies);
                    }
                    catch (Exception e) {
                        // This will catch any exception, because they are all descended from Exception
                        Log.d("MainActivity", e.getMessage());


                    }

                    if (currentPage <= TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;

                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                emptyStateTextView.setText("Error loading from API");
                Log.d("MainActivity", "error loading from API");
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(final int offset) {
        Log.d(TAG, "loadNextPage: " + currentPage);
        //Gets the used language of the phone
        String lang = Locale.getDefault().getLanguage();
        String usedLang;
        //if language is Hungarian, the app will use Hungarian, else it uses English
        if (lang.equals("hu")) {
            usedLang = "hu";
        } else usedLang = "en";
        movieEndpoints.getMoviesByChosenPreference(orderBy, API_KEY, offset, usedLang).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {

                if (response.isSuccessful()) {
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;

                    List<Movie> newMovies = response.body().getResults();
                    movieAdapter.addAll(newMovies);
                    Log.d("MainActivity", "posts loaded from API");
                    progressBar.setVisibility(View.GONE);

                    if (currentPage != TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;

                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                emptyStateTextView.setText("Error loading from API");
                Log.d("MainActivity", "error loading from API");
            }
        });


    }

    //Creates menu on the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        //Return true so that the menu is displayed in the Toolbar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Checks the setting choosen by the user, and refreshes layout if necessary

        if (id == R.id.most_popular) {
            if (!orderBy.equals(ORDER_BY_POPULAR)) {
                orderBy = ORDER_BY_POPULAR;
                initRetrofitAndLoadFirstData();
                subtitle = getResources().getString(R.string.most_popular);
                getSupportActionBar().setSubtitle(subtitle);
                return true;
            }
        }
        if (id == R.id.highest_rated) {
            if (!orderBy.equals(ORDER_BY_TOP_RATED)) {
                orderBy = ORDER_BY_TOP_RATED;
                initRetrofitAndLoadFirstData();
                subtitle = getResources().getString(R.string.highest_rated);
                getSupportActionBar().setSubtitle(subtitle);
                return true;
            }
        }
        if (id == R.id.now_playing) {
            if (!orderBy.equals(ORDER_BY_NOW_PLAYING)) {
                orderBy = ORDER_BY_NOW_PLAYING;
                initRetrofitAndLoadFirstData();
                subtitle = getResources().getString(R.string.now_playing);
                getSupportActionBar().setSubtitle(subtitle);
                return true;
            }
        }
        if (id == R.id.favourite) {
            if (!orderBy.equals(ORDER_BY_FAVOURITES)) {
                orderBy = ORDER_BY_FAVOURITES;
                getSupportLoaderManager().initLoader(ID_MOVIE_LIST_LOADER, null, this);
                subtitle = getResources().getString(R.string.favourite);
                getSupportActionBar().setSubtitle(subtitle);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        Uri uri = FavouritesContract.FavouritesEntry.CONTENT_URI;
        return new MovieListLoader(this, uri);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        //hides the progress bar circle
        progressBar.setVisibility(View.GONE);

        // Clear the adapter of previous movie data
        movieAdapter.setMovieList(null);

        // If there is a valid list of movies, then add them to the adapter's data set.
        if (movies != null && !movies.isEmpty()) {
            emptyStateTextView.setVisibility(View.GONE);
            movieAdapter.setMovieList(movies);
            movieAdapter.notifyDataSetChanged();

        } else {
            // Set empty state text to display "There are no movies to display."
            emptyStateTextView.setText(R.string.no_movies);
            emptyStateTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movieAdapter.clear();
    }

    @Override
    public void onItemClick(int position, Movie movie, ImageView imageView) {
        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);

        //Passes the chosen Movie to Details Activity
        detailsIntent.putExtra(MOVIE_DETAILS, movie);

        //Passes the shared element info for transition
        // (used guidelines: https://plavatvornica.com/activity-animations-shared-element-transitions-demistified)
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageView, "transition");
        ActivityCompat.startActivity(MainActivity.this, detailsIntent, options.toBundle());
    }
}
