package com.duan.musicoco.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class Song implements Parcelable {

    public String path;

    public Song(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
    }

    public Song() {
    }

    protected Song(Parcel in) {
        this.path = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
