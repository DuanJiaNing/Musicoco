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
import com.duan.musicoco.app.SongInfo;

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

    private Context context;

    private MediaManager(Context context) {
        this.context = context;
    }

    //传入 Application Context
    public static MediaManager getInstance(Context context) {
        if (MEDIAMANAGER == null) {
            synchronized (MediaManager.class) {
                if (MEDIAMANAGER == null)
                    MEDIAMANAGER = new MediaManager(context);
            }
        }
        return MEDIAMANAGER;
    }

    public HashSet<SongInfo> refreshData() {
        if (songs == null)
            songs = new HashSet<>();
        else
            songs.clear();

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        while (cursor.moveToNext()) {
            SongInfo song = new SongInfo();
            song.setAlbum_id(cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_ID)));
            song.setAlbum_path(getAlbumArtPicPath(song.getAlbum_id()));
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
    private String getAlbumArtPicPath(String albumId) {
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Cursor cur = context.getContentResolver().query(Uri.parse("content://media" +
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId), projection, null, null,
                null);
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();
        return imagePath;
    }

    public SongInfo getSongInfo(@NonNull Song song) {
        check();
        SongInfo info = null;
        for (SongInfo song1 : songs) {
            info = song1;
            if (info.getData().equals(song.path)) {
                break;
            }
        }
        return info;
    }

    public SongInfo getSongInfo(@NonNull String path) {
        return getSongInfo(new Song(path));
    }

    public List<Song> getSongList() {
        check();
        List<Song> songInfos = new ArrayList<>();
        for (SongInfo song : songs) {
            songInfos.add(new Song(song.getData()));
        }
        return songInfos;
    }

    public List<SongInfo> getSongInfoList() {
        check();
        List<SongInfo> songInfos = new ArrayList<>();
        for (SongInfo song : songs) {
            songInfos.add(song);
        }
        return songInfos;
    }

    private void check() {
        if (songs == null)
            refreshData();
    }

    public void scanSdCard(@Nullable MediaScannerConnection.OnScanCompletedListener listener) {

        MediaScannerConnection.scanFile(context, new String[]{Environment
                .getExternalStorageDirectory().getAbsolutePath()}, null, listener);

    }

    /**
     * 检查媒体库是否为空
     *
     * @param refresh 是否要重新获取数据之后再确定，这个过程可能比较耗时
     * @return 为空返回 true
     */
    public boolean emptyMediaLibrary(boolean refresh) {
        if (refresh) {
            refreshData();
        } else {
            check();
        }

        if (songs.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
