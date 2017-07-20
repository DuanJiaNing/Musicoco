package com.duan.musicoco.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;

import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.db.DBSongInfo;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class Utils {

    public static DisplayMetrics getMetrics(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

}
