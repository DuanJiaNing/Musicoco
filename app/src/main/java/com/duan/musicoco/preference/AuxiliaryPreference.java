package com.duan.musicoco.preference;

import android.content.Context;

/**
 * Created by DuanJiaNing on 2017/6/2.
 * 保存临时数据：定时停止播放时长、开始时间、是否启用
 */

public class AuxiliaryPreference extends BasePreference {


    public AuxiliaryPreference(Context context) {
        super(context, Preference.AUXILIARY_PREFERENCE);
    }

    /**
     * 用于定时退出应用
     */
    private class TimeSleep {
        static final String TIME_SLEEP_ENABLE = "time_sleep_enable";
        static final String TIME_SLEEP_START_TIME = "time_sleep_start_time";
        static final String TIME_SLEEP_DURATION = "time_sleep_duration";
    }

    /**
     * 在成功设置了定时关闭之后调用
     *
     * @param duration 定时时长，单位：分钟
     */
    public void updateTimeSleep(int duration) {
        editor = preferences.edit();
        editor.putBoolean(TimeSleep.TIME_SLEEP_ENABLE, true);
        editor.putInt(TimeSleep.TIME_SLEEP_DURATION, duration);
        editor.putLong(TimeSleep.TIME_SLEEP_START_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * 在定时退出时间到达时要调用此方法
     */
    public void setTimeSleepDisable() {
        editor = preferences.edit();
        editor.putBoolean(TimeSleep.TIME_SLEEP_ENABLE, false);
        editor.putInt(TimeSleep.TIME_SLEEP_DURATION, -1);
        editor.putLong(TimeSleep.TIME_SLEEP_START_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public boolean getTimeSleepEnable() {
        return preferences.getBoolean(TimeSleep.TIME_SLEEP_ENABLE, false);
    }

    public int getTimeSleepDuration() {
        return preferences.getInt(TimeSleep.TIME_SLEEP_DURATION, -1);
    }

    public long getTimeSleepStartTime() {
        return preferences.getLong(TimeSleep.TIME_SLEEP_START_TIME, -1);
    }
}
