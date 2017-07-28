package com.duan.musicoco.app;

import android.animation.PropertyValuesHolder;
import android.app.Application;
import android.content.Context;

import com.duan.musicoco.preference.AppPreference;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    //FIXME 内存泄漏
    private AppPreference appPreference;
    private static Context sCONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        sCONTEXT = this;
        appPreference = new AppPreference(this);
    }

    public static Context getContext() {
        return sCONTEXT;
    }

    public AppPreference getAppPreference() {
        return appPreference;
    }
}
