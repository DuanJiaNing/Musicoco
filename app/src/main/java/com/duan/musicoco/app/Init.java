package com.duan.musicoco.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.SettingPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by DuanJiaNing on 2017/6/21.
 */

public class Init {

    private static final String TAG = "Init";

    public static Bitmap initAlbumVisualizerImageCache(Activity activity) {

        BitmapCache cache = new BitmapCache(activity, BitmapCache.CACHE_ALBUM_VISUALIZER_IMAGE);

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

    public static void initMusicocoDB(Context context, MediaManager mediaManager) {
        DBMusicocoController db = new DBMusicocoController(context, true);

        db.truncate(DBMusicocoController.TABLE_SONG);
        db.truncate(DBMusicocoController.TABLE_SHEET);

        db.addSongInfo(mediaManager.getSongList());

        db.addSheet("我喜欢的", "收藏我所喜欢", 0);
        //FIXME double_number_picker
        db.addSheet("新的歌单 --", "", 0);
        db.addSheet("song sheet", "double_number_picker for remark", 0);
        db.addSheet("nice song", "double_number_picker 胜多负少的法法阿达是否阿达的说法大师傅大厦法定是否大厦法定是否for remark", 0);

        db.close();
        Log.d(TAG, "initMusicocoDB: initialization database success ");

    }

}
