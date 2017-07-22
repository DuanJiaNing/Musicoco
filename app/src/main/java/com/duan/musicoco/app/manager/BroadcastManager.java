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
    public static final String FILTER_MAIN_SHEET_CHANGED = "filter_main_sheet_changed";

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
    static public void sendMyBroadcast(Context context, String identity, @Nullable Bundle extras) {
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
    static public void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }
}
