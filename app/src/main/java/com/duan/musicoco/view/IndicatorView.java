package com.duan.musicoco.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/4/3.
 */

public class IndicatorView extends View {

    private Context mContext;

    private int mDotColor;

    private int mDotSize;
    private int mDotCount;

    private boolean mLineVisible;
    private int mLineColor;
    private int mLineWidth;
    private int mLineLength;

    private int mIndicatorSize;
    private int mIndicatorColor;
    private int mIndicatorPos;
    private boolean mDotClickEnable;

    private boolean mIndicatorDragEnable;
    private boolean mTouchEnable;

    private int mIndicatorSwitchAnim;
    public static final int INDICATOR_SWITCH_ANIM_NONE = 0;
    public static final int INDICATOR_SWITCH_ANIM_TRANSLATION = 1;
    public static final int INDICATOR_SWITCH_ANIM_SQUEEZE = 2;

    private int mIndicatorOrientation;
    public static final int INDICATOR_ORIENTATION_VERTICAL = 0;
    public static final int INDICATOR_ORIENTATION_HORIZONTAL = 1;

    private int mDuration;

    private boolean mChangeLineColorWhileSwitch = true;

    private int[] mIndicatorColors;

    private int defaultDotSize = 8;
    private int defaultIndicatorSize = 15;
    private int defaultLineLength = 40;
    private int minLineWidth = 1;
    private int maxDotCount = 30;
    private int minDotNum = 2;

    private Paint mPaint;

    /**
     * 上下左右边界
     * 由 clickableAreas 定义的边界
     * 拖拽指示点离开上下边界（左右边界）时，使 switchTo 值为边界点的下标，防止数组越界。
     */
    private int mBorderTop;
    private int mBorderBottom;
    private int mBorderRight;
    private int mBorderLeft;

    /**
     * 保存所有小圆点的圆点坐标，用于在touch事件中判断触摸了哪个点
     */
    private int[][] clickableAreas;

    /**
     * 指示点，不断修改它的属性从而实现动画（属性动画）
     */
    private IndicatorHolder indicatorHolder;

    /**
     * 指示点要移动到的目标位置
     */
    private int switchTo = -1;

    /**
     * 辅助 switchTo 变量，
     */
    private int switchToTemp;

    /**
     * 手松开后根据该变量判断是否需要启动切换动画
     */
    private boolean haveIndicatorPressAniming = false;

    /**
     * 指示点是否被拖拽过，当指示点被拖拽了但没有超过当前指示点位置范围时使之回到原位
     */
    private boolean haveIndicatorDraged = false;

    /**
     * 保存转移动画开始时线的颜色
     */
    private int tempLineColor;

    public interface OnDotClickListener {
        /**
         * 小圆点点击事件监听（点击的小圆点不是当前指示点所在位置时才会回调）
         *
         * @param v        view
         * @param position 点击的指示点 0 ~ mDotCount
         */
        void onDotClickChange(View v, int position);
    }

    public interface OnIndicatorPressAnimator {
        /**
         * 自定义指示点挤压时的属性动画
         *
         * @param view   IndicatorView
         * @param target 属性动画操作的目标对象
         * @return 返回定义好的属性动画，动画的启动由IndicatorView自己控制，用户不应该调用Animator.start()
         */
        AnimatorSet onIndicatorPress(IndicatorView view, IndicatorHolder target);
    }

    public interface OnIndicatorSwitchAnimator {
        /**
         * 自定义指示点切换时的属性动画
         *
         * @param view   IndicatorView
         * @param target 属性动画操作的目标对象
         * @return 返回定义好的属性动画，动画的启动由IndicatorView自己控制，用户不应该调用Animator.start()
         */
        AnimatorSet onIndicatorSwitch(IndicatorView view, IndicatorHolder target);
    }

    /**
     * 指示点拖拽状态监听（前提为指示点可拖拽）
     */
    public interface OnIndicatorSeekListener {

        /**
         * 指示点被拖拽时回调
         *
         * @param view     view
         * @param distance 当前指示点位置与最左边（或最下边“纵向视图”）的小圆点的间距
         * @param dotPos   当前指示点靠近的小圆点（松手后指示点会固定到这一位置）
         */
        void onSeekChange(IndicatorView view, int distance, int dotPos);

