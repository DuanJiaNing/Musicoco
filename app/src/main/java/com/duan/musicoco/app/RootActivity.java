package com.duan.musicoco.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.duan.musicoco.R;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.AuxiliaryPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.SettingPreference;
import com.duan.musicoco.preference.ThemeEnum;

/**
 * Created by DuanJiaNing on 2017/8/6.
 * 检查主题，打开获得数据库连接
 */

public class RootActivity extends AppCompatActivity {

    protected AppPreference appPreference;
    protected PlayPreference playPreference;
    protected SettingPreference settingPreference;
    protected AuxiliaryPreference auxiliaryPreference;

    protected DBMusicocoController dbController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appPreference = new AppPreference(this);
        playPreference = new PlayPreference(this);
        auxiliaryPreference = new AuxiliaryPreference(this);
        settingPreference = new SettingPreference(this);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbController != null) {
            dbController.close();
        }

    }
}
