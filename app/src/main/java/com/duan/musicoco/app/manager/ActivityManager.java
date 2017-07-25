package com.duan.musicoco.app.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.detail.sheet.SheetDetailActivity;
import com.duan.musicoco.detail.song.SongDetailActivity;
import com.duan.musicoco.play.PlayActivity;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class ActivityManager {

    private Context context;
    public static final String SONG_DETAIL_PATH = "song_detail_path";
    public static final String SHEET_DETAIL_ID = "sheet_detail_id";

    private static ActivityManager mInstance;

    private ActivityManager(Context context) {
        this.context = context;
    }

    public static ActivityManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ActivityManager(context);
        }
        return mInstance;
    }

    public void startSongDetailActivity(Song whichSong) {
        Intent intent = new Intent(context, SongDetailActivity.class);
        intent.putExtra(SONG_DETAIL_PATH, whichSong.path);
        context.startActivity(intent);
    }

    public void startImageCheckActivity(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
        context.startActivity(intent);
    }

    public void startSheetDetailActivity(int sheetID) {
        Intent intent = new Intent(context, SheetDetailActivity.class);
        intent.putExtra(SHEET_DETAIL_ID, sheetID);
        context.startActivity(intent);
    }

    public void startPlayActivity() {
        context.startActivity(new Intent(context, PlayActivity.class));
    }
}
