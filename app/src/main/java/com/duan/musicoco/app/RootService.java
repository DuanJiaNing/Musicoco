package com.duan.musicoco.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;

/**
 * Created by DuanJiaNing on 2017/6/24.
 */

public abstract class RootService extends Service {

    protected MediaManager mediaManager;
    protected final PlayPreference playPreference;
    protected final AppPreference appPreference;

    public RootService() {
        this.playPreference = new PlayPreference(this);
        appPreference = new AppPreference(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaManager = MediaManager.getInstance(getApplicationContext());

    }
}
