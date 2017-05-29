package com.duan.musicoco.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class Song implements Parcelable {

    //与客户端 SongInfo 中的 data 域对应，对于同一首歌曲（文件路径相同），两者应该相同
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
