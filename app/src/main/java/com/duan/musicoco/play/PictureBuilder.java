package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.duan.musicoco.util.BitmapUtils;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * <p>
 */

public class PictureBuilder {

    private int radius = 0;
    private String path;
    private Bitmap bitM;
    private final Bitmap defaultBitmap;
    private Context context;

    public PictureBuilder(Context context, int radius, String fillPath, Bitmap defaultBitmap) {
        this.radius = radius;
        this.path = fillPath;
        this.context = context;
        this.defaultBitmap = defaultBitmap;
    }

    public PictureBuilder resize() {
        bitM = BitmapUtils.bitmapResizeFromFile(path, radius * 2, radius * 2);
        return this;
    }

    public PictureBuilder resize(int reqWidth, int reqHeight) {
        bitM = BitmapUtils.bitmapResizeFromFile(path, reqWidth, reqHeight);
        return this;
    }

    /**
     * 如果需要生成的图片拥有透明层（png），则确保在此之前调用 {@link #jpg2png()} 方法
     */
    public PictureBuilder toRoundBitmap() {
        bitM = BitmapUtils.getCircleBitmap(check());
        return this;
    }

    public PictureBuilder jpg2png() {
        bitM = BitmapUtils.jpgTopng(check(), context);
        return this;
    }

    private Bitmap check() {
        return bitM == null ? defaultBitmap : bitM;
    }

    /**
     * 在图片外面绘制圆圈，该方法不应该与{@link #resize(int, int)}一起使用
     *
     * @param space       与图片或上一个圆圈的间距
     * @param strokeWidth 圆圈宽度
     * @param color       圆圈颜色
     */
    public PictureBuilder addOuterCircle(int space, int strokeWidth, int color) {
        check();

        radius += strokeWidth;

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        Bitmap b = check();
        Canvas canvas = new Canvas(b);
        canvas.drawCircle(b.getWidth() / 2, b.getHeight() / 2, radius, paint);

        return this;
    }

    public PictureBuilder build(){
        return this;
    }

    public Bitmap getBitmap() {
        return check();
    }

}
