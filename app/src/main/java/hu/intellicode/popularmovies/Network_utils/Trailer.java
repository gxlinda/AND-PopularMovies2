package hu.intellicode.popularmovies.Network_utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melinda.kostenszki on 2018.02.24.
 */

public class Trailer implements Parcelable {

    @SerializedName("id")
    private String id;
    @SerializedName("key")
    private String videoKey;
    @SerializedName("name")
    private String videoName;

    public Trailer() {
    }

    public Trailer(String id, String videoKey, String videoName) {
        this.id = id;
        this.videoKey = videoKey;
        this.videoName = videoName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
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
        parcel.writeString(videoKey);
        parcel.writeString(videoName);
    }

    /**
     * Static field used to regenerate object, individually or as arrays
     */
    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        public Trailer createFromParcel(Parcel pc) {
            return new Trailer(pc);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    /**
     * Ctor from Parcel, reads back fields IN THE ORDER they were written
     */
    public Trailer(Parcel pc) {
        id = pc.readString();
        videoKey = pc.readString();
        videoName = pc.readString();
    }
}
