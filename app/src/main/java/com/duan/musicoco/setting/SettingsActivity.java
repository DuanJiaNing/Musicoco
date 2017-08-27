package com.duan.musicoco.setting;


import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.DialogProvider;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;

import java.io.IOException;

public class SettingsActivity extends RootActivity implements ThemeChangeable, View.OnClickListener {

    private Toolbar toolbar;
    private TextView feedBack;
    private TextView about;
    private TextView aboutMe;
    private TextView clearCache;

    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        activityManager = ActivityManager.getInstance();
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

        feedBack = (TextView) findViewById(R.id.setting_feedback);
        about = (TextView) findViewById(R.id.setting_about);
        aboutMe = (TextView) findViewById(R.id.setting_about_me);
        clearCache = (TextView) findViewById(R.id.setting_clear_cache);
        feedBack.setOnClickListener(this);
        about.setOnClickListener(this);
        aboutMe.setOnClickListener(this);
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
        int vicBC = cs[4];
        int accentC = cs[2];
        int bc = theme == ThemeEnum.WHITE ? Color.WHITE : vicBC;

        feedBack.setTextColor(mainTC);
        about.setTextColor(mainTC);
        aboutMe.setTextColor(mainTC);
        clearCache.setTextColor(accentC);

        findViewById(R.id.setting_container).setBackgroundColor(bc);
        findViewById(R.id.fragment_container).setBackgroundColor(bc);
        clearCache.setBackgroundColor(bc);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_feedback:
                activityManager.startFeedBackActivity(this);
                break;
            case R.id.setting_about:
                activityManager.startAboutActivity(this);
                break;
            case R.id.setting_clear_cache:
                handleClearCache();
                break;
            case R.id.setting_about_me:
                activityManager.startMeActivity(this);
                break;
            default:
                break;

        }
    }

    private void handleClearCache() {
        DialogProvider provider = new DialogProvider(this);
        Dialog dialog = provider.createPromptDialog(
                getString(R.string.tip),
                getString(R.string.info_cleat_cache),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearCache();
                        String msg = SettingsActivity.this.getString(R.string.success_clear_cache);
                        ToastUtils.showShortToast(msg, SettingsActivity.this);
                    }
                }, null, true);
        dialog.show();

    }

    private void clearCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 清除 Glide 缓存
                Glide.get(SettingsActivity.this).clearDiskCache();

                // 清除播放页专辑图片(圆形旋转图片)缓存
                try {
                    BitmapCache bc = new BitmapCache(SettingsActivity.this, BitmapCache.CACHE_ALBUM_VISUALIZER_IMAGE);
                    bc.getCacheControl().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
