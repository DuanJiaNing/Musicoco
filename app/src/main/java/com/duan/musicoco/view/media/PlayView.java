package com.duan.musicoco.view.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Checkable;

import com.duan.musicoco.R;


/**
 * Created by DuanJiaNing on 2017/6/15.
 * 【播放】
 * 【暂停】
 * 控件由如下几部分组成：
 * 1 暂停状态下的三角形，直接继承自 SkipView
 * 2 播放状态下的双竖线
 */

public class PlayView extends SkipView implements Checkable {

    //true 表示正在播放，此时应显示双竖线（暂停）
    private boolean isPlaying = false;

    //双竖线间距
    private int pauseLineDistance;

    //一条竖线的宽度
    private int pauseLineWidth;

    //竖线的高度（两条竖线的外观时完全一样的）
    private int pauseLineHeight;

    //双竖线圆角
    private int pauseLineRadius;

    //双竖线颜色
    private int pauseLineColor;

    //双竖线是否空心，此时可通过 pauseLineStroke 指定描边宽度
    private boolean pauseLineHollow;
    private int pauseLineStroke;

    public interface OnCheckedChangeListener {
        /**
         * 选中状态改变时回调
         */
        void onCheckedChanged(PlayView view, boolean checked);
    }

    private OnCheckedChangeListener checkedChangeListener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        if (l != null)
            this.checkedChangeListener = l;
    }

    public PlayView(Context context) {
        super(context);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayView, defStyleAttr, 0);

        pauseLineDistance = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineDistance, 0);
        pauseLineWidth = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineWidth, 0);
        pauseLineHeight = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineHeight, 951228);
        pauseLineRadius = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineRadius, triangleRadius);
        pauseLineColor = array.getColor(R.styleable.PlayView_pauseLineColor, solidColor);
        pauseLineHollow = array.getBoolean(R.styleable.PlayView_pauseLineHollow, false);
        isPlaying = array.getBoolean(R.styleable.PlayView_checked, false);
        pauseLineStroke = array.getDimensionPixelSize(R.styleable.PlayView_pauseLineStroke, strokeWidth);

        //设置为 0 ，两条竖线间距和高宽转由 pauseLineDistance ， pauseLineWidth 和 pauseLineHeight 控制
        distance = 0;
        innerLineWidth = 0;
        innerLineHeight = 0;

        array.recycle();

//
//        if (pauseLineHeight <= 0)
//            pauseLineHeight = radius * 3 / 4;
//
//        if (pauseLineWidth <= 0)
//            pauseLineWidth = radius / 10;
//
//        if (pauseLineDistance <= 0)
//            pauseLineDistance = radius * 2 / 5;

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (pauseLineHeight <= 0)
            pauseLineHeight = radius * 3 / 4;
        else if (pauseLineHeight == 951228)
            pauseLineHeight = triangleHeight;

        if (pauseLineWidth <= 0)
            pauseLineWidth = radius / 10;

        if (pauseLineDistance <= 0)
            pauseLineDistance = radius * 2 / 5;

        //赋值不合理，进行重置（这不是此控件预期的展现形式，应根据整体比例合理赋值）
        //只对上限进行处理
        if (pauseLineHeight > radius * 2)
            pauseLineHeight = radius * 2;
        if (pauseLineWidth > radius)
            pauseLineWidth = radius;
        if (pauseLineDistance > radius * 2)
            pauseLineDistance = radius * 2;
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
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPreAnim();
                //调用 View 的事件监听以使用 View 的 OnClickListener 和 longClick 监听
                super.onTouchEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
                startReleaseAnim();
                //状态反转
                toggle();
                //调用 View 的事件监听以使用 View 的 OnClickListener 和 longClick 监听
                super.onTouchEvent(event);
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    protected void drawLine(Canvas canvas) {

        if (pauseLineHollow) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(pauseLineStroke);
        } else
            paint.setStyle(Paint.Style.FILL);

        paint.setColor(pauseLineColor);

        //绘制双竖线
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

    @Override
    public void setChecked(boolean checked) {
        setPlayStatus(checked);
    }

    @Override
    public boolean isChecked() {
        return isPlaying;
    }

    //反转状态
    @Override
    public void toggle() {
        isPlaying = !isPlaying;
        if (checkedChangeListener != null)
            checkedChangeListener.onCheckedChanged(this, isPlaying);
        invalidate();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlayStatus(boolean playStatus) {
        this.isPlaying = playStatus;
        invalidate();
    }


    public int getPauseLineDistance() {
        return pauseLineDistance;
    }

    public int getPauseLineWidth() {
        return pauseLineWidth;
    }

    public int getPauseLineHeight() {
        return pauseLineHeight;
    }

    public int getPauseLineRadius() {
        return pauseLineRadius;
    }

    public int getPauseLineColor() {
        return pauseLineColor;
    }

    public boolean isPauseLineHollow() {
        return pauseLineHollow;
    }

    public int getPauseLineStroke() {
        return pauseLineStroke;
    }

    public void setPauseLineDistance(int pauseLineDistance) {
        this.pauseLineDistance = pauseLineDistance;
        invalidate();
    }

    public void setPauseLineWidth(int pauseLineWidth) {
        this.pauseLineWidth = pauseLineWidth;
        invalidate();
    }

    public void setPauseLineHeight(int pauseLineHeight) {
        this.pauseLineHeight = pauseLineHeight;
        invalidate();
    }

    public void setPauseLineRadius(int pauseLineRadius) {
        this.pauseLineRadius = pauseLineRadius;
        invalidate();
    }

    public void setPauseLineColor(int pauseLineColor) {
        this.pauseLineColor = pauseLineColor;
        invalidate();
    }

    public void setPauseLineHollow(boolean pauseLineHollow) {
        this.pauseLineHollow = pauseLineHollow;
        invalidate();
    }

    public void setPauseLineStroke(int pauseLineStroke) {
        this.pauseLineStroke = pauseLineStroke;
        invalidate();
    }
}
