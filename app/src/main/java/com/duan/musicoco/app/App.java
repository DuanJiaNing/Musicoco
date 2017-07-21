package com.duan.musicoco.app;

import android.app.Application;

import com.duan.musicoco.preference.AppPreference;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class App extends Application {

    public final AppPreference appPreference;

    public App() {
        appPreference = new AppPreference(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }


}
