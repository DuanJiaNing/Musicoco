package com.duan.musicoco.app;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.duan.musicoco.BuildConfig;
import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;

public class AboutActivity extends RootActivity implements
        ThemeChangeable, View.OnClickListener {

    private Toolbar toolbar;
    private TextView version;
    private View container;
    private TextView guide;
    private TextView star;
    private TextView share;
    private TextView name;

    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        activityManager = ActivityManager.getInstance();

        initViews();
        themeChange(null, null);
        initData();

    }

    private void initData() {
        version.setText(getString(R.string.app_name_us) + " v " + BuildConfig.VERSION_NAME);
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        version = (TextView) findViewById(R.id.about_version);
        container = findViewById(R.id.about_container);
        guide = (TextView) findViewById(R.id.about_guide);
        star = (TextView) findViewById(R.id.about_star);
        share = (TextView) findViewById(R.id.about_share);
        name = (TextView) findViewById(R.id.about_name);

        guide.setOnClickListener(this);
        star.setOnClickListener(this);
        share.setOnClickListener(this);
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
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int[] cs2 = ColorUtils.get2ActionStatusBarColors(this);
        int actionC = cs2[0];
        int statusC = cs2[1];
        toolbar.setBackgroundColor(statusC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(actionC);
        }

        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);
        int vicBC = theme == ThemeEnum.WHITE ? Color.WHITE : cs[4];
        int mainTC = cs[5];
        int vicTC = cs[6];
        guide.setTextColor(mainTC);
        star.setTextColor(mainTC);
        share.setTextColor(mainTC);
        name.setTextColor(vicTC);
        version.setTextColor(mainTC);
        container.setBackgroundColor(vicBC);

        Drawable d;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            d = getDrawable(R.drawable.ic_navigate_next);
            d.setTint(vicTC);
        } else {
            d = getResources().getDrawable(R.drawable.ic_navigate_next);
        }
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        d.setAlpha(150);
        guide.setCompoundDrawables(null, null, d, null);
        star.setCompoundDrawables(null, null, d, null);
        share.setCompoundDrawables(null, null, d, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_guide:
                activityManager.startWebActivity(this, getString(R.string.guide_url));
                break;
            case R.id.about_star:
                activityManager.startWebActivity(this, getString(R.string.guide_star));
                break;
            case R.id.about_share:
                activityManager.startSystemShare(this, getString(R.string.share_to_friends));
                break;
            default:
                break;
        }
    }
}