        /**
         * 指示点开始被拖拽，在指示点触摸回馈动画开始时回调
         *
         * @param view view
         */
        void onStartTrackingTouch(IndicatorView view);

        /**
         * 指示点停止被拖拽，手指离开指示点后立即回调
         *
         * @param view view
         */
        void onSopTrackingTouch(IndicatorView view);

    }

    public interface OnIndicatorChangeListener {
        /**
         * 指示点所在位置改变时回调
         * 注意：若拖动指示点位置从1到3再回到2后松手，则 oldPos 的值始终为1，currentPos 的值依次为 2,3,2
         *
         * @param currentPos 当前所在位置
         * @param oldPos     开始拖动时所在的位置
         */
        void onIndicatorChange(int currentPos, int oldPos);
    }

    private OnIndicatorPressAnimator mPressAnimator;
    private OnIndicatorSwitchAnimator mSwitchAnimator;

    private OnDotClickListener mListener;
    private OnIndicatorSeekListener mSeekListener;
    private OnIndicatorChangeListener mChangeListener;

    public void setOnIndicatorChangeListener(OnIndicatorChangeListener mChangeListener) {
        this.mChangeListener = mChangeListener;
    }

    public void setOnIndicatorSeekListener(OnIndicatorSeekListener mSeekListener) {
        this.mSeekListener = mSeekListener;
    }

    public void setOnIndicatorPressAnimator(OnIndicatorPressAnimator pressAnimator) {
        this.mPressAnimator = pressAnimator;
    }

    public void setOnIndicatorSwitchAnimator(OnIndicatorSwitchAnimator switchAnimator) {
        this.mSwitchAnimator = switchAnimator;
    }

    public void setOnDotClickListener(OnDotClickListener listener) {
        this.mListener = listener;
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        final TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndicatorView, defStyleAttr, 0);

        //默认动画为“挤扁”
        mIndicatorSwitchAnim = array.getInteger(R.styleable.IndicatorView_IndicatorSwitchAnimation, INDICATOR_SWITCH_ANIM_SQUEEZE);

        //默认为 水平
        mIndicatorOrientation = array.getInteger(R.styleable.IndicatorView_indicatorOrientation, INDICATOR_ORIENTATION_HORIZONTAL);

        mTouchEnable = array.getBoolean(R.styleable.IndicatorView_touchEnable, true);
        if (!mTouchEnable) {
            mIndicatorDragEnable = false;
            mDotClickEnable = false;
        } else {
            mIndicatorDragEnable = array.getBoolean(R.styleable.IndicatorView_indicatorDragEnable, true);
            mDotClickEnable = array.getBoolean(R.styleable.IndicatorView_dotClickEnable, true);
        }

        mDotColor = array.getColor(R.styleable.IndicatorView_dotColor, Color.GRAY);
        mLineColor = array.getColor(R.styleable.IndicatorView_lineColor, Color.GRAY);
        mIndicatorColor = array.getColor(R.styleable.IndicatorView_indicatorColor, Color.LTGRAY);

        mDotSize = array.getDimensionPixelSize(R.styleable.IndicatorView_dotSize, defaultDotSize);
        mLineLength = array.getDimensionPixelSize(R.styleable.IndicatorView_lineLength, defaultLineLength);
        mLineWidth = array.getDimensionPixelSize(R.styleable.IndicatorView_lineWidth, minLineWidth);
        mIndicatorSize = array.getDimensionPixelSize(R.styleable.IndicatorView_indicatorSize, defaultIndicatorSize);
        //因为在onDraw中绘制指示点时会通过 indicatorHolder.getWidth() / 2 使两点间切换动画播放过程中椭圆边界不超过 mLineLength * Math.abs(switchTo - mIndicatorPos) + mIndicatorSize
        //因而这里乘以 2 否则绘制出来的大小会只有实际大小的一半
        mIndicatorSize *= 2;
        //默认动画时间为500ms
        mDuration = array.getInteger(R.styleable.IndicatorView_duration, 500);
        mDotCount = array.getInteger(R.styleable.IndicatorView_dotNum, 3);
        //判断小圆点个数是否超过上限30
        mDotCount = mDotCount > maxDotCount ? maxDotCount : mDotCount < minDotNum ? minDotNum : mDotCount;
        mIndicatorPos = array.getInteger(R.styleable.IndicatorView_indicatorPos, 0);
        //在 xml 中指定位置时从 1 开始
        mIndicatorPos = mIndicatorPos == 0 ? 0 : mIndicatorPos - 1;
        mLineVisible = array.getBoolean(R.styleable.IndicatorView_lineVisible, true);

