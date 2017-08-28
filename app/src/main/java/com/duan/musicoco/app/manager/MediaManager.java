package com.duan.musicoco.app.manager;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.modle.SongInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/24.
 * 线程安全的单例，该类在播放进程中也会用到，此时单例失效。
 */

public class MediaManager {

    private HashSet<SongInfo> songs;

    private static volatile MediaManager MEDIAMANAGER;

    private MediaManager() {
    }

    //传入 Application Context
    public static MediaManager getInstance() {
        if (MEDIAMANAGER == null) {
            synchronized (MediaManager.class) {
                if (MEDIAMANAGER == null)
                    MEDIAMANAGER = new MediaManager();
            }
        }
        return MEDIAMANAGER;
    }

    public HashSet<SongInfo> refreshData(Context context) {
        if (songs == null)
            songs = new HashSet<>();
        else
            songs.clear();

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor == null) {
            return songs;
        }

        while (cursor.moveToNext()) {
            SongInfo song = new SongInfo();
            song.setAlbum_id(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_ID)));
            song.setAlbum_path(getAlbumArtPicPath(context, song.getAlbum_id()));
            song.setTitle_key(cursor.getString(cursor.getColumnIndex(SongInfo.TITLE_KEY)));
            song.setArtist_key(cursor.getString(cursor.getColumnIndex(SongInfo.ARTIST_KEY)));
            song.setAlbum_key(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_KEY)));
            song.setArtist(cursor.getString(cursor.getColumnIndex(SongInfo.ARTIST)));
            song.setAlbum(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM)));
            song.setData(cursor.getString(cursor.getColumnIndex(SongInfo.DATA)));
            song.setDisplay_name(cursor.getString(cursor.getColumnIndex(SongInfo.DISPLAY_NAME)));
            song.setTitle(cursor.getString(cursor.getColumnIndex(SongInfo.TITLE)));
            song.setMime_type(cursor.getString(cursor.getColumnIndex(SongInfo.MIME_TYPE)));
            song.setYear(cursor.getLong(cursor.getColumnIndex(SongInfo.YEAR)));
            song.setDuration(cursor.getLong(cursor.getColumnIndex(SongInfo.DURATION)));
            song.setSize(cursor.getLong(cursor.getColumnIndex(SongInfo.SIZE)));
            song.setDate_added(cursor.getLong(cursor.getColumnIndex(SongInfo.DATE_ADDED)));
            song.setDate_modified(cursor.getLong(cursor.getColumnIndex(SongInfo.DATE_MODIFIED)));

            songs.add(song);
        }
        cursor.close();
        return songs;
    }

    //根据专辑 id 获得专辑图片保存路径
    private String getAlbumArtPicPath(Context context, String albumId) {
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Cursor cur = context.getContentResolver().query(Uri.parse("content://media" +
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId), projection, null, null,
                null);
        if (cur == null) {
            return null;
        }

        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();
        return imagePath;
    }

    @Nullable
    public SongInfo getSongInfo(Context context, @NonNull Song song) {
        check(context);
        SongInfo info = null;
        for (SongInfo s : songs) {
            info = s;
            if (info.getData().equals(song.path)) {
                break;
            }
        }
        return info;
    }

    public SongInfo getSongInfo(Context context, @NonNull String path) {
        return getSongInfo(context, new Song(path));
    }

    public List<Song> getSongList(Context context) {
        check(context);
        List<Song> songInfos = new ArrayList<>();
        for (SongInfo song : songs) {
            songInfos.add(new Song(song.getData()));
        }
        return songInfos;
    }

    public List<SongInfo> getSongInfoList(Context context) {
        check(context);
        List<SongInfo> songInfos = new ArrayList<>();
        for (SongInfo song : songs) {
            songInfos.add(song);
        }
        return songInfos;
    }

    private void check(Context context) {
        if (songs == null)
            refreshData(context);
    }

    public void scanSdCard(Context context, @Nullable MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection.scanFile(context, new String[]{Environment
                .getExternalStorageDirectory().getAbsolutePath()}, null, listener);

    }

    /**
     * 检查媒体库是否为空
     *
     * @param refresh 是否要重新获取数据之后再确定，这个过程可能比较耗时
     * @return 为空返回 true
     */
    public boolean emptyMediaLibrary(Context context, boolean refresh) {
        if (refresh) {
            refreshData(context);
        } else {
            check(context);
        }

        if (songs.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
