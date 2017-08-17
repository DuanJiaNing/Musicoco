package com.duan.musicoco.main.leftnav.timesleep;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;

public class TimeSleepActivity extends RootActivity implements ThemeChangeable {


    private int time;
    private ViewHolder holder;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timme_sleep);

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        holder = new ViewHolder();

        initViews();
        themeChange(null, null);
        initData();
    }

    private void initData() {
        //TODO
        time = 10;
        boolean enable = true;
        holder.initData(enable);
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.time_sleep_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        holder.initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                handleSave();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSave() {

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        final int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }

        int acc = appPreference.getAccentColor();
//        holder.custom.setHighlightColor(acc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.custom.setBackgroundTintList(ColorStateList.valueOf(acc));
        } else {
            holder.custom.setDrawingCacheBackgroundColor(acc);
        }
    }

    private class ViewHolder implements
            CompoundButton.OnCheckedChangeListener {
        TextView status;
        Switch cwitch;
        TextView m10;
        TextView m20;
        TextView m30;
        TextView m45;
        TextView m60;
        Button custom;
        TextView show;

        View[] vs;

        final int color;

        public ViewHolder() {
            color = appPreference.getAccentColor();
        }

        View.OnClickListener checkListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = tagToInt(v);

                int dur = getResources().getInteger(R.integer.anim_default_duration) * 2 / 3;
                AnimationUtils.startScaleAnim(v, dur, null, 0.7f, 1.0f);

                updateText();
                updateSelected();
            }
        };

        View.OnClickListener customListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        void initViews() {
            status = (TextView) findViewById(R.id.time_sleep_status);
            cwitch = (Switch) findViewById(R.id.time_sleep_status_switch);
            m10 = (TextView) findViewById(R.id.time_sleep_10m);
            m20 = (TextView) findViewById(R.id.time_sleep_20m);
            m30 = (TextView) findViewById(R.id.time_sleep_30m);
            m45 = (TextView) findViewById(R.id.time_sleep_45m);
            m60 = (TextView) findViewById(R.id.time_sleep_60m);
            custom = (Button) findViewById(R.id.time_sleep_custom);
            show = (TextView) findViewById(R.id.time_sleep_custom_show);

            vs = new View[]{
                    m10,
                    m20,
                    m30,
                    m45,
                    m60,
            };

            initTexts();

            cwitch.setOnCheckedChangeListener(this);
            m10.setClickable(true);
            m10.setOnClickListener(checkListener);
            m20.setClickable(true);
            m20.setOnClickListener(checkListener);
            m30.setClickable(true);
            m30.setOnClickListener(checkListener);
            m45.setClickable(true);
            m45.setOnClickListener(checkListener);
            m60.setClickable(true);
            m60.setOnClickListener(checkListener);

            custom.setOnClickListener(customListener);

        }

        private void initTexts() {
            String s = getString(R.string.minute);
            int dur = getResources().getInteger(R.integer.anim_default_duration);
            for (View v : vs) {
                ((TextView) v).setText(tagToInt(v) + s);
                AnimationUtils.startScaleAnim(v, dur, null, 0.0f, 1.0f);
                AnimationUtils.startAlphaAnim(v, dur, null, 0.0f, 1.0f);
            }
        }

        private void initData(boolean enable) {
            cwitch.setChecked(enable);
            updateText();
            updateSelected();
        }

        void setEnable(boolean enable) {
            String en = enable ? getString(R.string.enable) : getString(R.string.disable);
            status.setText(en);
            for (View v : vs) {
                v.setEnabled(enable);
            }
            custom.setEnabled(enable);
            show.setEnabled(enable);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setEnable(isChecked);
        }

        private void updateSelected() {
            for (View view : vs) {
                int tag = tagToInt(view);
                if (tag == time) {
                    view.setBackgroundColor(color);
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }

        void updateText() {
            String txt = getString(R.string.replace_time_sleep);
            String s = txt.replace("*", String.valueOf(time));
            show.setText(s);
        }

        int tagToInt(View v) {
            String tag = (String) v.getTag();
            int r = 0;
            if (tag != null && !TextUtils.isEmpty(tag)) {
                r = Integer.valueOf(tag);
            }
            return r;
        }
    }
}
