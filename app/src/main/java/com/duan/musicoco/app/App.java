package com.duan.musicoco.app;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayBackgroundModeEnum;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    private PlayPreference playPreference;
    private AppPreference appPreference;

    //FIXME 内存泄漏
    private static Context sCONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        sCONTEXT = this;
        appPreference = new AppPreference(this);
        playPreference = new PlayPreference(this);

        //FIXME test
        test();
    }

    private void test() {

        int color = Color.parseColor("#2979FF");
        int color1 = Color.parseColor("#2979FF");
        int color2 = Color.parseColor("#212121");
        appPreference.updateActionbarColor(color);
        appPreference.updateStatusBarColor(color1);
        appPreference.updateAccentColor(color2);

        appPreference.updateTheme(ThemeEnum.WHITE);
        playPreference.updateTheme(ThemeEnum.VARYING);
        playPreference.updatePlayBgMode(PlayBackgroundModeEnum.GRADIENT_COLOR);

    }

    public static Context getContext() {
        return sCONTEXT;
    }

    public AppPreference getAppPreference() {
        return appPreference;
    }
}
