package com.duan.musicoco.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.duan.musicoco.R;

import static com.duan.musicoco.list.ListActivity.TAG;

/**
 * Created by DuanJiaNing on 2017/6/14.
 */

public class SkipView extends View {

    //半径
    private int radius;
    private final int shadowRadius;

    //颜色
    private int strokeColor;
    private int shadowColor;
    private int triangleColor;

    private int strokeWidth;
    private int triangleWidth;

    private Paint paint;

    private boolean isPressing = false;

    private Context context;

    private void init() {
        radius = 100;

        strokeColor = context.getResources().getColor(R.color.colorPrimary);
        shadowColor = strokeColor;
        triangleColor = strokeColor;

        strokeWidth = 2;
        triangleWidth = strokeWidth;

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public SkipView(Context context) {
        super(context);
        this.context = context;
        this.shadowRadius = 5;
        init();
    }

    public SkipView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.shadowRadius = 5;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        int t = (shadowRadius + strokeWidth) * 2;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize + t;
        } else {//xml中宽度设为warp_content
            width = radius * 2 + t;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize + t;
        } else {
            height = radius * 2 + t;
        }

        width = Math.max(width, getMinimumWidth());
        height = Math.max(height, getMinimumHeight());

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressing = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                isPressing = false;
                invalidate();
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    private void drawStroke(Canvas canvas) {

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(strokeColor);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        //计算第一象限的圆上的点
        float[][] ps = new float[radius + 1][2];
        int j = 0;
        float r2 = radius * radius;
        for (int i = cx; i <= cx + radius; i++) {
            float x = i;
            float y = (float) (Math.sqrt(r2 - Math.pow(x - cx, 2)) + cy);
            ps[j][0] = x;
            ps[j][1] = y;
            j++;
        }

        paint.setShadowLayer(isPressing ? shadowRadius : 0, 0, 0, shadowColor);
        float x, x0, y, y0;

        //绘制第一象限
        x0 = ps[0][0];
        y0 = ps[0][1];
        for (int i = 1; i < ps.length; i++) {
            x = ps[i][0];
            y = ps[i][1];
            canvas.drawLine(x0, y0, x, y, paint);
            x0 = x;
            y0 = y;
        }

        //绘制第二象限
        x0 = ps[0][0];
        y0 = ps[0][1];
        for (int i = 1; i < ps.length; i++) {
            x = cx - (ps[i][0] - cx);
            y = ps[i][1];
            canvas.drawLine(x0, y0, x, y, paint);
            x0 = x;
            y0 = y;
        }

        //绘制第三象限
        x0 = ps[0][0];
        y0 = cy - (ps[0][1] - cy);
        for (int i = 1; i < ps.length; i++) {
            x = cx - (ps[i][0] - cx);
            y = cy - (ps[i][1] - cy);
            canvas.drawLine(x0, y0, x, y, paint);
            x0 = x;
            y0 = y;
        }

        //绘制第四象限
        x0 = ps[0][0];
        y0 = cy - (ps[0][1] - cy);
        for (int i = 1; i < ps.length; i++) {
            x = ps[i][0];
            y = cy - (ps[i][1] - cy);
            canvas.drawLine(x0, y0, x, y, paint);
            x0 = x;
            y0 = y;
        }

    }

    private void drawTriangle(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(triangleColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawARGB(0, 0, 0, 0);

        drawStroke(canvas);

        drawTriangle(canvas);
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setTriangleColor(int triangleColor) {
        this.triangleColor = triangleColor;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setTriangleWidth(int triangleWidth) {
        this.triangleWidth = triangleWidth;
    }

}
