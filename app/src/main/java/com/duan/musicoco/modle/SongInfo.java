package com.duan.musicoco.modle;

import android.provider.MediaStore;

/**
 * Created by DuanJiaNing on 2017/5/24.
 */

final public class SongInfo implements MediaStore.Audio.AudioColumns {

    //用于搜索、排序、分类
    private String title_key;
    private String artist_key;
    private String album_key;

    //时间 ms
    private long duration;

    //艺术家
    private String artist;

    //所属专辑
    private String album;

    //专辑 ID
    private String album_id;

    //专辑图片路径
    private String album_path;

    //专辑录制时间
    private long year;

    //磁盘上的保存路径
    //与服务端的 path 域对应，对于同一首歌曲（文件路径相同），两者应该相同
    private String data;

    //文件大小 bytes
    private long size;

    //显示的名字
    private String display_name;

    //内容标题
    private String title;

    //文件被加入媒体库的时间
    private long date_added;

    //文件最后修改时间
    private long date_modified;

    //MIME type
    private String mime_type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SongInfo songInfo = (SongInfo) o;

        return data.equals(songInfo.data);

    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return "DBSongInfo{" +
                "title_key='" + title_key + '\'' +
                ", artist_key='" + artist_key + '\'' +
                ", album_key='" + album_key + '\'' +
                ", duration=" + duration +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", album_id='" + album_id + '\'' +
                ", album_path='" + album_path + '\'' +
                ", year=" + year +
                ", data='" + data + '\'' +
                ", size=" + size +
                ", display_name='" + display_name + '\'' +
                ", title='" + title + '\'' +
                ", date_added=" + date_added +
                ", date_modified=" + date_modified +
                ", mime_type='" + mime_type + '\'' +
                '}' + "\n";
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public void setAlbum_path(String album_path) {
        this.album_path = album_path;
    }

    public void setTitle_key(String title_key) {
        this.title_key = title_key;
    }

    public void setArtist_key(String artist_key) {
        this.artist_key = artist_key;
    }

    public void setAlbum_key(String album_key) {
        this.album_key = album_key;
    }


    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


    public void setData(String data) {
        this.data = data;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }


    public void setYear(long year) {
        this.year = year;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDate_added(long date_added) {
        this.date_added = date_added;
    }

    public void setDate_modified(long date_modified) {
        this.date_modified = date_modified;
    }


    public String getTitle_key() {
        return title_key;
    }

    public String getArtist_key() {
        return artist_key;
    }

    public String getAlbum_key() {
        return album_key;
    }

    public long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getYear() {
        return year;
    }

    public String getData() {
        return data;
    }

    public long getSize() {
        return size;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getTitle() {
        return title;
    }

    public long getDate_added() {
        return date_added;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public String getMime_type() {
        return mime_type;
    }


    public String getAlbum_id() {
        return album_id;
    }


    public String getAlbum_path() {
        return album_path;
    }

}
