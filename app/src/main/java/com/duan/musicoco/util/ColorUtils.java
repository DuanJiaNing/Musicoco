package com.duan.musicoco.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

import com.duan.musicoco.R;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;

/**
 * Created by DuanJiaNing on 2017/4/2.
 */

public class ColorUtils {

    /**
     * 获得一个随机的颜色
     */
    public static int getRandomColor() {
        int r = (int) (Math.random() * 255); //产生一个255以内的整数
        int g = (int) (Math.random() * 255); //产生一个255以内的整数
        int b = (int) (Math.random() * 255); //产生一个255以内的整数
        return Color.rgb(r, g, b);
    }

    /**
     * 获得一个比较暗的随机颜色
     */
    public static int getRandomBrunetColor() {
        int r = (int) (Math.random() * 100); //产生一个100以内的整数
        int g = (int) (Math.random() * 100);
        int b = (int) (Math.random() * 100);
        return Color.rgb(r, g, b);
    }

    /**
     * 获得图片中出现最多的颜色
     * 0 活力颜色<br>
     * 1 亮的活力颜色<br>
     * 2 暗的活力颜色<br>
     * 3 柔和颜色<br>
     * 4 亮的柔和颜色<br>
     * 5 暗的柔和颜色<br>
     */
    public static void get6ColorFormBitmap(@NonNull Bitmap bitmap, int defaultColor, int[] colors) {

        if (colors.length != 6)
            return;

        Palette palette;
        palette = new Palette.Builder(bitmap).generate();

        Palette.Swatch swatch;
        int color;

        if ((swatch = palette.getVibrantSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[0] = color;

        if ((swatch = palette.getLightVibrantSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[1] = color;

        if ((swatch = palette.getDarkVibrantSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[2] = color;

        if ((swatch = palette.getMutedSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[3] = color;

        if ((swatch = palette.getLightMutedSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[4] = color;

        if ((swatch = palette.getDarkMutedSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[5] = color;

    }

    /**
     * 获得图片中出现最多的颜色
     * 0 活力颜色<br>
     * 1 对应字体颜色<br>
     * 2 亮的活力颜色<br>
     * 3 对应字体颜色<br>
     * 4 暗的活力颜色<br>
     * 5 对应字体颜色<br>
     * 6 柔和颜色<br>
     * 7 对应字体颜色<br>
     * 8 亮的柔和颜色<br>
     * 9 对应字体颜色<br>
     * 10 暗的柔和颜色<br>
     * 11 对应字体颜色<br>
     */
    public static void get12ColorFormBitmap(@NonNull Bitmap bitmap, int defaultColor, int defaultTextColor, int[] colors) {

        if (colors.length != 12)
            return;

        Palette palette;
        palette = new Palette.Builder(bitmap).generate();

        Palette.Swatch swatch;
        int color;
        int tColor;

        if ((swatch = palette.getVibrantSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();
        } else {
            color = defaultColor;
            tColor = defaultTextColor;
        }
        colors[0] = color;
        colors[1] = tColor;

        if ((swatch = palette.getLightVibrantSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();

        } else {
            color = defaultColor;
            tColor = defaultTextColor;

        }
        colors[2] = color;
        colors[3] = tColor;

        if ((swatch = palette.getDarkVibrantSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();

        } else {
            color = defaultColor;
            tColor = defaultTextColor;

        }
        colors[4] = color;
        colors[5] = tColor;

        if ((swatch = palette.getMutedSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();

        } else {
            color = defaultColor;
            tColor = defaultTextColor;

        }
        colors[6] = color;
        colors[7] = tColor;

        if ((swatch = palette.getLightMutedSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();

        } else {
            color = defaultColor;
            tColor = defaultTextColor;

        }
        colors[8] = color;
        colors[9] = tColor;

        if ((swatch = palette.getDarkMutedSwatch()) != null) {
            color = swatch.getRgb();
            tColor = swatch.getTitleTextColor();

        } else {
            color = defaultColor;
            tColor = defaultTextColor;

        }
        colors[10] = color;
        colors[11] = tColor;

    }

    /**
     * 获得图片中出现最多的颜色
     * 0 亮的活力颜色
     * 1 亮的柔和颜色
     */
    public static void get2ColorFormBitmap(@NonNull Bitmap bitmap, int defaultColor, int[] colors) {

        if (colors.length != 2)
            return;

        Palette palette;
        palette = new Palette.Builder(bitmap).generate();

        Palette.Swatch swatch;
        int color;

        if ((swatch = palette.getLightVibrantSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[0] = color;

        if ((swatch = palette.getLightMutedSwatch()) != null)
            color = swatch.getRgb();
        else color = defaultColor;
        colors[1] = color;

    }

    /**
     * 获得图片中出现最多的颜色<br>
     * 0 暗的活力颜色<br>
     * 1 暗的活力颜色 对应适合的字体颜色<br>
     * 2 暗的柔和颜色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色<br>
     */
    public static void get4DarkColorWithTextFormBitmap(@NonNull Bitmap bitmap, int defaultColor, int defaultTextColor, int[] colors) {

        if (colors.length != 4)
            return;

        Palette palette;
        palette = new Palette.Builder(bitmap).generate();

        Palette.Swatch swatch;
        int color = defaultColor;
        int textColor = defaultTextColor;

        if ((swatch = palette.getDarkVibrantSwatch()) != null) {
            color = swatch.getRgb();
            textColor = swatch.getTitleTextColor();
        }
        colors[0] = color;
        colors[1] = textColor;

        color = defaultColor;
        textColor = defaultTextColor;

        if ((swatch = palette.getDarkMutedSwatch()) != null) {
            color = swatch.getRgb();
            textColor = swatch.getTitleTextColor();
        }
        colors[2] = color;
        colors[3] = textColor;

    }

    /**
     * 获得图片中出现最多的颜色<br>
     * 0 亮的活力颜色<br>
     * 1 亮的活力颜色 对应适合的字体颜色<br>
     * 2 亮的柔和颜色<br>
     * 3 亮的柔和颜色 对应适合的字体颜色<br>
     */
    public static void get4LightColorWithTextFormBitmap(@NonNull Bitmap bitmap, int defaultColor, int defaultTextColor, int[] colors) {

        if (colors.length != 4)
            return;

        Palette palette;
        palette = new Palette.Builder(bitmap).generate();

        Palette.Swatch swatch;
        int color = defaultColor;
        int textColor = defaultTextColor;

        if ((swatch = palette.getLightVibrantSwatch()) != null) {
            color = swatch.getRgb();
            textColor = swatch.getTitleTextColor();
        }
        colors[0] = color;
        colors[1] = textColor;

        color = defaultColor;
        textColor = defaultTextColor;

        if ((swatch = palette.getLightMutedSwatch()) != null) {
            color = swatch.getRgb();
            textColor = swatch.getTitleTextColor();
        }
        colors[2] = color;
        colors[3] = textColor;

    }

    /**
     * 0 状态栏背景色
     * 1 标题栏背景色
     */
    public static int[] get2ActionStatusBarColors(Context context) {

        AppPreference preference = new AppPreference(context);

        int actionColor;
        int statusColor;

        if (preference.getTheme() != ThemeEnum.DARK) {
            actionColor = preference.getActionbarColor();
            statusColor = preference.getStatusBarColor();
        } else {
            statusColor = context.getResources().getColor(R.color.theme_dark_primary);
            actionColor = context.getResources().getColor(R.color.theme_dark_primary_dark);
        }

        return new int[]{statusColor, actionColor};
    }

    public static int getAccentColor(Context context) {
        AppPreference preference = new AppPreference(context);
        if (preference.getTheme() != ThemeEnum.DARK) {
            return preference.getAccentColor();
        } else {
            return context.getResources().getColor(R.color.theme_dark_accent);
        }
    }

    /**
     * 0 状态栏背景色<br>
     * 1 标题栏背景色<br>
     * 2 控件首选色<br>
     * 3 主背景色<br>
     * 4 辅背景色<br>
     * 5 主字体色<br>
     * 6 辅字体色<br>
     * 7 底部导航背景色<br>
     * 8 标题栏主字体色<br>
     * 9 标题栏辅字体色<br>
     */
    public static int[] get10WhiteThemeColors(Context context) {
        int[] colors = new int[10];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.theme_white_primary);
            colors[1] = context.getColor(R.color.theme_white_primary_dark);
            colors[2] = context.getColor(R.color.theme_white_accent);
            colors[3] = context.getColor(R.color.theme_white_main_bg);
            colors[4] = context.getColor(R.color.theme_white_vic_bg);
            colors[5] = context.getColor(R.color.theme_white_main_text);
            colors[6] = context.getColor(R.color.theme_white_vic_text);
            colors[7] = context.getColor(R.color.theme_white_nav);
            colors[8] = context.getColor(R.color.theme_white_toolbar_main_text);
            colors[9] = context.getColor(R.color.theme_white_toolbar_vic_text);
        } else {
            colors[0] = context.getResources().getColor(R.color.theme_white_primary);
            colors[1] = context.getResources().getColor(R.color.theme_white_primary_dark);
            colors[2] = context.getResources().getColor(R.color.theme_white_accent);
            colors[3] = context.getResources().getColor(R.color.theme_white_main_bg);
            colors[4] = context.getResources().getColor(R.color.theme_white_vic_bg);
            colors[5] = context.getResources().getColor(R.color.theme_white_main_text);
            colors[6] = context.getResources().getColor(R.color.theme_white_vic_text);
            colors[7] = context.getResources().getColor(R.color.theme_white_nav);
            colors[8] = context.getResources().getColor(R.color.theme_white_toolbar_main_text);
            colors[9] = context.getResources().getColor(R.color.theme_white_toolbar_vic_text);
        }

        AppPreference preference = new AppPreference(context);
        colors[2] = preference.getAccentColor();

        return colors;
    }

    /**
     * 0 状态栏背景色<br>
     * 1 标题栏背景色<br>
     * 2 控件首选色<br>
     * 3 主背景色<br>
     * 4 辅背景色<br>
     * 5 主字体色<br>
     * 6 辅字体色<br>
     * 7 底部导航背景色<br>
     * 8 标题栏主字体色<br>
     * 9 标题栏辅字体色<br>
     */
    public static int[] get10DarkThemeColors(Context context) {
        int[] colors = new int[10];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.theme_dark_primary);
            colors[1] = context.getColor(R.color.theme_dark_primary_dark);
            colors[2] = context.getColor(R.color.theme_dark_accent);
            colors[3] = context.getColor(R.color.theme_dark_main_bg);
            colors[4] = context.getColor(R.color.theme_dark_vic_bg);
            colors[5] = context.getColor(R.color.theme_dark_main_text);
            colors[6] = context.getColor(R.color.theme_dark_vic_text);
            colors[7] = context.getColor(R.color.theme_dark_nav);
            colors[8] = context.getColor(R.color.theme_dark_toolbar_main_text);
            colors[9] = context.getColor(R.color.theme_dark_toolbar_vic_text);
        } else {
            colors[0] = context.getResources().getColor(R.color.theme_dark_primary);
            colors[1] = context.getResources().getColor(R.color.theme_dark_primary_dark);
            colors[2] = context.getResources().getColor(R.color.theme_dark_accent);
            colors[3] = context.getResources().getColor(R.color.theme_dark_main_bg);
            colors[4] = context.getResources().getColor(R.color.theme_dark_vic_bg);
            colors[5] = context.getResources().getColor(R.color.theme_dark_main_text);
            colors[6] = context.getResources().getColor(R.color.theme_dark_vic_text);
            colors[7] = context.getResources().getColor(R.color.theme_dark_nav);
            colors[8] = context.getResources().getColor(R.color.theme_dark_toolbar_main_text);
            colors[9] = context.getResources().getColor(R.color.theme_dark_toolbar_vic_text);
        }

        return colors;
    }

    /**
     * 0 状态栏背景色<br>
     * 1 标题栏背景色<br>
     * 2 控件首选色<br>
     * 3 主背景色<br>
     * 4 辅背景色<br>
     * 5 主字体色<br>
     * 6 辅字体色<br>
     * 7 底部导航背景色<br>
     * 8 标题栏主字体色<br>
     * 9 标题栏辅字体色<br>
     */
    public static int[] get10ThemeColors(Context context, ThemeEnum themeEnum) {
        if (themeEnum == ThemeEnum.WHITE || themeEnum == ThemeEnum.VARYING) {
            return get10WhiteThemeColors(context);
        } else {
            return get10DarkThemeColors(context);
        }
    }

    /**
     * 0 主字体颜色
     * 1 辅字体颜色
     */
    public static int[] get2WhiteThemeTextColor(Context context) {
        int[] colors = new int[2];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.theme_white_main_text); //主字体色
            colors[1] = context.getColor(R.color.theme_white_vic_text); // 辅字体色
        } else {
            colors[0] = context.getResources().getColor(R.color.theme_white_main_text);
            colors[1] = context.getResources().getColor(R.color.theme_white_vic_text);
        }

        return colors;
    }


    /**
     * 0 主字体颜色
     * 1 辅字体颜色
     */
    public static int[] get2ThemeTextColor(Context context, ThemeEnum themeEnum) {
        if (themeEnum == ThemeEnum.DARK) {
            return get2DarkThemeTextColor(context);
        } else {
            return get2WhiteThemeTextColor(context);
        }
    }

    /**
     * 0 主字体颜色
     * 1 辅字体颜色
     */
    public static int[] get2DarkThemeTextColor(Context context) {
        int[] colors = new int[2];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.theme_dark_main_text); //主字体色
            colors[1] = context.getColor(R.color.theme_dark_vic_text); // 辅字体色
        } else {
            colors[0] = context.getResources().getColor(R.color.theme_dark_main_text);
            colors[1] = context.getResources().getColor(R.color.theme_dark_vic_text);
        }
        return colors;
    }

    public static int[] get2ToolbarTextColors(Context context) {
        int[] colors = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.white_d);
            colors[1] = context.getColor(R.color.white_d_d);
        } else {
            colors[0] = context.getResources().getColor(R.color.white_d);
            colors[1] = context.getResources().getColor(R.color.white_d_d);
        }

        return colors;
    }

    public static int[] get2ColorWhiteThemeForPlayOptions(Context context) {
        int[] colors = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.dark_l_l_l_l);
            colors[1] = context.getColor(R.color.white_d_d_d);
        } else {
            colors[0] = context.getResources().getColor(R.color.dark_l_l_l_l);
            colors[1] = context.getResources().getColor(R.color.white_d_d_d);
        }
        return colors;
    }

    public static int[] get2ColorDarkThemeForPlayOptions(Context context) {

        int[] colors = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colors[0] = context.getColor(R.color.white_d_d);
            colors[1] = context.getColor(R.color.white_d_d_d);
        } else {
            colors[0] = context.getResources().getColor(R.color.white_d_d);
            colors[1] = context.getResources().getColor(R.color.white_d_d_d);
        }
        return colors;
    }

    /**
     * r g b >= 160 时返回 true
     */
    public static boolean isBrightSeriesColor(int color) {

        double d = android.support.v4.graphics.ColorUtils.calculateLuminance(color);
        if (d - 0.400 > 0.000001) {
            return true;
        } else {
            return false;
        }
    }

}
