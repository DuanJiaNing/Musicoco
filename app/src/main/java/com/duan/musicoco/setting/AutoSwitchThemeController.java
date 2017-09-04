package com.duan.musicoco.setting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by DuanJiaNing on 2017/8/19.
 */

public class AutoSwitchThemeController {

    private static volatile AutoSwitchThemeController mInstance;

    private final Context context;
    private final AppPreference appPreference;
    private Calendar current, nightThemeStart, nightThemeEnd;
    private PendingIntent piS, piE;
    private AlarmManager alarmManager;

    private boolean isSet = false;

    private AutoSwitchThemeController(Context context) {
        this.context = context;
        this.appPreference = new AppPreference(context);
        this.alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
    }

    public static AutoSwitchThemeController getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AutoSwitchThemeController.class) {
                if (mInstance == null) {
                    mInstance = new AutoSwitchThemeController(context);
                }
            }
        }
        return mInstance;
    }

    // 设置切换提醒
    public void setAlarm() {

        // UPDATE: 2017/8/24 更新 时间段自定义
        current = Calendar.getInstance();
        nightThemeStart = new GregorianCalendar(
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH),
                22, 30); // 22:30 切换到夜间模式

        nightThemeEnd = (Calendar) nightThemeStart.clone();
        nightThemeEnd.add(Calendar.DAY_OF_MONTH, 1); // 后一天
        nightThemeEnd.set(Calendar.HOUR_OF_DAY, 7); // 07:00 切换到白天模式
        nightThemeEnd.set(Calendar.MINUTE, 0);

        checkTheme();
        isSet = true;

        Intent intentS = new Intent();
        Intent intentE = new Intent();

        intentS.setAction(BroadcastManager.FILTER_APP_THEME_CHANGE_AUTOMATIC);
        intentS.putExtra(BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_TOKEN,
                BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_DARK);

        intentE.setAction(BroadcastManager.FILTER_APP_THEME_CHANGE_AUTOMATIC);
        intentE.putExtra(BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_TOKEN,
                BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_WHITE);

        piS = PendingIntent.getBroadcast(context, 0, intentS, 0);
        piE = PendingIntent.getBroadcast(context, 1, intentE, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, nightThemeStart.getTimeInMillis(), piS);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nightThemeEnd.getTimeInMillis(), piE);

    }

    public void cancelAlarm() {
        if (piE != null) {
            alarmManager.cancel(piE);
        }
        if (piS != null) {
            alarmManager.cancel(piS);
        }
        isSet = false;
    }

    private void checkTheme() {

        if (current.compareTo(nightThemeStart) > 0 && current.compareTo(nightThemeEnd) < 0) {
            appPreference.updateTheme(ThemeEnum.DARK);
        } else {
            appPreference.updateTheme(ThemeEnum.WHITE);
        }

        BroadcastManager.getInstance().sendBroadcast(context, BroadcastManager.FILTER_APP_THEME_CHANGE_AUTOMATIC, null);
    }

    public boolean isSet() {
        return isSet;
    }
}
