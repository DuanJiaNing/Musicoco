package com.duan.musicoco.preference;

/**
 * Created by DuanJiaNing on 2017/6/13.
 */

public enum ThemeEnum {
    //白色
    WHITE,

    //黑色
    DARK,

    //随专辑图片变化
    VARYING;

    public static ThemeEnum reversal(ThemeEnum theme) {
        if (theme == WHITE || theme == VARYING) {
            return DARK;
        } else {
            return WHITE;
        }
    }
}
