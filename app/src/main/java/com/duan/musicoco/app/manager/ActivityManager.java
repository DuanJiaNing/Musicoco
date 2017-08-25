package com.duan.musicoco.app.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.ExploreByTouchHelper;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.AboutActivity;
import com.duan.musicoco.app.FeedBackActivity;
import com.duan.musicoco.app.MeActivity;
import com.duan.musicoco.app.WebActivity;
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
import com.duan.musicoco.util.ToastUtils;

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

    public static final String WEB_URL = "web_url";

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


    public void startSystemBrower(@NonNull String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public void startSystemShare(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
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

    public void startWebActivity(@NonNull String url) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(WEB_URL, url);
        context.startActivity(intent);
    }

    public void startMeActivity() {
        context.startActivity(new Intent(context, MeActivity.class));
    }

    public void startQQChartPanel(String qqId) {
        try {
            //可以跳转到添加好友，如果qq号是好友了，直接聊天
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qqId;//uin是发送过去的qq号码
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = context.getString(R.string.error_check_qq_install);
            ToastUtils.showShortToast(msg);
        }
    }

    public void startSystemPhoneCallPanel(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startSystemEmailPanel(String email) {
        Uri uri = Uri.parse("mailto:" + email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, ""); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.info_choice_email_app)));
    }
}
