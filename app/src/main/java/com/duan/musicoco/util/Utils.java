package com.duan.musicoco.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class Utils {

    public static String getGenTime(int misec) {
        int min = misec / 1000 / 60;
        int sec = (misec / 1000) % 60;
        String minStr = min < 10 ? "0" + min : min + "";
        String secStr = sec < 10 ? "0" + sec : sec + "";
        return minStr + ":" + secStr;
    }

    public static DisplayMetrics getMetrics(Activity activity) {
        Context context;
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }
}
