package com.safaryan.suren.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class GridItem implements Parcelable{
    private String image;
    private String title;
    private String release_date;
    private String vote_average;
    private String overview;
    private String backdrop_path;
    private String language;

    public GridItem() {
        super();
    }

    protected GridItem(Parcel in) {
        image = in.readString();
        title = in.readString();
        release_date = in.readString();
        vote_average = in.readString();
        overview = in.readString();
        backdrop_path = in.readString();
        language = in.readString();
    }

    public static final Creator<GridItem> CREATOR = new Creator<GridItem>() {
        @Override
        public GridItem createFromParcel(Parcel in) {
            return new GridItem(in);
        }

        @Override
        public GridItem[] newArray(int size) {
            return new GridItem[size];
        }
    };

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getVote_average() {
        return vote_average;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(vote_average);
        dest.writeString(overview);
        dest.writeString(backdrop_path);
        dest.writeString(language);
    }
}
