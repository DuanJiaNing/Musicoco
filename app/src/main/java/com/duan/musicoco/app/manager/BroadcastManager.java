package com.duan.musicoco.app.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 管理广播
 */
public class BroadcastManager {


    // 主界面【主歌单】信息更新(主要针对歌曲收藏状态)
    public static final String FILTER_MAIN_SHEET_UPDATE = "filter_main_sheet_update";

    // 主界面【我的歌单】信息更新
    public static final String FILTER_MY_SHEET_UPDATE = "filter_my_sheet_update";

    // 歌单详情界面信息更新
    public static final String FILTER_SHEET_DETAIL_SONGS_UPDATE = "filter_sheet_detail_songs_update";

    //服务器退出
    public static final String FILTER_PLAY_SERVICE_QUIT = "filter_play_service_quit";


    //播放界面界面主题改变
    public static final String FILTER_PLAY_UI_MODE_CHANGE = "filter_play_ui_mode_change";

    public static final class Play {
        public static final String PLAY_THEME_CHANGE_TOKEN = "play_theme_change_token";
        public static final int PLAY_APP_THEME_CHANGE = 1;
        public static final int PLAY_PLAY_THEME_CHANGE = 2;
    }


    // 定时关闭应用时在主界面左边导航栏显示倒计时
    public static final String FILTER_APP_QUIT_TIME_COUNTDOWN = "filter_app_quit_time_countdown";

    public static final class Countdown {
        public static final String APP_QUIT_TIME_COUNTDOWN_STATUS = "app_quit_time_countdown_status";
        public static final int START_COUNTDOWN = 1;
        public static final int STOP_COUNTDOWN = 2;
    }

    // 应用主题自动切换
    public static final String FILTER_APP_THEME_CHANGE_AUTOMATIC = "filter_app_theme_change_automatic";
    public static final String APP_THEME_CHANGE_AUTOMATIC_TOKEN = "filter_app_theme_change_automatic_token";
    public static final int APP_THEME_CHANGE_AUTOMATIC_WHITE = 1;
    public static final int APP_THEME_CHANGE_AUTOMATIC_DARK = 2;

    // 耳机插入和拔出事件
    public static final String FILTER_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";

    private static BroadcastManager mInstance;

    private BroadcastManager() {
    }

    public static BroadcastManager getInstance() {
        if (mInstance == null) {
            mInstance = new BroadcastManager();
        }
        return mInstance;
    }

    /**
     * 注册广播接收器
     */
    public void registerBroadReceiver(Context context, BroadcastReceiver receiver, String identity) {
        IntentFilter filter = new IntentFilter(identity);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 发送广播
     */
    public void sendBroadcast(Context context, String identity, @Nullable Bundle extras) {
        Intent intent = new Intent();
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.setAction(identity);
        context.sendBroadcast(intent);
    }

    /**
     * 注销广播接收者
     */
    public void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

}
