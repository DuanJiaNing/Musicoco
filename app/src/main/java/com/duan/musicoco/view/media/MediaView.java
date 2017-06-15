package com.duan.musicoco.view.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/6/14.
 */

public abstract class MediaView extends View {

    protected float centerX;
    protected float centerY;

    //半径
    protected int radius;
    protected int minRadius;
    protected int shadowRadius;

    //颜色
    protected int strokeColor;

    protected int strokeWidth;
    protected int minStrokeWidth;

    protected Paint paint;

    private boolean isPressing = false;
    private int isClick = 0;

    protected Context context;

    protected OnClickListener listener;

    private void init() {
        minRadius = 20;
        minStrokeWidth = 1;
        strokeWidth = minStrokeWidth;
        radius = minRadius;

        strokeColor = Color.GRAY;
        shadowRadius = 5;

        paint = new Paint();
        paint.setAntiAlias(true);

    }

    public MediaView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MediaView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MediaView, defStyleAttr, 0);

        radius = array.getDimensionPixelSize(R.styleable.MediaView_radius, minRadius);
        shadowRadius = array.getDimensionPixelSize(R.styleable.MediaView_shadowRadius, 5);
        strokeColor = array.getColor(R.styleable.MediaView_strokeColor, strokeColor);
        strokeWidth = array.getDimensionPixelSize(R.styleable.MediaView_strokeWidth, strokeWidth);

        array.recycle();

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
        int wp = getPaddingLeft() + getPaddingRight();
        int hp = getPaddingTop() + getPaddingBottom();

        int te = -1;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            te = 0;
        } else {//xml中宽度设为warp_content
            width = radius * 2 + t + wp;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            te = 0;
        } else {
            height = radius * 2 + t + hp;
        }

        width = Math.max(width, getMinimumWidth());
        height = Math.max(height, getMinimumHeight());
        setMeasuredDimension(width, height);

        if (te != -1)
            radius = (Math.min(width - wp, height - hp) - t) / 2;

    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (l != null)
            this.listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressing = true;
                isClick = 0;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                isClick++;
                break;
            case MotionEvent.ACTION_UP:
                isPressing = false;
                invalidate();
                if (isClick < 5)
                    if (listener != null)
                        listener.onClick(this);
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }


    protected void drawOuter(Canvas canvas) {

        if (strokeWidth == 0)
            return;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(strokeColor);

        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    protected abstract void drawInside(Canvas canvas);

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawARGB(0, 0, 0, 0);

        //只绘制外阴影和图形内容本身，不绘制内阴影
        paint.setMaskFilter(new BlurMaskFilter(isPressing ? shadowRadius : 0.1f, BlurMaskFilter.Blur.SOLID));

        drawOuter(canvas);

        drawInside(canvas);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        invalidate();
    }

    public void setShadowRadius(int shadowRadius) {
        this.shadowRadius = shadowRadius;
        invalidate();
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        invalidate();
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    public int getRadius() {
        return radius;
    }

    public int getShadowRadius() {
        return shadowRadius;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public boolean isPressing() {
        return isPressing;
    }
}
