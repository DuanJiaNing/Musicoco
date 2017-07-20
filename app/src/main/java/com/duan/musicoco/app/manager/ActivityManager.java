package com.duan.musicoco.app.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.detail.SongDetailActivity;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class ActivityManager {

    private Context context;

    public static final String SONG_DETAIL = "song";

    public ActivityManager(Context context) {
        this.context = context;
    }

    public void startSongDetailActivity(Song whichSong) {
        Intent intent = new Intent(context, SongDetailActivity.class);
        intent.putExtra(SONG_DETAIL, whichSong.path);
        context.startActivity(intent);
    }

    public void startImageCheckActivity(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
        context.startActivity(intent);
    }
}
