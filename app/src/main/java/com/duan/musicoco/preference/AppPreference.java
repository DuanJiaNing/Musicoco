package com.duan.musicoco.preference;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;

import com.duan.musicoco.R;
import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/6/2.
 * 应用配置：应用主题，颜色，照片墙属性
 */

public class AppPreference extends BasePreference {

    public static final String KEY_THEME = "key_theme";
    public static final String KEY_OPEN_TIMES = "key_open_times";

    public static final String KEY_THEME_ACTIONBAR_COLOR = "key_theme_actionbar_color";
    public static final String KEY_THEME_STATUS_BAR_COLOR = "key_theme_statusBar_color";
    public static final String KEY_THEME_ACCENT_COLOR = "key_theme_accent_color";

    // 照片墙照片数
    public static final String KEY_IMAGE_WALL_SIZE = "key_image_wall_size";
    // 照片墙照片虚化程度
    public static final String KEY_IMAGE_WALL_BLUR = "key_image_wall_blur";
    // 照片墙照片透明度
    public static final String KEY_IMAGE_WALL_ALPHA = "key_image_wall_alpha";

    public AppPreference(Context context) {
        super(context, Preference.APP_PREFERENCE);
    }

    public int getImageWallSize() {
        int defaultSize = 80;
        return preferences.getInt(KEY_IMAGE_WALL_SIZE, defaultSize);
    }

    /**
     * @param size 0 - 100
     */
    public void updateImageWallSize(int size) {
        int defaultSize = context.getResources().getInteger(R.integer.image_wall_max_size);
        if (size < 0 || size > defaultSize) {
            return;
        }

        editor = preferences.edit();
        editor.putInt(KEY_IMAGE_WALL_SIZE, size);
        editor.apply();
    }

    public int getImageWallBlur() {
        int defaultBlur = 1; // 不能小于 1
        return preferences.getInt(KEY_IMAGE_WALL_BLUR, defaultBlur);
    }

    /**
     * @param size 1 - 100
     */
    public void updateImageWallBlur(int size) {
        int max = context.getResources().getInteger(R.integer.image_wall_max_blur);
        if (size < 1 || size > max) {
            return;
        }

        editor = preferences.edit();
        editor.putInt(KEY_IMAGE_WALL_BLUR, size);
        editor.apply();
    }

    public int getImageWallAlpha() {
        int defaultAlpha = context.getResources().getInteger(R.integer.image_wall_default_alpha);
        return preferences.getInt(KEY_IMAGE_WALL_ALPHA, defaultAlpha);
    }

    /**
     * @param alpha (全透明)0 - 255(不透明 黑色)
     */
    public void updateImageWallAlpha(int alpha) {
        if (alpha < 0 || alpha > 255) {
            return;
        }

        editor = preferences.edit();
        editor.putInt(KEY_IMAGE_WALL_ALPHA, alpha);
        editor.apply();
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
        int deC;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deC = context.getColor(R.color.colorPrimary);
        } else {
            deC = context.getResources().getColor(R.color.colorPrimary);
        }
        return preferences.getInt(KEY_THEME_ACTIONBAR_COLOR, deC);
    }

    public void updateActionbarColor(@ColorInt int color) {
        editor = preferences.edit();
        editor.putInt(KEY_THEME_ACTIONBAR_COLOR, color);
        editor.apply();
    }

    @ColorInt
    public int getStatusBarColor() {
        int deC;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deC = context.getColor(R.color.colorPrimary);
        } else {
            deC = context.getResources().getColor(R.color.colorPrimary);
        }
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

    /**
     * 不要直接调用该方法，除非你确定此时应用主题为【白天】，应该使用{@link ColorUtils#getAccentColor(Context)}
     * 获取该值。
     *
     * @return 控件首选色
     */
    @ColorInt
    public int getAccentColor() {
        int deC;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deC = context.getColor(R.color.colorPrimary);
        } else {
            deC = context.getResources().getColor(R.color.colorPrimary);
        }
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
