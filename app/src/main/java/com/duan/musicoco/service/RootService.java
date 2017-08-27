package com.duan.musicoco.service;

import android.app.Service;

import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.SettingPreference;

/**
 * Created by DuanJiaNing on 2017/6/24.
 */

public abstract class RootService extends Service {

    protected DBMusicocoController dbController;
    protected MediaManager mediaManager;

    protected PlayPreference playPreference;
    protected AppPreference appPreference;
    protected SettingPreference settingPreference;

    public RootService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.playPreference = new PlayPreference(this);
        this.appPreference = new AppPreference(this);
        this.settingPreference = new SettingPreference(this);
        this.dbController = new DBMusicocoController(this, false);
        this.mediaManager = MediaManager.getInstance();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dbController != null) {
            dbController.close();
        }
    }
}
