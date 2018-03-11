package hu.intellicode.popularmovies;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import hu.intellicode.popularmovies.Network_utils.Movie;

/**
 * Created by melinda.kostenszki on 2018.02.16.
 * Used guidelines:
 * https://antonioleiva.com/recyclerview-listener/
 * https://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
 * https://stackoverflow.com/questions/31799619/animating-shared-element-transitions-using-android-fragments-seems-to-be-a-night/31800026#31800026
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context context;
    private boolean isLoadingAdded = false;

    public interface OnItemClickListener  {
        void onItemClick(int position, Movie movie, ImageView imageView);
    }

    private List<Movie> movies;
    private OnItemClickListener listener;

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    @Override
    public MovieAdapter.MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_item, viewGroup, false);

        return new MovieAdapterViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder holder, int position) {
        Movie movie = movies.get(position);

        String voteString = String.valueOf(movie.getVoteAverage());
        holder.ratingView.setText(context.getResources().getString(R.string.rating) + voteString + "/10");

        Picasso.with(holder.itemView.getContext())
                .load(movie.getImageUriString())
                .into(holder.moviePosterView);

        holder.moviePosterView.setTransitionName(
                getImageTransitionName(holder.moviePosterView.getContext(), position)
        );
    }

    public String getImageTransitionName(Context context, int position) {
        return context.getString(R.string.movie_transition_name) + position;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        if (null == movies) return 0;
        return movies.size();
    }

    // Define viewholder
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        ImageView moviePosterView;
        TextView ratingView;

        public MovieAdapterViewHolder(final View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cv_item);
            moviePosterView = itemView.findViewById(R.id.iv_thumbnail);
            ratingView = itemView.findViewById(R.id.tv_avg_votes);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int pos = getAdapterPosition();
                Movie movie = movies.get(pos);
                listener.onItemClick(pos, movie, moviePosterView);
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.listener = listener;
    }

    // Helper method to set the actual movie list into the recyclerview on the activity
    public void setMovieList(List<Movie> movieList) {
        movies = movieList;
        notifyDataSetChanged();
    }

     /*
   Helpers
    */

    public void add(Movie r) {
        movies.add(r);
        notifyItemInserted(movies.size() - 1);
    }

    public void addAll(List<Movie> moveResults) {
        for (Movie result : moveResults) {
            add(result);
        }
    }

    public void remove(Movie r) {
        int position = movies.indexOf(r);
        if (position > -1) {
            movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Movie());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = movies.size() - 1;
        Movie movie = getItem(position);

        if (movie != null) {
            movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Movie getItem(int position) {
        return movies.get(position);
    }

}
