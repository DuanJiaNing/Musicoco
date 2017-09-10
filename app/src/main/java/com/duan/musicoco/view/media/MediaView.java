package com.duan.musicoco.view.media;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duan.musicoco.R;


/**
 * Created by DuanJiaNing on 2017/6/14.
 * 媒体控件抽象基类
 */

public abstract class MediaView extends View implements ValueAnimator.AnimatorUpdateListener {

    //绘制部分中小坐标（圆心坐标）
    protected float mCenterX;
    protected float mCenterY;

    //空心
    private boolean mHollow;

    //圆圈颜色
    protected int solidColor;

    //半径
    protected int radius;

    //阴影半径
    protected int shadowRadius;
    protected int shadowRadiusForAnim = 1;

    /**
     * 圆圈描边宽度（只在空心时有效），赋值为 0 可取消圆圈的绘制
     */
    protected int strokeWidth;

    protected Paint paint;

    protected Context context;

    private ValueAnimator pressAnim;
    private ValueAnimator releaseAnim;

    //单击时:挤压动画（pressAnim）结束后是否自动开始释放动画（releaseAnim）
    private boolean autoRelease = false;
    //当前是否处于可释放状态（pressAnim已经开始）
    private boolean releasable = false;

    protected final int defaultColor = Color.GRAY;

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

        radius = array.getDimensionPixelSize(R.styleable.MediaView_radius, radius);
        shadowRadius = array.getDimensionPixelSize(R.styleable.MediaView_shadowRadius, shadowRadius);
        strokeWidth = array.getDimensionPixelSize(R.styleable.MediaView_strokeWidth, strokeWidth);
        mHollow = array.getBoolean(R.styleable.MediaView_hollow, true);
        solidColor = array.getColor(R.styleable.MediaView_solidColor, solidColor);

        array.recycle();

        //measure 会用到下面变量的值，应在这里确定值，而不应该是 onLayout 中
        if (shadowRadius <= 0) {
            shadowRadius = 1;
        }

        if (strokeWidth <= 0) {
            strokeWidth = 0;
        }

        // shadowRadius 的值在这里才被正真确定，所以要在这里更新动画的属性值
        updateAnim();

    }

    @Override
    @CallSuper
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mCenterX = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        mCenterY = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        int t = (shadowRadius + (mHollow ? strokeWidth : 0)) * 2;
        int wp = getPaddingLeft() + getPaddingRight();
        int hp = getPaddingTop() + getPaddingBottom();

        boolean ignoreRadius = false;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            ignoreRadius = true;
        } else {//xml中宽度设为warp_content
            width = radius * 2 + t + wp;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            ignoreRadius = true;
        } else {
            height = radius * 2 + t + hp;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            width = Math.max(width, getMinimumWidth());
            height = Math.max(height, getMinimumHeight());
        }

        setMeasuredDimension(width, height);

        //长宽任一者指定具体长度（EXACTLY）时，xml 中设置的 radius 失效
        if (ignoreRadius) {
            radius = (Math.min(width - wp, height - hp) - t) / 2;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int ac = event.getActionMasked();
        int ex = (int) event.getX();
        int ey = (int) event.getY();
        if (ex < 0 || ey < 0 || ex > getWidth() || ey > getHeight()) {
            release();
            return true;
        }

        switch (ac) {
            case MotionEvent.ACTION_DOWN:
                press();
                //调用 View 的事件监听以使用 View 的 click 和 longClick 监听
                super.onTouchEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
                release();
                //调用 View 的事件监听以使用 View 的 click 和 longClick 监听
                super.onTouchEvent(event);
                break;
        }

        return false;
    }

    private void init() {
        strokeWidth = 1;
        radius = 30;

        solidColor = defaultColor;
        shadowRadius = 5;

        paint = new Paint();
        paint.setAntiAlias(true);

        //Android4.0（API14）之后硬件加速功能就被默认开启了,setMaskFilter 在开启硬件加速的情况下无效，需要关闭硬件加速
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //正真的初始化在 updateAnim 中完成
        pressAnim = ObjectAnimator.ofInt(1, 1);
        releaseAnim = ObjectAnimator.ofInt(1, 1);
        pressAnim.addUpdateListener(this);
        releaseAnim.addUpdateListener(this);
        pressAnim.setDuration(500);
        releaseAnim.setDuration(200);
        pressAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                releasable = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (autoRelease) {
                    releaseAnim.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        releaseAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                releasable = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void updateAnim() {
        pressAnim.setIntValues(1, shadowRadius + shadowRadius * 2 / 3, shadowRadius);
        releaseAnim.setIntValues(shadowRadius, 1);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        shadowRadiusForAnim = value;
        invalidate();
    }

    protected void release() {
        if (pressAnim.isRunning()) {
            //开始动画还没结束而想要停止动画（快速单击）
            //此时需使 pressAnim 在结束时自动开始 releaseAnim 动画
            autoRelease = true;
        } else {
            if (!releaseAnim.isRunning() && releasable) {
                releaseAnim.start();
            }
        }
    }

    protected void press() {
        autoRelease = false;
        if (!pressAnim.isRunning()) {
            pressAnim.start();
        }
    }

    /**
     * 绘制外面的圆圈
     */
    protected void drawBorder(Canvas canvas) {

        if (mHollow && strokeWidth <= 0) {
            return;
        }

        if (mHollow) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
        } else {
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setColor(solidColor);
        canvas.drawCircle(mCenterX, mCenterY, radius, paint);
    }

    /**
     * 绘制圆圈内部自定义的内容
     */
    protected abstract void drawInside(Canvas canvas);

    @Override
    protected final void onDraw(Canvas canvas) {

        canvas.drawARGB(0, 0, 0, 0);

        //只绘制外阴影和图形内容本身，不绘制内阴影
        paint.setMaskFilter(new BlurMaskFilter(shadowRadiusForAnim, BlurMaskFilter.Blur.SOLID));

        drawBorder(canvas);

        drawInside(canvas);

    }

    public void setRadius(int radius) {
        this.radius = radius;
        invalidate();
    }

    public boolean isHollow() {
        return mHollow;
    }

    public void setHollow(boolean hollow) {
        this.mHollow = hollow;
        invalidate();
    }

    @Override
    public int getSolidColor() {
        return solidColor;
    }

    public void setSolidColor(int solidColor) {
        this.solidColor = solidColor;
        invalidate();
    }

    public void setShadowRadius(int shadowRadius) {
        if (shadowRadius <= 1)
            shadowRadius = 1;
        this.shadowRadius = shadowRadius;
        updateAnim();
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

    public int getStrokeWidth() {
        return strokeWidth;
    }

}
