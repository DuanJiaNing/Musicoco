package com.duan.musicoco.util;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.DBSongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/22.
 */

public class SongUtils {

    public static List<Song> DBSongInfoListToSongList(List<DBSongInfo> list) {
        List<Song> songs = new ArrayList<>();
        for (DBSongInfo d : list) {
            Song song = new Song(d.path);
            songs.add(song);
        }
        return songs;
    }
}
