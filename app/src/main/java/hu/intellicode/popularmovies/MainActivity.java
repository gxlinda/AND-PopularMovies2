
package hu.intellicode.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import hu.intellicode.popularmovies.Data.FavouritesListLoader;
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

    //region variables
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
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    GridLayoutManager layoutManager;
    int lastFirstVisiblePosition;
    int selectedMenuItemIndex = 0; //default: popular
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subtitle = getResources().getString(R.string.most_popular);
        getSupportActionBar().setSubtitle(subtitle);

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

        boolean thereAreMovies = false;
        if (Utils.movieAdapter != null && Utils.movieAdapter.movies().size() > 0) {
            movieAdapter = Utils.movieAdapter;
            thereAreMovies = true;
            isFirstMovieList = true;
            progressBar.setVisibility(View.GONE);
        } else {
            movieAdapter = new MovieAdapter(movies);
        }

        if (Utils.haveDeletedAnItem) {
            getSupportLoaderManager().restartLoader(ID_MOVIE_LIST_LOADER, null, this);
            Utils.haveDeletedAnItem = false;
        }

        movieAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(movieAdapter);

        movieEndpoints = ApiUtil.getMovieEndpoints();

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                isLoading = true;
                currentPage = page + 1;
                loadData();
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        CheckInternet();
        if (!thereAreMovies && Utils.isConnectedToInternet(this)) {
            loadData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("orderBy", orderBy);
        outState.putString("subtitle", subtitle);
        outState.putInt("selectedMenuItemIndex", selectedMenuItemIndex);

        // Save list scrolled state
        lastFirstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        orderBy = state.getString("orderBy");
        subtitle = state.getString("subtitle");
        getSupportActionBar().setSubtitle(subtitle);

        selectedMenuItemIndex = state.getInt("selectedMenuItemIndex");
        OptionsItemLoad(selectedMenuItemIndex);

        if (Utils.haveDeletedAnItem) {
            getSupportLoaderManager().destroyLoader(ID_MOVIE_LIST_LOADER);
        }
    }

    boolean isFirstMovieList = false; //checks if this is the first set of movies (page 1)
    private void loadData() {
        if (!isFirstMovieList) {
            loadFirstMoviesByChosenPreference();
            isFirstMovieList = true;
        } else {
            loadNextDataFromApi(currentPage);
        }
        Utils.movieAdapter = movieAdapter;
    }

    private void CheckInternet() {
        //init Retrofit service and load data if there is internet
        //180316 áthelyezve: movieEndpoints = ApiUtil.getMovieEndpoints();
        if (!Utils.isConnectedToInternet(this)) {
            //region nincs internet, beállítjunk dolgokat
            View progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

            // Update empty state with no connection error message
            movieAdapter.setMovieList(null);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setTextColor(getResources().getColor(R.color.colorWhite));
            emptyStateTextView.setText(R.string.no_internet_connection);
            //endregion
        } else {
            //van internet, betöltjük az adaokat
            //loadFirstMoviesByChosenPreference();
        }
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

        movieEndpoints.getMoviesByChosenPreference(orderBy, API_KEY, currentPage, usedLang).enqueue(
                new Callback<MovieResponse>() {
                    //region létrehozott obj felülírt metódusai
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        Callback_onResponse(response, true);
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        emptyStateTextView.setText("Error loading from API");
                        Log.d("MainActivity", "error loading from API");
                    }
                }
        );
    }

    public void loadNextDataFromApi(final int offset) {
        Log.d(TAG, "loadNextPage: " + currentPage);
        //Gets the used language of the phone
        String lang = Locale.getDefault().getLanguage();
        String usedLang;
        //if language is Hungarian, the app will use Hungarian, else it uses English
        if (lang.equals("hu")) {
            usedLang = "hu";
        } else usedLang = "en";

        //180316 átszervezve
        movieEndpoints.getMoviesByChosenPreference(orderBy, API_KEY, offset, usedLang).enqueue(
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        Callback_onResponse(response, false);
                        //region korábbi kód 180316
                /*
                if (response.isSuccessful()) {
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;

                    List<Movie> newMovies = response.body().getResults();
                    movieAdapter.addAll(newMovies);
                    Log.d("MainActivity", "posts loaded from API");
                    progressBar.setVisibility(View.GONE);

                    if (currentPage != TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;

                    //Sets scrolled position
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);

                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
                */
                        //endregion
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        emptyStateTextView.setText("Error loading from API");
                        Log.d("MainActivity", "error loading from API");
                    }
                }//*

        );
    }

    private void Callback_onResponse(Response<MovieResponse> response, boolean isFirst) {
        if (response.isSuccessful()) {
            if (isFirst)//ELSŐ ALKALOM
            {
                TOTAL_PAGES = response.body().getTotalPages();
                List<Movie> movies = (response.body().getResults());
                movieAdapter.setMovieList(movies);
                progressBar.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.GONE);
                if (currentPage <= TOTAL_PAGES) {
                    movieAdapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }//if

            if (!isFirst)//NEM AZ ELSŐ ALKALOM
            {
                movieAdapter.removeLoadingFooter();
                isLoading = false;
                List<Movie> newMovies = response.body().getResults();
                movieAdapter.addAll(newMovies);
                progressBar.setVisibility(View.GONE);
                emptyStateTextView.setVisibility(View.GONE);
                if (currentPage != TOTAL_PAGES) {
                    movieAdapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }

                //Sets scrolled position ez ide sztem nem kell 180316
                //((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);
            }
        } else {
            int statusCode = response.code();
            // handle request errors depending on status code
        }
        Log.d("MainActivity", "posts loaded from API");
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

        selectedMenuItemIndex = item.getItemId();
        OptionsItemLoad(selectedMenuItemIndex);
        return super.onOptionsItemSelected(item);
    }

    private void OptionsItemLoad(int index) {

        if (selectedMenuItemIndex == R.id.most_popular) {
            if (!orderBy.equals(ORDER_BY_POPULAR)) {
                orderMoviesBy(ORDER_BY_POPULAR);
            }
        }
        if (selectedMenuItemIndex == R.id.highest_rated) {
            if (!orderBy.equals(ORDER_BY_TOP_RATED)) {
                lastFirstVisiblePosition = 0;
                orderMoviesBy(ORDER_BY_TOP_RATED);
            }
        }
        if (selectedMenuItemIndex == R.id.now_playing) {
            if (!orderBy.equals(ORDER_BY_NOW_PLAYING)) {
                orderMoviesBy(ORDER_BY_NOW_PLAYING);
            }
        }
        if (selectedMenuItemIndex == R.id.favourite) {
            if (!orderBy.equals(ORDER_BY_FAVOURITES)) {
                movieAdapter.setMovieList(null);
                orderMoviesBy(ORDER_BY_FAVOURITES);
            }
        }
    }

    //Starts an AsyncTask Loader to get favourites data from SQLite db
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        Uri uri = FavouritesContract.FavouritesEntry.CONTENT_URI;
        return new FavouritesListLoader(this, uri);
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

    //handles click on the chosen movie
    @Override
    public void onItemClick(int position, Movie movie, ImageView imageView) {
        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);

        //Passes the chosen Movie to Details Activity
        detailsIntent.putExtra(MOVIE_DETAILS, movie);

        //Passes the shared element info for transition
        // (used guidelines: https://plavatvornica.com/activity-animations-shared-element-transitions-demistified)
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageView, "transition");
        ActivityCompat.startActivity(MainActivity.this, detailsIntent, options.toBundle());
    }

    public void orderMoviesBy(String sortOrder) {
        switch (sortOrder) {
            case ORDER_BY_FAVOURITES:

                lastFirstVisiblePosition = 0;
                orderBy = ORDER_BY_FAVOURITES;
                subtitle = getResources().getString(R.string.favourite);
                getSupportActionBar().setSubtitle(subtitle);
                getSupportLoaderManager().initLoader(ID_MOVIE_LIST_LOADER, null, this);
                break;

            default:
                lastFirstVisiblePosition = 0;
                orderBy(sortOrder);
                getSupportActionBar().setSubtitle(subtitle);
                getSupportLoaderManager().destroyLoader(ID_MOVIE_LIST_LOADER);
                CheckInternet();
                if (Utils.isConnectedToInternet(this)) {
                    isFirstMovieList = false;
                    loadData();
                }
        }
    }

    public String orderBy(String pOrderBy) {
        switch (pOrderBy) {
            case ORDER_BY_TOP_RATED:
                orderBy = ORDER_BY_TOP_RATED;
                subtitle = getResources().getString(R.string.highest_rated);
                break;
            case ORDER_BY_NOW_PLAYING:
                orderBy = ORDER_BY_NOW_PLAYING;
                subtitle = getResources().getString(R.string.now_playing);
                break;
            default:
                orderBy = ORDER_BY_POPULAR;
                subtitle = getResources().getString(R.string.most_popular);
        } return orderBy;
    }
}

