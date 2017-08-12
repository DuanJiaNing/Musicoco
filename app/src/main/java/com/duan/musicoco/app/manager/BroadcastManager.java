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

    public static final String FILTER_MY_SHEET_CHANGED = "filter_my_sheet_changed";
    public static final String FILTER_MAIN_DATA_UPDATE = "filter_main_data_update";
    public static final String FILTER_MAIN_SHEET_CHANGED = "filter_main_sheet_changed";
    public static final String FILTER_PLAY_SHEET_RANDOM = "filter_play_sheet_random";
    public static final String FILTER_SHEET_DETAIL_SONGS_CHANGE = "filter_sheet_detail_songs_change";

    // 夜间日间模式切换，主题色定制
    public static final String FILTER_APP_THEME_CHANGE = "filter_app_theme_change";

    private Context context;

    private static BroadcastManager mInstance;

    private BroadcastManager(Context context) {
        this.context = context;
    }

    public static BroadcastManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BroadcastManager(context);
        }
        return mInstance;
    }

    /**
     * 注册广播接收器
     */
    public void registerBroadReceiver(BroadcastReceiver receiver, String identity) {
        IntentFilter filter = new IntentFilter(identity);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 发送广播
     */
    public void sendMyBroadcast(String identity, @Nullable Bundle extras) {
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
    public void unregisterReceiver(BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

}
