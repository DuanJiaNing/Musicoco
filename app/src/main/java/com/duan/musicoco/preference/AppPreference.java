package com.duan.musicoco.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

/**
 * Created by DuanJiaNing on 2017/6/2.
 */

public class AppPreference {

    public static final String APP_PREFERENCE = "app_preference";
    public static final String KEY_THEME = "key_theme";
    public static final String KEY_OPEN_TIMES = "key_open_times";


    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private Context context;

    public AppPreference(Context context) {
        this.context = context;
    }

    public void modifyTheme(Theme theme) {
        if (theme == Theme.VARYING)
            return;

        check();
        editor = preferences.edit();
        editor.putString(KEY_THEME, theme.name());
        editor.apply();
    }

    private void check() {
        if (preferences == null)
            preferences = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);

    }

    public Theme getTheme() {
        check();
        String pa = preferences.getString(KEY_THEME, Theme.WHITE.name());
        return Theme.valueOf(pa);
    }

    public int appOpenTimes() {
        check();
        int times = preferences.getInt(KEY_OPEN_TIMES, 0);

        editor = preferences.edit();
        editor.putInt(KEY_OPEN_TIMES, times + 1);
        editor.apply();

        return times;
    }

}
