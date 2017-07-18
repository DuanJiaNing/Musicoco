package com.duan.musicoco.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 管理广播
 */
public class BroadcastManager {

    public static final String REFRESH_MAIN_ACTIVITY_DATA = "refresh_main_activity_data";

    public static final String REFRESH_PLAY_ACTIVITY_DATA = "refresh_play_activity_data";

    /**
     * 注册广播接收器
     */
    static public void registerBroadReceiver(Context context, BroadcastReceiver receiver, String identity) {
        IntentFilter filter = new IntentFilter(identity);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 发送广播
     */
    static public void sendMyBroadcast(Context context, String identity) {
        Intent intent = new Intent();
        intent.setAction(identity);
        context.sendBroadcast(intent);
    }

    /**
     * 注销广播接收者
     */
    static public void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }
}
