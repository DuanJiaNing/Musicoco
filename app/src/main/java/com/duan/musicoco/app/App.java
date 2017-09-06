package com.duan.musicoco.app;

import android.app.Application;

import com.duan.musicoco.preference.SettingPreference;
import com.duan.musicoco.setting.AutoSwitchThemeController;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        checkAutoThemeSwitch();
    }

    private void checkAutoThemeSwitch() {
        SettingPreference settingPreference = new SettingPreference(this);
        AutoSwitchThemeController instance = AutoSwitchThemeController.getInstance(this);
        if (settingPreference.autoSwitchNightTheme() && !instance.isSet()) {
            instance.setAlarm();
        } else {
            instance.cancelAlarm();
        }
    }
}
