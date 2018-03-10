package com.duan.musicoco.app;

import android.app.Application;
import android.content.pm.PackageManager;

import com.duan.musicoco.preference.SettingPreference;
import com.duan.musicoco.setting.AutoSwitchThemeController;
import com.duan.musicoco.util.Utils;
import com.xiaomi.ad.AdSdk;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        checkAutoThemeSwitch();

        initXiaomiAd();

    }

    //初始化小米流量变现服务
    private void initXiaomiAd() {

        AdSdk.setDebugOn();

        try {

            String APPID = Utils.getApplicationMetaData(this, "XIAOMI_APPID");
            AdSdk.initialize(this, APPID);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
