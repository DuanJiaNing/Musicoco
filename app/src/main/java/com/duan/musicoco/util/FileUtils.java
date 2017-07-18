package com.duan.musicoco.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/6/9.
 */

public class FileUtils {

    public static File getDiskCacheDirFile(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        boolean d = false;
        if (file.exists() && file.isFile()) {
            d = file.delete();
        }

        if (d) {
            Log.d("musicoco", "success deleteFile: " + path);
        } else {
            Log.d("musicoco", "fial deleteFile: " + path);
        }

        return d;
    }
}
