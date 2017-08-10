package com.duan.musicoco.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.provider.Settings;
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

public class BitmapProducer {

    private static final String TAG = "BitmapProducer";

    private final Context context;

    public BitmapProducer(Context context) {
        this.context = context;
    }

    @Nullable
    public Bitmap getBitmapForVisualizer(BitmapCache cache, SongInfo info, int size, int defaultColor) {

        if (!cache.getName().equals(context.getString(R.string.cache_bitmap_album_visualizer))) {
            return null;
        }

        Bitmap result;
        BitmapBuilder builder = new BitmapBuilder(context);

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

                addDefaultOuters(builder, defaultColor);

                Bitmap b = builder.getBitmap();
                if (b != null) { // 成功构建
                    Log.d(TAG, "getBitmapForVisualizer: create album picture for 【" + info.getTitle() + "】 successfully.");
                    cache.add(key, b);
                    result = b;
                } else {//构建失败
                    Log.d(TAG, "getBitmapForVisualizer: create album picture for 【" + info.getTitle() + "】 fail.");
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


    private void addDefaultOuters(BitmapBuilder builder, int defaultColor) {

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

    /**
     * 获得 万花筒 图片
     *
     * @param souPath 图片源路径
     * @param width   目标图片宽
     * @param height  目标图片高
     */
    public Bitmap getKaleidoscope(String[] souPath, int width, int height, Bitmap defalt) {

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);

        int count = souPath.length;
        count = count % 2 == 0 ? count : count - 1;

        String[] res = new String[count];
        System.arraycopy(souPath, 0, res, 0, count);

        Bitmap[] sous = new Bitmap[count];
        float[][] coors = new float[count][2];


        int[] c = new int[count / 2];
        int j = 0;
        for (int i = 1; i <= count; i++) {
            if (count % i == 0) {
                c[j++] = i;
            }
        }


        return bitmap;
    }

}
