package com.duan.musicoco.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.duan.musicoco.R;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;

/**
 * Created by DuanJiaNing on 2017/8/6.
 */

public class RootActivity extends AppCompatActivity {

    protected final AppPreference appPreference;
    protected final PlayPreference playPreference;
    protected DBMusicocoController dbController;

    public RootActivity() {
        appPreference = new AppPreference(this);
        playPreference = new PlayPreference(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkTheme();
        dbController = new DBMusicocoController(this, true);
    }

    protected void checkTheme() {
        ThemeEnum themeEnum = appPreference.getTheme();
        if (themeEnum == ThemeEnum.DARK) {
            this.setTheme(R.style.Theme_DARK);
        } else {
            this.setTheme(R.style.Theme_WHITE);
        }
    }

}
