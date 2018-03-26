package hu.intellicode.popularmovies.Network_utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melinda.kostenszki on 2018.02.24.
 */

public class Review implements Parcelable {

    @SerializedName("id")
    private String id;
    @SerializedName("author")
    private String author;
    @SerializedName("content")
    private String content;

    public Review() {
    }

    public Review(String id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(author);
        parcel.writeString(content);
    }

    /**
     * Static field used to regenerate object, individually or as arrays
     */
    public static final Creator<Review> CREATOR = new Creator<Review>() {
        public Review createFromParcel(Parcel pc) {
            return new Review(pc);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    /**
     * Creator from Parcel, reads back fields IN THE ORDER they were written
     */
    public Review(Parcel pc) {
        id = pc.readString();
        author = pc.readString();
        content = pc.readString();
    }
}
