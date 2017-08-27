package com.duan.musicoco.util;

import android.content.Context;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.modle.DBSongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/22.
 */

public class MediaUtils {

    public static List<Song> DBSongInfoListToSongList(List<DBSongInfo> list) {
        List<Song> songs = new ArrayList<>();
        for (DBSongInfo d : list) {
            Song song = new Song(d.path);
            songs.add(song);
        }
        return songs;
    }

    public static List<SongInfo> DBSongInfoToSongInfoList(Context context, List<DBSongInfo> list, MediaManager mediaManager) {
        List<SongInfo> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            DBSongInfo info = list.get(i);
            SongInfo si = mediaManager.getSongInfo(context, info.path);
            if (si != null) {
                res.add(si);
            }
        }
        return res;
    }
}
