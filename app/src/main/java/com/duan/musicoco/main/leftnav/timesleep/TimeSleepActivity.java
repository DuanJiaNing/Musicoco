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
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;

public class TimeSleepActivity extends RootActivity implements ThemeChangeable {

    private ViewHolder viewHolder;
    private NumberPickerHolder numberPickerHolder;
    private Toolbar toolbar;
    private boolean enable;
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_sleep);

        initToolbar();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
        numberPickerHolder = null;
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        viewHolder = new ViewHolder();
        numberPickerHolder = new NumberPickerHolder();

        viewHolder.initViews();
        themeChange(null, null);
        initData();
    }

    private void initData() {
        // 当且仅当已经设置了定时关闭而且时间还没到才为 true
        enable = auxiliaryPreference.getTimeSleepEnable();
        if (enable) {
            time = auxiliaryPreference.getTimeSleepDuration();
        } else {
            time = 10;
        }
        viewHolder.initData(enable);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.time_sleep_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }
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
                if (time == 0) {
                    String msg = getString(R.string.error_time_sleep_time_must_more_then_zero);
                    ToastUtils.showShortToast(msg, this);
                } else {
                    if (enable) {
                        handleSave();
                    } else {
                        handleCancelIfEnableBefore();
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleCancelIfEnableBefore() {
        boolean enableBefore = auxiliaryPreference.getTimeSleepEnable();
        if (enableBefore) {
            Bundle bundle = new Bundle();
            bundle.putInt(BroadcastManager.Countdown.APP_QUIT_TIME_COUNTDOWN_STATUS,
                    BroadcastManager.Countdown.STOP_COUNTDOWN);
            BroadcastManager.getInstance()
                    .sendBroadcast(this, BroadcastManager.FILTER_APP_QUIT_TIME_COUNTDOWN, bundle);

            String msg = getString(R.string.info_time_sleep_is_canceled);
            ToastUtils.showShortToast(msg, this);
        }
        finish();
    }

    private void handleSave() {
        auxiliaryPreference.updateTimeSleep(time);

        String ti = getString(R.string.replace_time_sleep_app_quit_at);
        String re;
        if (time > 60) {
            re = time / 60 + " " + getString(R.string.hour) + " " + time % 60 + " " + getString(R.string.minute);
        } else {
            re = time + " " + getString(R.string.minute);
        }
        String str = ti.replace("*", re);
        ToastUtils.showLongToast(this, str);

        Bundle bundle = new Bundle();
        bundle.putInt(BroadcastManager.Countdown.APP_QUIT_TIME_COUNTDOWN_STATUS, BroadcastManager.Countdown.START_COUNTDOWN);
        BroadcastManager.getInstance()
                .sendBroadcast(this, BroadcastManager.FILTER_APP_QUIT_TIME_COUNTDOWN, bundle);

        finish();
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int acc = ColorUtils.getAccentColor(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.custom.setBackgroundTintList(ColorStateList.valueOf(acc));
        } else {
            viewHolder.custom.setDrawingCacheBackgroundColor(acc);
        }

        // checkTheme 的文字颜色没生效，?.?
        int tcs[] = ColorUtils.get2ThemeTextColor(this, appPreference.getTheme());
        int mC = tcs[0];
        viewHolder.status.setTextColor(mC);
        for (View v : viewHolder.vs) {
            ((TextView) v).setTextColor(mC);
        }
        viewHolder.line.setBackgroundColor(tcs[1]);
        viewHolder.show.setTextColor(mC);

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
        View line;

        View[] vs;

        final int color;

        public ViewHolder() {
            color = ColorUtils.getAccentColor(TimeSleepActivity.this);
        }

        View.OnClickListener checkListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time = tagToInt(v);

                int dur = getResources().getInteger(R.integer.anim_default_duration) * 2 / 3;
                AnimationUtils.startScaleAnim(v, dur, null, 0.7f, 1.0f);

                updateCurrentTimeText();
                updateSelected();
            }
        };

        View.OnClickListener customListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!numberPickerHolder.custom) {
                    numberPickerHolder.init();
                    AnimationUtils.startScaleAnim(v, 300, null, 1.0f, 0.0f);
                }
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
            line = findViewById(R.id.time_sleep_line);

            vs = new View[]{
                    m10,
                    m20,
                    m30,
                    m45,
                    m60,
            };

            cwitch.setOnCheckedChangeListener(this);
            m10.setOnClickListener(checkListener);
            m20.setOnClickListener(checkListener);
            m30.setOnClickListener(checkListener);
            m45.setOnClickListener(checkListener);
            m60.setOnClickListener(checkListener);

            custom.setOnClickListener(customListener);

        }

        private void initTexts() {
            String s = getString(R.string.minute);
            int dur = getResources().getInteger(R.integer.anim_default_duration);
            for (View v : vs) {
                ((TextView) v).setText(tagToInt(v) + " " + s);
                AnimationUtils.startScaleAnim(v, dur, null, 0.0f, 1.0f);
                AnimationUtils.startAlphaAnim(v, dur, null, 0.0f, enable ? 1.0f : 0.4f);
            }
        }

        private void initData(boolean enable) {
            cwitch.setChecked(enable);
            initTexts();
            updateCurrentTimeText();
            updateSelected();
            setEnable(enable);
        }

        void setEnable(boolean enable) {
            String en = enable ? getString(R.string.enable) : getString(R.string.disable);
            status.setText(en);
            for (View v : vs) {
                v.setEnabled(enable);
            }
            custom.setEnabled(enable);
            show.setEnabled(enable);

            float alpha = 1.0f;
            if (!enable) {
                alpha = 0.4f;
            }
            for (View v : vs) {
                v.setAlpha(alpha);
            }
            custom.setAlpha(alpha);
            show.setAlpha(alpha);

            if (numberPickerHolder.custom) {
                numberPickerHolder.setEnable(enable);
            }

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            enable = isChecked;
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

        void updateCurrentTimeText() {
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

    private class NumberPickerHolder implements
            NumberPicker.OnValueChangeListener {

        TextView tHour;
        TextView tMinute;
        NumberPicker minute;
        NumberPicker hour;

        boolean custom = false;

        void init() {
            custom = true;
            View v = findViewById(R.id.time_sleep_pickers);
            v.setVisibility(View.VISIBLE);

            tHour = (TextView) findViewById(R.id.time_sleep_custom_num_left_t);
            tMinute = (TextView) findViewById(R.id.time_sleep_custom_num_right_t);
            minute = (NumberPicker) findViewById(R.id.time_sleep_custom_num_right);
            hour = (NumberPicker) findViewById(R.id.time_sleep_custom_num_left);
            minute.setOnValueChangedListener(this);
            hour.setOnValueChangedListener(this);

            initTheme();
            initPickersData();
        }

        private void initPickersData() {
            minute.setMinValue(0);
            hour.setMinValue(0);
            minute.setMaxValue(59);
            hour.setMaxValue(5);
            hour.setValue(0);
            minute.setValue(time);
        }

        private void initTheme() {
            int[] cs = ColorUtils.get2ThemeTextColor(TimeSleepActivity.this, appPreference.getTheme());
            tHour.setTextColor(cs[0]);
            tMinute.setTextColor(cs[0]);
        }

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            int min = minute.getValue();
            int hou = hour.getValue();
            time = hou * 60 + min;
            viewHolder.updateCurrentTimeText();
        }

        public void setEnable(boolean enable) {
            tHour.setEnabled(enable);
            tMinute.setEnabled(enable);
            minute.setEnabled(enable);
            hour.setEnabled(enable);

            float alpha = 1.0f;
            if (!enable) {
                alpha = 0.4f;
            }
            tHour.setAlpha(alpha);
            tMinute.setAlpha(alpha);
            minute.setAlpha(alpha);
            hour.setAlpha(alpha);
        }
    }

}
