package com.duan.musicoco.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.app.Init;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;

import static com.duan.musicoco.cache.BitmapCache.DEFAULT_PIC_KEY;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class AlbumBitmapProducer {

    private static final String TAG = "AlbumBitmapProducer";

    private final BitmapCache cache;

    private final BitmapBuilder builder;

    private final int defaultColor;

    private final Context context;

    public AlbumBitmapProducer(Context context, BitmapCache cache, int defaultColor) {
        this.builder = new BitmapBuilder(context);
        this.defaultColor = defaultColor;
        this.context = context;

        if (cache.getName().equals(context.getString(R.string.cache_bitmap_album_visualizer)))
            this.cache = cache;
        else
            this.cache = new BitmapCache(context, context.getString(R.string.cache_bitmap_album_visualizer));

    }

    @Nullable
    public Bitmap get(SongInfo info, int size) {

        Bitmap result;

        if (info == null || info.getAlbum_path() == null)
            result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
        else {

            String key = StringUtils.stringToMd5(info.getAlbum_path());

            result = cache.get(key);

            if (result == null) { //磁盘缓存中没有

                builder.reset();

                builder.setPath(info.getAlbum_path())
                        .resize(size)
                        .toRoundBitmap()
                        .build();

                addDefaultOuters(builder);

                Bitmap b = builder.getBitmap();
                if (b != null) { // 成功构建
                    Log.d(TAG, "get: create album picture for 【" + info.getTitle() + "】 successfully.");
                    cache.add(key, b);
                    result = b;
                } else {//构建失败
                    Log.d(TAG, "get: create album picture for 【" + info.getTitle() + "】 fail.");
                    result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
                }
            }
        }

        if (result == null)
            try {
                result = cache.getDefaultBitmap();
            } catch (Exception e) {
                result = new Init().initAlbumVisualizerImageCache((Activity) context);
            }
        return result;
    }


    private void addDefaultOuters(BitmapBuilder builder) {

        if (builder == null || builder.getBitmap() == null)
            return;

        int[] colors = new int[2];
        ColorUtils.get2ColorFormBitmap(builder.getBitmap(), defaultColor, colors);

        int color = defaultColor;
        for (int c : colors)
            if (c != defaultColor) {
                color = c;
                break;
            }

        builder.addOuterCircle(0, 10, color)
                .addOuterCircle(7, 1, Color.WHITE);
    }

}
