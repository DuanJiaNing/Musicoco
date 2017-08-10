package com.duan.musicoco.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/6/2.
 */

public class AppPreference {

    public static final String APP_PREFERENCE = "app_preference";
    public static final String KEY_THEME = "key_theme";
    public static final String KEY_OPEN_TIMES = "key_open_times";

    public static final String KEY_THEME_ACTIONBAR_COLOR = "key_theme_actionbar_color";
    public static final String KEY_THEME_STATUS_BAR_COLOR = "key_theme_statusBar_color";
    public static final String KEY_THEME_ACCENT_COLOR = "key_theme_accent_color";

    private SharedPreferences.Editor editor;
    private final SharedPreferences preferences;

    private final Context context;

    public AppPreference(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE);
    }

    public void updateTheme(ThemeEnum themeEnum) {
        if (themeEnum == ThemeEnum.VARYING)
            return;

        editor = preferences.edit();
        editor.putString(KEY_THEME, themeEnum.name());
        editor.apply();
    }

    public ThemeEnum getTheme() {
        String pa = preferences.getString(KEY_THEME, ThemeEnum.WHITE.name());
        return ThemeEnum.valueOf(pa);
    }

    @ColorInt
    public int getActionbarColor() {
        // 默认为 R.color.white_accent
        int deC = Color.parseColor("#de3a31");
        return preferences.getInt(KEY_THEME_ACTIONBAR_COLOR, deC);
    }

    public void updateActionbarColor(@ColorInt int color) {
        editor = preferences.edit();
        editor.putInt(KEY_THEME_ACTIONBAR_COLOR, color);
        editor.apply();
    }

    @ColorInt
    public int getStatusBarColor() {
        // 默认为 R.color.white_accent
        int deC = Color.parseColor("#de3a31");
        return preferences.getInt(KEY_THEME_STATUS_BAR_COLOR, deC);
    }

    /**
     * MainActivity 由于透明状态栏的需要，状态栏颜色只能为 透明，即该值对 MainActivity 无效，其他 Activity 有效，
     * 因此为了保持一致风格，updateActionbarColor 更改时应调用 updateStatusBarColor，使两者一致。
     */
    public void updateStatusBarColor(@ColorInt int color) {
        editor = preferences.edit();
        editor.putInt(KEY_THEME_STATUS_BAR_COLOR, color);
        editor.apply();
    }

    @ColorInt
    public int getAccentColor() {
        int deC = Color.parseColor("#FF002B");
        return preferences.getInt(KEY_THEME_ACCENT_COLOR, deC);
    }

    public void updateAccentColor(@ColorInt int color) {
        editor = preferences.edit();
        editor.putInt(KEY_THEME_ACCENT_COLOR, color);
        editor.apply();
    }

    public int appOpenTimes() {
        int times = preferences.getInt(KEY_OPEN_TIMES, 0);

        editor = preferences.edit();
        editor.putInt(KEY_OPEN_TIMES, times + 1);
        editor.apply();

        return times;
    }

}
