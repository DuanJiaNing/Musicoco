package com.duan.musicoco.app;

import android.animation.PropertyValuesHolder;
import android.app.Application;
import android.content.Context;

import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayBackgroundModeEnum;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    //FIXME 内存泄漏
    private AppPreference appPreference;
    private PlayPreference playPreference;
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

        appPreference.updateTheme(ThemeEnum.DARK);
        playPreference.updateTheme(ThemeEnum.DARK);
        playPreference.updatePlayBgMode(PlayBackgroundModeEnum.PICTUREWITHBLUR);

    }

    public static Context getContext() {
        return sCONTEXT;
    }

    public AppPreference getAppPreference() {
        return appPreference;
    }
}
