package com.duan.musicoco.app.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

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
import com.duan.musicoco.main.leftnav.themecolor.ThemeColorCustomActivity;
import com.duan.musicoco.main.leftnav.timesleep.TimeSleepActivity;
import com.duan.musicoco.play.PlayActivity;
import com.duan.musicoco.rmp.RecentMostPlayActivity;
import com.duan.musicoco.search.SearchActivity;
import com.duan.musicoco.setting.SettingsActivity;
import com.duan.musicoco.sheetmodify.SheetModifyActivity;
import com.duan.musicoco.util.ToastUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class ActivityManager {

    public static final String SONG_DETAIL_PATH = "song_detail_path";
    public static final String SONG_DETAIL_START_FROM_PLAY_ACTIVITY = "song_detail_start_from_play_activity";


    public static final String SHEET_MODIFY_ID = "sheet_modify_id";
    public static final String SHEET_SEARCH_ID = "sheet_search_id";

    public static final String SHEET_DETAIL_ID = "sheet_detail_id";
    public static final String SHEET_DETAIL_LOCATION_AT = "sheet_detail_location_at";

    public static final String WEB_URL = "web_url";

    private static ActivityManager mInstance;

    private final Map<String, Activity> activitys = new HashMap<>();

    private ActivityManager() {
    }

    public Activity getActivity(String name) {
        return activitys.get(name);
    }

    public void addActivity(Activity activity) {
        activitys.put(activity.getClass().getName(), activity);
    }

    public static ActivityManager getInstance() {
        if (mInstance == null) {
            mInstance = new ActivityManager();
        }
        return mInstance;
    }

    /**
     * @param whichSong           歌曲
     * @param startByPlayActivity PlayActivity 在单独的 activity 栈中，而 SongDetailActivity 不与其在同一个栈，因而此时
     *                            在 SongDetailActivity 中退出时会直接跳过 PlayActivity(即使时序上为在 PlayActivity 中
     *                            启动 SongDetailActivity，SongDetailActivity 退出时理应回到 PlayActivity)
     */
    public void startSongDetailActivity(Context context, Song whichSong, boolean startByPlayActivity) {
        Intent intent = new Intent(context, SongDetailActivity.class);
        intent.putExtra(SONG_DETAIL_PATH, whichSong.path);
        intent.putExtra(SONG_DETAIL_START_FROM_PLAY_ACTIVITY, startByPlayActivity);
        context.startActivity(intent);
    }

    public void startImageCheckActivity(Context context, String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        File file = new File(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 适配 N (解决FileUriExposedException)
            // 见：https://my.oschina.net/shenhuniurou/blog/870156
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }


    public void startSystemBrowser(Context context, @NonNull String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public void startSystemShare(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    /**
     * locationAt: 显示列表时滚动到，不需要滚动传 -1 即可
     */
    public void startSheetDetailActivity(Context context, int sheetID, Song locationAt) {
        Intent intent = new Intent(context, SheetDetailActivity.class);
        intent.putExtra(SHEET_DETAIL_ID, sheetID);
        if (locationAt != null) {
            intent.putExtra(SHEET_DETAIL_LOCATION_AT, locationAt);
        }
        context.startActivity(intent);
    }

    public void startSheetModifyActivity(Context context, int sheetID) {
        Intent intent = new Intent(context, SheetModifyActivity.class);
        intent.putExtra(SHEET_MODIFY_ID, sheetID);
        context.startActivity(intent);
    }

    public void startSearchActivity(Context context, int sheetID) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SHEET_SEARCH_ID, sheetID);
        context.startActivity(intent);
    }

    public void startPlayActivity(Context context) {
        context.startActivity(new Intent(context, PlayActivity.class));
    }

    public void startRecentMostPlayActivity(Context context) {
        context.startActivity(new Intent(context, RecentMostPlayActivity.class));
    }

    public void startMainActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public void startThemeColorCustomActivity(Context context) {
        context.startActivity(new Intent(context, ThemeColorCustomActivity.class));
    }

    public void startImageWallActivity(Context context) {
        context.startActivity(new Intent(context, ImageWallActivity.class));
    }

    public void startTimeSleepActivity(Context context) {
        context.startActivity(new Intent(context, TimeSleepActivity.class));
    }

    public void startPlayThemeCustomActivity(Context context) {
        context.startActivity(new Intent(context, PlayThemeCustomActivity.class));
    }

    public void startSettingActivity(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

    public void startFeedBackActivity(Context context) {
        context.startActivity(new Intent(context, FeedBackActivity.class));
    }

    public void startAboutActivity(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    public void startWebActivity(Context context, @NonNull String url) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(WEB_URL, url);
        context.startActivity(intent);
    }

    public void startMeActivity(Context context) {
        context.startActivity(new Intent(context, MeActivity.class));
    }

    public void startQQChartPanel(Context context, String qqId) {
        try {
            //可以跳转到添加好友，如果qq号是好友了，直接聊天
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qqId;//uin是发送过去的qq号码
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = context.getString(R.string.error_check_qq_install);
            ToastUtils.showShortToast(msg, context);
        }
    }

    public void startSystemPhoneCallPanel(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startSystemEmailPanel(Context context, String email) {
        Uri uri = Uri.parse("mailto:" + email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, ""); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.info_choice_email_app)));
    }
}
