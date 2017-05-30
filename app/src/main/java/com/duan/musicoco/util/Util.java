package com.duan.musicoco.util;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class Util {

    public static String getGenTime(int misec) {
        int min = misec / 1000 / 60;
        int sec = (misec / 1000) % 60;
        String minStr = min < 10 ? "0" + min : min + "";
        String secStr = sec < 10 ? "0" + sec : sec + "";
        return minStr + ":" + secStr;
    }
}
