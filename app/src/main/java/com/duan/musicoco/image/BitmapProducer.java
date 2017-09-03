package com.duan.musicoco.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import com.duan.musicoco.app.Init;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;

import static com.duan.musicoco.cache.BitmapCache.DEFAULT_PIC_KEY;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class BitmapProducer {

    private final Context context;

    public BitmapProducer(Context context) {
        this.context = context;
    }

    @Nullable
    public Bitmap getBitmapForVisualizer(BitmapCache cache, String path, int size, int defaultColor) {

        if (!cache.getName().equals(BitmapCache.CACHE_ALBUM_VISUALIZER_IMAGE)) {
            return null;
        }

        Bitmap result;
        BitmapBuilder builder = new BitmapBuilder(context);

        if (!StringUtils.isReal(path))
            result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
        else {

            String key = StringUtils.stringToMd5(path);

            result = cache.get(key);

            if (result == null) { //磁盘缓存中没有

                builder.reset();

                builder.setPath(path)
                        .resize(size)
                        .toRoundBitmap()
                        .build();

                addDefaultOuters(builder, defaultColor);

                Bitmap b = builder.getBitmap();
                if (b != null) { // 成功构建
                    cache.add(key, b);
                    result = b;
                } else {//构建失败
                    result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
                }
            }
        }

        if (result == null)
            try {
                result = cache.getDefaultBitmap();
            } catch (Exception e) {
                result = Init.initAlbumVisualizerImageCache((Activity) context);
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
    public Bitmap getKaleidoscope(String[] souPath, int width, int height, @DrawableRes int defaultBitmap) {

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);

        int count = souPath.length;
        // 传入数据源为奇数时减一使其成为偶数
        count = count % 2 == 0 ? count : count - 1;

        // 求出水平和垂直方向分别应该有几张图片
        // 如 8 张: 1 2 4 8 。 水平 2，垂直 4
        // 如 10 张: 1 2 5 10 。 水平 2，垂直 5
        // 如 18 张: 1 2 3 6 9 18 。 水平 3，垂直 6
        int[] c = new int[count];
        int j = 0;
        for (int i = 1; i <= count; i++) {
            if (count % i == 0) {
                c[j++] = i;
            }
        }
        int index = j / 2;
        int tx = c[index];
        int ty = count / tx;

        // 断定高大于宽
        int x = Math.min(tx, ty); // 水平上分为 x 份（即水平上 x 张图片）
        int y = Math.max(tx, ty); // 垂直方向为 y 份

        int w = width / x;
        int h = height / y;

        //避免挤压，使宽高相等
        if (width > height) h = w; // 以长边为准
        else w = h;

        Paint paint = new Paint();
        Bitmap default_ = null;
        paint.setAntiAlias(true);

//        Log.i(TAG, "getKaleidoscope: w=" + w + " h=" + h);
//
//        Bitmap b = BitmapUtils.bitmapResizeFromFile(res[0], w, h);
//
//        if (b == null) {
//            if (default_ == null) {
//                default_ = BitmapUtils.bitmapResizeFromResource(context.getResources(),
//                        defaultBitmap, w, h);
//            }
//            b = default_;
//        }
//        Log.i(TAG, "bitmapResizeFromFile: w=" + b.getWidth() + " h=" + b.getHeight());
//
//        Matrix matrix = new Matrix();
//        Bitmap bi = BitmapFactory.decodeFile(res[index]);
//        float sx = bi.getWidth() / w;
//        float sy = bi.getHeight() / h;
//        matrix.setScale(sx, sy);
//        b = Bitmap.createBitmap(b, 0, 0, w, h, matrix, true);
//
//        Log.i(TAG, "matrix: w=" + b.getWidth() + " h=" + b.getHeight());
//
//        b = Bitmap.createScaledBitmap(bi,w,h,true);
//        Log.i(TAG, "createScaledBitmap: w=" + b.getWidth() + " h=" + b.getHeight());
//
//        return b;

        // 绘制
        String[] res = new String[count];
        System.arraycopy(souPath, 0, res, 0, count);

        for (int i = 0; i < y; i++) {
            for (int k = 0; k < x; k++) {
                int resIndex = i * x + k;
                String path = res[resIndex];

                // 压缩得到的图片宽高大于目标宽高
                Bitmap sou = BitmapUtils.bitmapResizeFromFile(path, w, h);
                if (sou == null) {
                    if (default_ == null) {
                        default_ = BitmapUtils.bitmapResizeFromResource(context.getResources(),
                                defaultBitmap, w, h);
                    }
                    sou = default_;
                }

                // 需要再次处理
                Bitmap b = Bitmap.createScaledBitmap(sou, w, h, false);

                int top = h * i;
                int left = w * k;

                canvas.drawBitmap(b, left, top, paint);
            }
        }

        return bitmap;

    }

}
