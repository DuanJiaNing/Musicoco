package com.duan.musicoco.app;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.Utils;

/**
 * Created by DuanJiaNing on 2017/6/21.
 */

public class Init {

    private static final String TAG = "Init";

    public Bitmap initAlbumVisualizerImageCache(Activity activity) {

        String name = activity.getString(R.string.cache_bitmap_album_visualizer);
        BitmapCache cache = new BitmapCache(activity, name);

        String key = StringUtils.stringToMd5(BitmapCache.DEFAULT_PIC_KEY);
        Bitmap result = cache.get(key);
        if (result == null) {
            DisplayMetrics metrics = Utils.getMetrics(activity);
            int r = metrics.widthPixels * 2 / 3;

            BitmapBuilder builder = new BitmapBuilder(activity);
            builder.resizeForDefault(r, r, R.mipmap.default_album);
            builder.toRoundBitmap();
            builder.addOuterCircle(0, 10, Color.parseColor("#df3b43"))
                    .addOuterCircle(7, 1, Color.WHITE);

            cache.initDefaultBitmap(builder.getBitmap());
            return builder.getBitmap();
        }
        return result;
    }

    public void initMusicocoDB(Context context, MediaManager mediaManager) {
        DBMusicocoController db = new DBMusicocoController(context, true);

        db.truncate(DBMusicocoController.TABLE_SONG);
        db.truncate(DBMusicocoController.TABLE_SHEET);

        db.addSongInfo(mediaManager.getSongList());

        String allName = context.getString(R.string.sheet_new);
        db.addSheet(allName, "");

        Log.d(TAG, "initMusicocoDB: initialization database success ");

    }

}
