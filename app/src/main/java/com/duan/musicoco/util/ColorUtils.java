package com.duan.musicoco.util;

import android.graphics.Color;

/**
 * Created by DuanJiaNing on 2017/4/2.
 */

public class ColorUtils {

    /**
     * 获得一个随机的颜色
     *
     * @return 颜色
     */
    public static int getRandomColor() {
        int r = (int) (Math.random() * 255); //产生一个255以内的整数
        int g = (int) (Math.random() * 255); //产生一个255以内的整数
        int b = (int) (Math.random() * 255); //产生一个255以内的整数
        return Color.rgb(r, g, b);
    }

    /**
     * 获得一个比较暗的随机颜色
     *
     * @return 颜色
     */
    public static int getRandomBrunetColor() {
        int r = (int) (Math.random() * 100); //产生一个100以内的整数
        int g = (int) (Math.random() * 100);
        int b = (int) (Math.random() * 100);
        return Color.rgb(r, g, b);
    }

}
