package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.duan.musicoco.util.BitmapUtil;

/**
 * Created by DuanJiaNing on 2017/6/13.
 */

public class AlbumPicture {

    public static class Builder {
        private int radius = 0;
        private Bitmap bitmap;
        private String path;
        private Context context;

        public Builder(Context context, int radius, String fillPath) {
            this.radius = radius;
            this.path = fillPath;
            this.context = context;
        }

        public Builder resize() {
            bitmap = BitmapUtil.bitmapResizeFromFile(path, radius * 2, radius * 2);
            return this;
        }

        /**
         * 如果需要生成的图片拥有透明层（png），则确保在此之前调用 {@link #jpg2png()} 方法
         */
        public Builder toRoundBitmap() {
            check();
            bitmap = BitmapUtil.getCircleBitmap(bitmap);
            return this;
        }

        public Builder jpg2png() {
            check();
            bitmap = BitmapUtil.jpgTopng(bitmap, context);
            return this;
        }

        private void check() {
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(path);
            }
        }

        //在外面画一个圈 FIXME
        public Builder addOuterCircle(int strokeWidth, int color) {
            check();

            radius += strokeWidth;

            Paint paint = new Paint();
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, radius, paint);

            return this;
        }

        public void build(View view) {
            check();
            if (view instanceof ImageView) {
                ((ImageView) view).setImageBitmap(bitmap);
            } else
                view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
        }

        public Bitmap build() {
            check();
            return bitmap;
        }

    }

}
