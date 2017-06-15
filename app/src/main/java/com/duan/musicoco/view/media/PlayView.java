package com.duan.musicoco.view.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/6/15.
 */

public class PlayView extends SkipView {

    //true 表示正在播放，此时应显示双竖线（暂停）
    private boolean isPlaying = false;

    private int pauseLineDistance;
    private int pauseLineWidth;
    private int pauseLineHeight;
    private int pauseLineRadius;
    private int pauseLineColor;
    private boolean pauseLineHollow;
    private int pauseLineStroke;

    public PlayView(Context context) {
        super(context);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayView, defStyleAttr, 0);

        pauseLineDistance = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineDistance, 1);
        pauseLineWidth = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineWidth, 1);
        pauseLineHeight = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineHeight, triangleHeight);
        pauseLineRadius = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineRadius, triangleRadius);
        pauseLineColor = array.getColor(R.styleable.PlayView_pauseLineColor, strokeColor);
        pauseLineHollow = array.getBoolean(R.styleable.PlayView_pauseLineHollow,false);
        pauseLineStroke = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineStroke, strokeWidth);

        //设置为 0 ，两条竖线间距和线高宽由 pauseLineDistance ， pauseLineWidth 和 pauseLineHeight 控制
        distance = 0;
        innerLineWidth = 0;
        innerLineHeight = 0;

        array.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void drawInside(Canvas canvas) {
        if (isPlaying) {
            drawLine(canvas);
        } else {
            super.drawTriangle(canvas);
        }
    }

    @Override
    protected void drawLine(Canvas canvas) {
        if (pauseLineHollow){
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(pauseLineStroke);
        } else
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(pauseLineColor);

        //绘制竖线
        float left = centerX - pauseLineDistance / 2 - pauseLineWidth;
        float top = centerY - pauseLineHeight / 2;
        float right = left + pauseLineWidth;
        float bottom = top + pauseLineHeight;
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectF, pauseLineRadius, pauseLineRadius, paint);

        float l = right + pauseLineDistance;
        float t = top;
        float r = l + pauseLineWidth;
        float b = bottom;
        RectF rf = new RectF(l, t, r, b);
        canvas.drawRoundRect(rf, pauseLineRadius, pauseLineRadius, paint);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlayStatus(boolean playStatus) {
        this.isPlaying = playStatus;
        invalidate();
    }
}
