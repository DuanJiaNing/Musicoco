package com.duan.musicoco.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by DuanJiaNing on 2017/6/1.
 */

public class ToastUtil {

    public static void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
