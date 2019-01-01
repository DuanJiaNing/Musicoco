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

    public static float min(float[] data) {
        float min = data[0];
        for (float d : data) {
            if (d < min) min = d;
        }

        return min;
    }

    public static float max(float[] data) {
        float max = data[0];
        for (float d : data) {
            if (d > max) max = d;
        }

        return max;
    }
}
