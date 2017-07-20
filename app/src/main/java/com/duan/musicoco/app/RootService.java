package com.duan.musicoco.app;

import android.app.Service;

import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;

/**
 * Created by DuanJiaNing on 2017/6/24.
 */

public abstract class RootService extends Service {

    protected MediaManager mediaManager;
    protected final PlayPreference playPreference;
    protected final AppPreference appPreference;
    protected DBMusicocoController dbController;

    public RootService() {
        this.playPreference = new PlayPreference(this);
        this.appPreference = new AppPreference(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.dbController = new DBMusicocoController(this, false);
        this.mediaManager = MediaManager.getInstance(getApplicationContext());

    }
}
