package com.duan.musicoco.main;

import com.duan.musicoco.aidl.Song;

import java.util.Date;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/6/27.
 * 歌单
 */

public class SongSheet {

    //歌单标题
    private String title;

    //歌单备注
    private String remarks;

    //包含歌曲，及歌曲的播放次数
    private List<Song> songs;

    //创建日期
    private Date date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
