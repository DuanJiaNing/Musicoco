package com.duan.musicoco.app;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;

public class AboutActivity extends RootActivity implements
        ThemeChangeable, View.OnClickListener {

    private Toolbar toolbar;
    private TextView version;
    private View container;
    private TextView guide;
    private TextView welcome;
    private TextView star;
    private TextView me;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
        themeChange(null, null);

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        version = (TextView) findViewById(R.id.about_version);
        container = findViewById(R.id.about_container);
        guide = (TextView) findViewById(R.id.about_guide);
        welcome = (TextView) findViewById(R.id.about_welcome);
        star = (TextView) findViewById(R.id.about_star);
        me = (TextView) findViewById(R.id.about_me);
        name = (TextView) findViewById(R.id.about_name);

        guide.setOnClickListener(this);
        welcome.setOnClickListener(this);
        star.setOnClickListener(this);
        me.setOnClickListener(this);
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
        welcome.setTextColor(mainTC);
        star.setTextColor(mainTC);
        me.setTextColor(mainTC);
        name.setTextColor(vicTC);
        version.setTextColor(mainTC);
        container.setBackgroundColor(vicBC);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_guide:

                break;
            case R.id.about_welcome:

                break;
            case R.id.about_star:

                break;
            case R.id.about_me:

                break;
            default:
                break;
        }
    }
}
