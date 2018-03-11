package hu.intellicode.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.intellicode.popularmovies.Data.FavouritesContract;
import hu.intellicode.popularmovies.Data.MovieLoader;
import hu.intellicode.popularmovies.Network_utils.ApiUtil;
import hu.intellicode.popularmovies.Network_utils.Movie;
import hu.intellicode.popularmovies.Network_utils.MovieEndpoints;
import hu.intellicode.popularmovies.Network_utils.Review;
import hu.intellicode.popularmovies.Network_utils.ReviewResponse;
import hu.intellicode.popularmovies.Network_utils.Trailer;
import hu.intellicode.popularmovies.Network_utils.TrailerResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    In the making of this app I used materials from Udacity lessons
 */

public class DetailsActivity extends AppCompatActivity implements
        TrailerAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<Integer> {

    private static final String API_KEY = BuildConfig.API_KEY;
    private int MOVIE_ID;
    private static final int ID_MOVIE_LOADER = 2;
    private TextView originalTitleView, titleView, releaseDateView, overviewView, emptyStateTextView, tv, emptyReview;
    private ImageView backdropView, posterView, favButton;
    int pStatus = 0;
    private Handler handler = new Handler();
    static final String MOVIE_DETAILS = "movie_details";
    private List<Trailer> trailers = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private RecyclerView rvTrailer, rvReview;
    private boolean isFavourite;
    private MovieEndpoints videoEndpoints, reviewEndpoints;
    private Movie chosenMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().hide();

        chosenMovie = getIntent().getParcelableExtra(MOVIE_DETAILS);
        MOVIE_ID = chosenMovie.getId();

        findViews();
        setViews(chosenMovie);
        setVoteAverageProgressBar(chosenMovie);

        LinearLayoutManager videoLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTrailer.setLayoutManager(videoLayoutManager);
        trailerAdapter = new TrailerAdapter(trailers);
        trailerAdapter.setOnItemClickListener(this);
        rvTrailer.setAdapter(trailerAdapter);

        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvReview.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new ReviewAdapter(reviews);
        //reviewAdapter.setOnItemClickListener(this);
        rvReview.setAdapter(reviewAdapter);

        //we restore state after phone rotation
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("isFavourite")) {
                isFavourite = savedInstanceState.getBoolean("isFavourite");
                setFavouriteButton();
            }
        }

        //init Retrofit service
        videoEndpoints = ApiUtil.getMovieEndpoints();
        reviewEndpoints = ApiUtil.getMovieEndpoints();
        isConnectedToInternet();

        getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);

        //setFavouriteButton();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFavourite", isFavourite);
    }

    private void isConnectedToInternet() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            loadVideos();
            loadReviews();
        } else {
            // Update empty state with no connection error message
            trailerAdapter.setTrailerList(null);
            reviewAdapter.setReviewList(null);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setTextColor(getResources().getColor(R.color.colorWhite));
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private void findViews() {
        rvTrailer = findViewById(R.id.rv_trailers);
        rvReview = findViewById(R.id.rv_reviews);
        originalTitleView = findViewById(R.id.tv_original_title);
        titleView = findViewById(R.id.tv_details_title);
        releaseDateView = findViewById(R.id.tv_release_date);
        backdropView = findViewById(R.id.iv_detail_backdrop);
        overviewView = findViewById(R.id.tv_overview);
        posterView = findViewById(R.id.iv_detail_poster);
        emptyStateTextView = findViewById(R.id.tv_details_empty_state);
        favButton = findViewById(R.id.iv_heart);
        emptyReview = findViewById(R.id.empty_review);
    }

    private void setViews(Movie chosenMovie) {
        emptyStateTextView.setVisibility(View.GONE);
        String backdropURL = chosenMovie.getBackdropUriString();
        Picasso.with(this)
                .load(backdropURL)
                .into(backdropView);
        originalTitleView.setText(chosenMovie.getOriginalTitle());
        titleView.setText(chosenMovie.getTitle());
        releaseDateView.setText(chosenMovie.getReleaseDate());
        String overview = chosenMovie.getOverview();
        if (overview.length() == 0) {
            overviewView.setText(getResources().getText(R.string.not_available_in_your_language));
        } else {
            overviewView.setText(chosenMovie.getOverview());
        }
        String posterURL = chosenMovie.getImageUriString();
        Picasso.with(this)
                .load(posterURL)
                .into(posterView);
        setFavouriteButton();
    }

    private void setVoteAverageProgressBar(final Movie chosenMovie) {
        final ProgressBar mProgress = findViewById(R.id.circularProgressbar);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress

        tv = findViewById(R.id.tv);
        new Thread(new Runnable() {

            @Override
            public void run() {
                int maxValue = (int) (chosenMovie.getVoteAverage() * 10);
                while (pStatus < maxValue) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            mProgress.setProgress(pStatus);
                            tv.setText(String.valueOf(chosenMovie.getVoteAverage()));

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(16); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onItemClick(int position, Trailer trailer, ImageView imageView) {
        Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + trailer.getVideoKey());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    public void addToFavourites(View view) {
        if (!isFavourite) {
            ContentValues cv = new ContentValues();
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_MOVIE_ID, chosenMovie.getId());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_ORIGINAL_TITLE, chosenMovie.getOriginalTitle());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_TITLE, chosenMovie.getTitle());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_POSTER_PATH, chosenMovie.getPosterPath());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_BACKDROP_PATH, chosenMovie.getBackdropPath());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_RELEASE_DATE, chosenMovie.getReleaseDate());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_VOTE_AVERAGE, chosenMovie.getVoteAverage());
            cv.put(FavouritesContract.FavouritesEntry.COLUMN_FAV_OVERVIEW, chosenMovie.getOverview());

            Uri uri = getContentResolver().insert(FavouritesContract.FavouritesEntry.CONTENT_URI, cv);
            if (uri != null) {
                Toast toast = Toast.makeText(this, R.string.movie_added, Toast.LENGTH_SHORT);
                toast.getView().setBackgroundColor(Color.WHITE);
                toast.getView().setPadding(10, 10, 10, 10);
                TextView text = toast.getView().findViewById(android.R.id.message);
                text.setTextColor(Color.BLACK);
                toast.show();
                isFavourite = true;
                setFavouriteButton();
            }
        } else {
            String[] whereParam = new String[1];
            whereParam[0] = String.valueOf(MOVIE_ID);
            getContentResolver().delete(FavouritesContract.FavouritesEntry.CONTENT_URI,
                    FavouritesContract.FavouritesEntry.COLUMN_FAV_MOVIE_ID + "=?", whereParam);
            Toast toast = Toast.makeText(this, R.string.movie_removed, Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(Color.WHITE);
            toast.getView().setPadding(10, 10, 10, 10);
            TextView text = toast.getView().findViewById(android.R.id.message);
            text.setTextColor(Color.BLACK);
            toast.show();
            isFavourite = false;
            setFavouriteButton();
        }

        getSupportLoaderManager().restartLoader(ID_MOVIE_LOADER, null, this);
    }

    public void loadVideos() {
        //Gets the used language of the phone
        String lang = Locale.getDefault().getLanguage();
        String usedLang;
        //if language is Hungarian, the app will use Hungarian, else it uses English
        if (lang.equals("hu")) {
            usedLang = "hu";
        } else {
            usedLang = "en";
        }

        videoEndpoints.getTrailerData(MOVIE_ID, API_KEY, usedLang).enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {

                if (response.isSuccessful()) {
                    trailerAdapter.setTrailerList(response.body().getResults());
                    Log.d("DetailActivity", "videos loaded from API");
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable t) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText("Error loading from API");
                Log.d("DetailActivity", "error loading from API" + t);
            }
        });
    }

    public void loadReviews() {
        //As reviews are available only in English, we use English
        String usedLang;
        usedLang = "en";

        reviewEndpoints.getReviewData(MOVIE_ID, API_KEY, usedLang).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {

                if (response.isSuccessful()) {
                    if (response.body().getResults().isEmpty()) {
                        emptyReview.setVisibility(View.VISIBLE);
                    } else {
                        emptyReview.setVisibility(View.GONE);
                        reviewAdapter.setReviewList(response.body().getResults());
                        Log.d("DetailActivity", "reviews loaded from API");
                    }
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                emptyStateTextView.setVisibility(View.VISIBLE);
                emptyStateTextView.setText("Error loading from API");
                Log.d("DetailActivity", "error loading from API" + t);
            }
        });
    }

    @Override
    public Loader<Integer> onCreateLoader(int id, Bundle args) {
        Uri uri = FavouritesContract.FavouritesEntry.CONTENT_URI;
        return new MovieLoader(this, uri, MOVIE_ID);
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {
        //Checks if chosen movie is already in db, and if yes, sets Favourite Button
        if (data > 0) {
            isFavourite = true;
            setFavouriteButton();
        } else {
            isFavourite = false;
            setFavouriteButton();
        }
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {
    }

    public void setFavouriteButton() {
        if (isFavourite) {
            favButton.setBackgroundResource(R.drawable.fav_button_add);
        } else favButton.setBackgroundResource(R.drawable.fav_button_default);
    }
}
