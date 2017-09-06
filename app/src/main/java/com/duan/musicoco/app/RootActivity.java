package com.duan.musicoco.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.AuxiliaryPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.SettingPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

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

        ActivityManager.getInstance().addActivity(this);
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

        // NOTICE: 2017/9/6 注意 修复内存泄漏
        ActivityManager.getInstance().removeActivity(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Init.xiaomiStatisticalervicesInitSuccess) {
            MiStatInterface.recordPageStart(this, this.getClass().getName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Init.xiaomiStatisticalervicesInitSuccess) {
            MiStatInterface.recordPageEnd();
        }
    }
}
