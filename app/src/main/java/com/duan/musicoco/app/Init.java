package com.duan.musicoco.app;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.util.StringUtil;

/**
 * Created by DuanJiaNing on 2017/6/21.
 */

public class Init {

    public Init() {
    }

    public void initImageCache(Activity activity) {
        BitmapCache cache = BitmapCache.getInstance(activity);
        String key = StringUtil.stringToMd5(BitmapCache.DEFAULT_PIC_KEY);
        if (cache.get(key) == null) {
            cache.initDefaultBitmap();
        }
    }

}
