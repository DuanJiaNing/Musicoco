package com.duan.musicoco.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.duan.musicoco.util.BitmapUtils;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * <p>
 */

public class BitmapBuilder {

    private int radius = -1;

    private String path;

    private Bitmap bitM;

    private final Context context;

    private final Paint paint;

    public BitmapBuilder(Context context) {
        this.context = context;
        this.paint = new Paint();
        paint.setAntiAlias(true);
    }

    public BitmapBuilder setPath(String fillPath) {
        this.path = fillPath;
        return this;
    }

    /**
     * 等长等宽进行压缩
     *
     * @param size 长度
     */
    public BitmapBuilder resize(int size) {
        if (path == null)
            return this;

        this.radius = size;
        bitM = BitmapUtils.bitmapResizeFromFile(path, radius * 2, radius * 2);
        return this;
    }

    public BitmapBuilder resizeForDefault(int reqWidth, int reqHeight, int resID) {
        bitM = BitmapUtils.bitmapResizeFromResource(context.getResources(), resID, reqWidth, reqHeight);
        return this;
    }

    /**
     * 长宽不等进行压缩
     */
    public BitmapBuilder resize(int reqWidth, int reqHeight) {
        bitM = BitmapUtils.bitmapResizeFromFile(path, reqWidth, reqHeight);
        return this;
    }

    public BitmapBuilder toRoundBitmap() {
        if (bitM == null)
            return this;

        bitM = BitmapUtils.getCircleBitmap(bitM);
        return this;
    }

    public BitmapBuilder toRoundBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return this;

        bitM = BitmapUtils.getCircleBitmap(bitmap);
        return this;
    }

    public BitmapBuilder jpgToPng() {
        bitM = BitmapUtils.jpgToPng(context, bitM);
        return this;
    }

    /**
     * 在图片外面绘制圆圈，该方法不应该与{@link #resize(int, int)}一起使用
     *
     * @param space       与图片或上一个圆圈的间距
     * @param strokeWidth 圆圈宽度
     * @param color       圆圈颜色
     */
    public BitmapBuilder addOuterCircle(int space, int strokeWidth, int color) {

        Bitmap b = bitM;

        int radius = Math.max(b.getWidth(), b.getHeight()) / 2;

        int newWidth = b.getWidth() + space * 2 + strokeWidth * 2;
        int newHeight = b.getHeight() + space * 2 + strokeWidth * 2;

        Bitmap temp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        int cx = temp.getWidth() / 2;
        int cy = temp.getHeight() / 2;

        Canvas canvas = new Canvas(temp);
        canvas.drawBitmap(b, cx - b.getWidth() / 2, cy - b.getHeight() / 2, paint);

        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(cx, cy, radius + space, paint);

        bitM = temp;

        return this;
    }

    public BitmapBuilder build() {
        return this;
    }

    public Bitmap getBitmap() {
        return bitM;
    }

    public void reset() {
        radius = -1;
        setPath(null);
        bitM = null;
    }
}
