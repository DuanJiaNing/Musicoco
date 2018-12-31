package com.duan.musicoco.util;

/**
 * Created by DuanJiaNing on 2018/12/31.
 */

public class ArrayUtils {

    public static float[] reverse(float[] Array) {
        float[] new_array = new float[Array.length];
        for (int i = 0; i < Array.length; i++) {
            // 反转后数组的第一个元素等于源数组的最后一个元素：
            new_array[i] = Array[Array.length - i - 1];
        }
        return new_array;
    }

    public static float average(float[] fft) {
        float sum = 0;
        for (float v : fft) {
            sum += v;
        }

        return sum / fft.length;
    }
}