        //释放资源
        array.recycle();

        mIndicatorColors = new int[mDotCount];
        for (int i = 0; i < mIndicatorColors.length; i++) {
            mIndicatorColors[i] = mIndicatorColor;
        }
        mPaint = new Paint();
        clickableAreas = new int[mDotCount][2];
        indicatorHolder = new IndicatorHolder();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        //默认的小圆点触摸反馈动画会放大小圆点，多留些空间给缩放动画（
        // 如果你发现你的小圆点在一些情况下显示不全时可在xml中增大padding或修改下面两个变量的值
        int expandTerminalPadding = getPaddingLeft() + mIndicatorSize / 6;//左右两端
        int expandSidePadding = getPaddingTop() + mIndicatorSize / 5;//上下两侧

        setPadding(expandTerminalPadding, expandSidePadding, expandTerminalPadding, expandSidePadding);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {//xml中宽度设为warp_content
            if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) //纵向
                width = getPaddingLeft() + getPaddingRight() + mIndicatorSize;
            else
                width = getPaddingLeft() + ((mDotCount - 1) * mLineLength + mIndicatorSize) + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) //纵向
                height = ((mDotCount - 1) * mLineLength + mIndicatorSize) + getPaddingBottom() + getPaddingTop();
            else
                height = getPaddingTop() + mIndicatorSize + getPaddingBottom();
        }

        width = Math.max(width, getMinimumWidth());
        height = Math.max(height, getMinimumHeight());

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //计算出上下左右边界
        if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) {
            mBorderTop = getPaddingTop() + mIndicatorSize / 2;
            mBorderBottom = mBorderTop + mLineLength * (mDotCount - 1);
        } else {
            mBorderLeft = getPaddingLeft() + mIndicatorSize / 2;
            mBorderRight = mBorderLeft + mLineLength * (mDotCount - 1);
        }

        //在 onLayout 中获取 View 的测量高和测量宽
        if (indicatorHolder != null) {
            indicatorHolder.setColor(mIndicatorColors[mIndicatorPos]);
            if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向
                indicatorHolder.setCenterX(getMeasuredWidth() / 2);
                indicatorHolder.setCenterY(mIndicatorPos * mLineLength + getPaddingBottom() + mIndicatorSize / 2);
            } else {
                indicatorHolder.setCenterX(mIndicatorPos * mLineLength + getPaddingLeft() + mIndicatorSize / 2);
                indicatorHolder.setCenterY(getMeasuredHeight() / 2);
            }
            indicatorHolder.setHeight(mIndicatorSize);
            indicatorHolder.setWidth(mIndicatorSize);
            indicatorHolder.setAlpha(255);
        }

    }

    //画线段
    private void drawLine(Canvas canvas) {
        mPaint.setColor(mLineColor);

        if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向，从下往上绘制
            for (int i = 0; i < mDotCount - 1; i++) {
                int top = getHeight() - (getPaddingBottom() + mIndicatorSize / 2 + mLineLength * (i + 1));
                int bottom = getHeight() - (getPaddingBottom() + mIndicatorSize / 2 + mLineLength * i);
                int left = (getWidth() - mLineWidth) / 2;
                int right = (getWidth() + mLineWidth) / 2;

                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        } else { //纵向，从左往右绘制
            for (int i = 0; i < mDotCount - 1; i++) {
                int top = (getHeight() - mLineWidth) / 2;
                int bottom = (getHeight() + mLineWidth) / 2;
                int left = getPaddingLeft() + mIndicatorSize / 2 + mLineLength * i;
                int right = getPaddingLeft() + mIndicatorSize / 2 + mLineLength * (i + 1);

                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    //画小圆点
    private void drawDots(Canvas canvas) {

        switchToTemp = switchTo;//用于 mChangeListener 回调判断，见 onTouchEvent 方法

        if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向 从上往下绘制
            for (int i = 0; i < clickableAreas.length; i++) {
                int cx = getWidth() / 2;
                int cy = i * mLineLength + getPaddingBottom() + mIndicatorSize / 2;

                if (switchTo != -1 && i == switchTo) {
                    mPaint.setColor(mIndicatorColors[switchTo]);
                } else
                    mPaint.setColor(mDotColor);

                canvas.drawCircle(cx, cy, mDotSize / 2, mPaint);
                clickableAreas[i][0] = cx;
                clickableAreas[i][1] = cy;
            }
        } else {
            for (int i = 0; i < clickableAreas.length; i++) {
                int cx = i * mLineLength + getPaddingLeft() + mIndicatorSize / 2;
                int cy = getHeight() / 2;

                if (switchTo != -1 && i == switchTo) {
                    mPaint.setColor(mIndicatorColors[switchTo]);
                } else
                    mPaint.setColor(mDotColor);
                canvas.drawCircle(cx, cy, mDotSize / 2, mPaint);

                clickableAreas[i][0] = cx;
                clickableAreas[i][1] = cy;
            }
        }
    }

    //画指示点
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawIndicator(Canvas canvas) {
        mPaint.setColor(indicatorHolder.getColor());
        mPaint.setAlpha(indicatorHolder.getAlpha());

        canvas.drawOval(
                indicatorHolder.getCenterX() - indicatorHolder.getWidth() / 2,
                indicatorHolder.getCenterY() - indicatorHolder.getHeight() / 2,
                indicatorHolder.getCenterX() + indicatorHolder.getWidth() / 2,
                indicatorHolder.getCenterY() + indicatorHolder.getHeight() / 2,
                mPaint
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //去锯齿
        mPaint.setAntiAlias(true);

        //画线段（如果可见）
        if (mLineVisible) {
            drawLine(canvas);
        }

        //画小圆点
        drawDots(canvas);

        //画指示点
        drawIndicator(canvas);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!mTouchEnable)
            return true;

        //动画正在进行时不在响应点击事件
        if (haveIndicatorPressAniming)
            return true;

        int ex = (int) event.getX();
        int ey = (int) event.getY();
        int temp = mLineLength / 2;
        switchTo = 0;
        //判断当前手指所在的小圆点是哪个
        if (mIndicatorOrientation != INDICATOR_ORIENTATION_VERTICAL) { //横向
            for (; switchTo < mDotCount; switchTo++) {
                int[] xy = clickableAreas[switchTo];
                //只对x坐标位置进行判断，这样即使用户手指在控件外面（先在控件内触摸后不抬起而是滑到控件外面）滑动也能判断
                if (ex <= xy[0] + temp && ex >= xy[0] - temp) {
                    break;
                }
            }
            //超出边界时要检查
            if (switchTo == mDotCount)
                if (indicatorHolder.getCenterX() > mBorderRight)
                    switchTo -= 1;
                else
                    switchTo = 0;
        } else {
            for (; switchTo < mDotCount; switchTo++) {
                int[] xy = clickableAreas[switchTo];
                //只对y坐标位置进行判断，这样即使用户手指在控件外面（先在控件内触摸后不抬起而是滑到控件外面）滑动也能判断
                if (ey <= xy[1] + temp && ey >= xy[1] - temp) {
                    break;
                }
            }
            if (switchTo == mDotCount)
                if (indicatorHolder.getCenterY() > mBorderTop)
                    switchTo -= 1;
                else
                    switchTo = 0;

        }

        if (switchTo != switchToTemp && switchTo != mIndicatorPos) {
            if (mChangeListener != null)
                mChangeListener.onIndicatorChange(switchTo, mIndicatorPos);
        }

        if (switchTo != mIndicatorPos && !mDotClickEnable && !haveIndicatorDraged) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //按下且不是指示点所在的小圆点
            if (mIndicatorPos != switchTo) {
                startSwitchAnimation();
                if (mListener != null)
                    mListener.onDotClickChange(this, switchTo);
            } else {//按下且是指示点所在的小圆点
                if (mIndicatorDragEnable)
                    startPressAnimation();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) { //手抬起
            if (mIndicatorDragEnable)
                if (mSeekListener != null)
                    mSeekListener.onSopTrackingTouch(this);

            if (switchTo != mIndicatorPos || haveIndicatorDraged) {
                haveIndicatorDraged = false;
                if (mIndicatorDragEnable)
                    startSwitchAnimation();
            }
        } else { //按着+拖拽
            if (mIndicatorDragEnable) {
                haveIndicatorDraged = true;
                if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向
                    indicatorHolder.setCenterY(ey);
                    if (mSeekListener != null)
                        mSeekListener.onSeekChange(this, (getHeight() - (getPaddingBottom() + mIndicatorSize / 2)) - indicatorHolder.getCenterY(), switchTo);
                } else {
                    indicatorHolder.setCenterX(ex);
                    if (mSeekListener != null)
                        mSeekListener.onSeekChange(this, indicatorHolder.getCenterX() - (getPaddingLeft() + mIndicatorSize / 2), switchTo);
                }
            }
        }

        return true;
    }

    /**
     * 指示点触摸（挤压）动画
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPressAnimation() {
        if (mPressAnimator == null) {
            //缩放
            int terminal = mIndicatorSize;
            int center = mIndicatorSize * 3 / 2;
            ValueAnimator scaleAnimH = ObjectAnimator.ofInt(indicatorHolder, "height", terminal, center, terminal);
            ValueAnimator scaleAnimW = ObjectAnimator.ofInt(indicatorHolder, "width", terminal, center, terminal);

            //颜色渐变
            int terminalColor = mIndicatorColor;
            int centerColor = mDotColor;
            ValueAnimator colorAnim = ObjectAnimator.ofArgb(indicatorHolder, "color", terminalColor, centerColor, terminalColor);

            AnimatorSet defaultIndicatorPressAnim = new AnimatorSet();
            defaultIndicatorPressAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    haveIndicatorPressAniming = true;
                    if (mIndicatorDragEnable)
                        if (mSeekListener != null)
                            mSeekListener.onStartTrackingTouch(IndicatorView.this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    haveIndicatorPressAniming = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    haveIndicatorPressAniming = false;

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            defaultIndicatorPressAnim.setDuration(500);
            defaultIndicatorPressAnim.play(scaleAnimH).with(scaleAnimW).with(colorAnim);
            defaultIndicatorPressAnim.start();
        } else { //自定义动画
            AnimatorSet customfAnim = mPressAnimator.onIndicatorPress(this, indicatorHolder);
            customfAnim.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    haveIndicatorPressAniming = true;
                    if (mIndicatorDragEnable)
                        if (mSeekListener != null)
                            mSeekListener.onStartTrackingTouch(IndicatorView.this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    haveIndicatorPressAniming = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    haveIndicatorPressAniming = false;

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            //进行挤压动画时控件不再响应触摸事件，因而动画时间不能太长
            customfAnim.setDuration(customfAnim.getDuration() > 700 ? 700 : customfAnim.getDuration());
            customfAnim.start();
        }

    }

    /**
     * 指示点切换动画
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startSwitchAnimation() {

        //平移
        int end;
        ValueAnimator trainsAnim;
        ValueAnimator colorAnim;
        if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向
            int start = indicatorHolder.getCenterY();
            end = switchTo * mLineLength + getPaddingBottom() + mIndicatorSize / 2;
            trainsAnim = ObjectAnimator.ofInt(indicatorHolder, "centerY", start, end);
        } else {
            int start = indicatorHolder.getCenterX();
            end = switchTo * mLineLength + getPaddingLeft() + mIndicatorSize / 2;
            trainsAnim = ObjectAnimator.ofInt(indicatorHolder, "centerX", start, end);
        }

        //颜色渐变
        int startColor = indicatorHolder.getColor();
        int endColor = mIndicatorColors[switchTo];
        colorAnim = ObjectAnimator.ofArgb(indicatorHolder, "color", startColor, endColor);

        AnimatorSet movingAnim = new AnimatorSet();
        movingAnim.setDuration(mDuration);
        movingAnim.play(trainsAnim).with(colorAnim);

        tempLineColor = mLineColor;
        AnimatorSet defaultIndicatorSwitchAnim = new AnimatorSet();
        defaultIndicatorSwitchAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mChangeLineColorWhileSwitch)
                    mLineColor = mIndicatorColors[switchTo];
                haveIndicatorPressAniming = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (mSwitchAnimator == null) {
            switch (mIndicatorSwitchAnim) {
                case INDICATOR_SWITCH_ANIM_NONE:
                    if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) //纵向
                        indicatorHolder.setCenterY(end);
                    else
                        indicatorHolder.setCenterX(end);
                    animEnd();
                    break;
                case INDICATOR_SWITCH_ANIM_SQUEEZE:
                    //“挤扁”
                    ValueAnimator heightAnim;
                    ValueAnimator widthAnim;
                    int centerH;
                    int centerW;
                    if (mIndicatorOrientation == INDICATOR_ORIENTATION_VERTICAL) { //纵向
                        centerH = Math.abs(indicatorHolder.getCenterY() - clickableAreas[switchTo][1]);
                        centerW = mLineWidth;
                    } else {
                        centerH = mLineWidth;
                        //indicatorHolder.getCenterX()的值在动画过程中会被trainsAnim动画不断改变
                        //centerW：指示点当前所在位置和目标点间的距离
                        centerW = Math.abs(indicatorHolder.getCenterX() - clickableAreas[switchTo][0]);
                    }
                    heightAnim = ObjectAnimator.ofInt(indicatorHolder, "height", mIndicatorSize, centerH, 0);
                    widthAnim = ObjectAnimator.ofInt(indicatorHolder, "width", mIndicatorSize, centerW, 0);
                    heightAnim.setDuration(mDuration);
                    widthAnim.setDuration(mDuration);

                    //缩放
                    ValueAnimator scaleAnimH = ObjectAnimator.ofInt(indicatorHolder, "height", mDotSize, mIndicatorSize);
                    ValueAnimator scaleAnimW = ObjectAnimator.ofInt(indicatorHolder, "width", mDotSize, mIndicatorSize);
                    AnimatorSet scaleSet = new AnimatorSet();
                    scaleSet.play(scaleAnimH).with(scaleAnimW);
                    scaleSet.setDuration(500);

                    defaultIndicatorSwitchAnim.play(movingAnim).with(heightAnim).with(widthAnim);
                    defaultIndicatorSwitchAnim.play(scaleSet).after(movingAnim);
                    defaultIndicatorSwitchAnim.start();
                    break;
                case INDICATOR_SWITCH_ANIM_TRANSLATION:
                    defaultIndicatorSwitchAnim.play(movingAnim);
                    defaultIndicatorSwitchAnim.start();
                    break;
            }

        } else { //自定义
            tempLineColor = mLineColor;
            AnimatorSet customAnim = mSwitchAnimator.onIndicatorSwitch(this, indicatorHolder);
            customAnim.play(movingAnim);
            customAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mLineColor = indicatorHolder.getColor();
                    haveIndicatorPressAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animEnd();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            customAnim.start();
        }

    }

    /**
     * 指示点切换动画结束或取消时重置和恢复一些变量的值
     */
    private void animEnd() {
        mLineColor = tempLineColor;
        mIndicatorPos = switchTo;
        switchTo = -1;
        haveIndicatorPressAniming = false;
    }

    /**
     * 属性动画的目标对象类-指示点，属性动画通过不断调用该类的setXXX方法改变指示点的属性值并重绘控件以实现动画
     */
    public class IndicatorHolder {
        private int centerX;
        private int centerY;
        private int height;
        private int color;
        private int width;
        private int alpha;

        public void setAlpha(int alpha) {
            this.alpha = alpha;
            invalidate();
        }

        public int getAlpha() {

            return alpha;
        }

        public void setHeight(int height) {
            this.height = height / 2;
            invalidate();
        }

        public void setWidth(int width) {
            this.width = width / 2;
            invalidate();
        }

        public void setCenterY(int centerY) {
            this.centerY = centerY;
            invalidate();
        }

        public void setColor(int color) {
            this.color = color;
            invalidate();
        }

        public void setCenterX(int centerX) {
            this.centerX = centerX;
            invalidate();
        }

        public int getColor() {
            return color;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setIndicatorPos(int indicatorPos) {
        if (indicatorPos != mIndicatorPos) {
            switchTo = indicatorPos;
            startSwitchAnimation();
        }
    }

    public void setDotColor(int dotColor) {
        this.mDotColor = dotColor;
        invalidate();
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        tempLineColor = mLineColor;
        invalidate();
    }

    public void setLineVisible(boolean lineVisible) {
        this.mLineVisible = lineVisible;
        invalidate();
    }

    public void setLineHeight(int lineHeight) {
        this.mLineWidth = lineHeight;
        invalidate();
    }

    /**
     * 在进行指示点切换过程中是否改变线段颜色
     *
     * @param chage false 为不改变
     */
    public void changeLineColorWhileSwitch(boolean chage) {
        this.mChangeLineColorWhileSwitch = chage;
    }

    /**
     * 将指示点在各个位置的颜色全部设置为同一颜色
     *
     * @param color 颜色
     */
    public void setIndicatorColor(int color) {
        this.mIndicatorColor = color;
        this.indicatorHolder.setColor(color);
        for (int i = 0; i < mIndicatorColors.length; i++) {
            mIndicatorColors[i] = color;
        }
        invalidate();
    }

    /**
     * 修改指定位置处指示点的颜色
     *
     * @param index 下标 （“水平视图”时从左往右依次为 0 - n，“纵向视图”时从上往下依次为 0 - n）
     * @param color 颜色
     */
    public void setIndicatorColor(int index, int color) {
        mIndicatorColors[index] = color;
        invalidate();
    }

    /**
     * 为所有指示点设置颜色
     *
     * @param colors 颜色值
     */
    public void setIndicatorColor(int... colors) {

        if (colors.length < mDotCount) {
            for (int i = 0; i < colors.length; i++) {
                mIndicatorColors[i] = colors[i];
            }
        } else {
            for (int i = 0; i < mDotCount; i++) {
                mIndicatorColors[i] = colors[i];
            }
        }

        invalidate();
    }

    public void setIndicatorSwitchAnim(int anim) {
        if (anim >= INDICATOR_SWITCH_ANIM_NONE && anim <= INDICATOR_SWITCH_ANIM_SQUEEZE)
            this.mIndicatorSwitchAnim = anim;
    }

    public int getDotColor() {
        return mDotColor;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public boolean isLineVisible() {
        return mLineVisible;
    }

    public int getDotPixelSize() {
        return mDotSize;
    }

    public int getLinePixelWidth() {
        return mLineLength;
    }

    public int getLinePixelHeight() {
        return mLineWidth;
    }

    public int[] getIndicatorColors() {
        return mIndicatorColors;
    }

    /**
     * 获得指示点的颜色值，该颜色值只有在 xml 中指定或调用{@link #setIndicatorColor(int color)}方法时才会改变
     * 如果你想获得当前指示点的颜色值应使用{@link #getCurrentIndicatorColor()}或
     * 使用{@link #getIndicatorColors()}方法获得所有指示点在对应位置的颜色值
     */
    @Deprecated
    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public int getCurrentIndicatorColor() {
        return mIndicatorColors[mIndicatorPos];
    }

    public int getIndicatorPixeSize() {
        return mIndicatorSize;
    }

    public int getDotCount() {
        return mDotCount;
    }

    public int getIndicatorPos() {
        return mIndicatorPos;
    }

    public int getmIndicatorOrientation() {
        return mIndicatorOrientation;
    }

}
