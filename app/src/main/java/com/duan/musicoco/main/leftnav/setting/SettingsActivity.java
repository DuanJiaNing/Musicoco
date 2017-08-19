package com.duan.musicoco.main.leftnav.setting;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;

public class SettingsActivity extends RootActivity implements ThemeChangeable, View.OnClickListener {

    private Toolbar toolbar;
    private TextView feedBack;
    private TextView about;
    private TextView clearCache;
    private View fragmentContainer;

    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        activityManager = ActivityManager.getInstance(this);
        initViews();
        themeChange(null, null);
        initData();
    }

    private void initData() {

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentContainer = findViewById(R.id.fragment_container);
        feedBack = (TextView) findViewById(R.id.setting_feedback);
        about = (TextView) findViewById(R.id.setting_about);
        clearCache = (TextView) findViewById(R.id.setting_clear_cache);
        feedBack.setOnClickListener(this);
        about.setOnClickListener(this);
        clearCache.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void checkTheme() {
        ThemeEnum themeEnum = appPreference.getTheme();
        if (themeEnum == ThemeEnum.DARK) {
            this.setTheme(R.style.Theme_DARK);
        } else {
            this.setTheme(R.style.AppTheme_Setting);
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        final int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }

        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);
        int mainTC = cs[5];
        int vicTC = cs[6];
        int accentC = cs[2];
        int bc = theme == ThemeEnum.WHITE ? Color.WHITE : vicTC;

        feedBack.setTextColor(mainTC);
        about.setTextColor(mainTC);
        clearCache.setTextColor(accentC);

        fragmentContainer.setBackgroundColor(bc);
        feedBack.setBackgroundColor(bc);
        about.setBackgroundColor(bc);
        clearCache.setBackgroundColor(bc);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_feedback:
                activityManager.startFeedBackActivity();
                break;
            case R.id.setting_about:
                activityManager.startAboutActivity();
                break;
            case R.id.setting_clear_cache:
                handleClearCache();
                break;
            default:
                break;

        }
    }

    private void handleClearCache() {

    }
}
