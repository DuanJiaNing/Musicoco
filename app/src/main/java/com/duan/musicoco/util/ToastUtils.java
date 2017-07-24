package com.duan.musicoco.util;

import android.widget.Toast;

import com.duan.musicoco.app.App;

/**
 * Created by DuanJiaNing on 2017/6/1.
 */

public class ToastUtils {

    public static void showShortToast(CharSequence msg) {
        Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(CharSequence msg) {
        Toast.makeText(App.getContext(), msg, Toast.LENGTH_LONG).show();
    }

}
