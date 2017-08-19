package com.duan.musicoco.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by DuanJiaNing on 2017/8/19.
 * 应用偏好设置：打开应用自动播放，记忆播放，自动切换夜间模式，耳机线控
 * 由 SettingFragment 进行管理
 */

public class SettingPreference {

    private final SharedPreferences preferences;
    private Context context;

    public SettingPreference(Context context) {
        this.context = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean openAutoPlay() {
        return preferences.getBoolean("pre_auto_play", false);
    }

    public boolean memoryPlay() {
        return preferences.getBoolean("pre_memory_play", true);
    }

    public boolean autoSwitchNightTheme() {
        return preferences.getBoolean("pre_auto_switch_night_theme", true);
    }

    public boolean preHeadphoneWire() {
        return preferences.getBoolean("pre_headphone_wire", true);
    }

}
