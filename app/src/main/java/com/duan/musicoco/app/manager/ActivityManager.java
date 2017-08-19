package com.duan.musicoco.app.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.AboutActivity;
import com.duan.musicoco.app.FeedBackActivity;
import com.duan.musicoco.detail.sheet.SheetDetailActivity;
import com.duan.musicoco.detail.song.SongDetailActivity;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.main.leftnav.imagewall.ImageWallActivity;
import com.duan.musicoco.main.leftnav.play.PlayThemeCustomActivity;
import com.duan.musicoco.setting.SettingsActivity;
import com.duan.musicoco.main.leftnav.timesleep.TimeSleepActivity;
import com.duan.musicoco.sheetmodify.SheetModifyActivity;
import com.duan.musicoco.play.PlayActivity;
import com.duan.musicoco.rmp.RecentMostPlayActivity;
import com.duan.musicoco.search.SearchActivity;
import com.duan.musicoco.main.leftnav.themecolor.ThemeColorCustomActivity;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class ActivityManager {

    private Context context;
    public static final String SONG_DETAIL_PATH = "song_detail_path";
    public static final String SHEET_MODIFY_ID = "sheet_modify_id";
    public static final String SHEET_SEARCH_ID = "sheet_search_id";

    public static final String SHEET_DETAIL_ID = "sheet_detail_id";
    public static final String SHEET_DETAIL_LOCATION_AT = "sheet_detail_location_at";

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
        //FIXME android N 以下正常，N 报 FileUriExposedException
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
        context.startActivity(intent);
    }

    /**
     * locationAt: 显示列表时滚动到，不需要滚动传 -1 即可
     */
    public void startSheetDetailActivity(int sheetID, Song locationAt) {
        Intent intent = new Intent(context, SheetDetailActivity.class);
        intent.putExtra(SHEET_DETAIL_ID, sheetID);
        if (locationAt != null) {
            intent.putExtra(SHEET_DETAIL_LOCATION_AT, locationAt);
        }
        context.startActivity(intent);
    }

    public void startSheetModifyActivity(int sheetID) {
        Intent intent = new Intent(context, SheetModifyActivity.class);
        intent.putExtra(SHEET_MODIFY_ID, sheetID);
        context.startActivity(intent);
    }

    public void startSearchActivity(int sheetID) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SHEET_SEARCH_ID, sheetID);
        context.startActivity(intent);
    }

    public void startPlayActivity() {
        context.startActivity(new Intent(context, PlayActivity.class));
    }

    public void startRecentMostPlayActivity() {
        context.startActivity(new Intent(context, RecentMostPlayActivity.class));
    }

    public void startMainActivity() {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public void startThemeColorCustomActivity() {
        context.startActivity(new Intent(context, ThemeColorCustomActivity.class));
    }

    public void startImageWallActivity() {
        context.startActivity(new Intent(context, ImageWallActivity.class));
    }

    public void startTimeSleepActivity() {
        context.startActivity(new Intent(context, TimeSleepActivity.class));
    }

    public void startPlayThemeCustomActivity() {
        context.startActivity(new Intent(context, PlayThemeCustomActivity.class));
    }

    public void startSettingActivity() {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void startFeedBackActivity() {
        context.startActivity(new Intent(context, FeedBackActivity.class));
    }

    public void startAboutActivity() {
        context.startActivity(new Intent(context, AboutActivity.class));
    }
}
