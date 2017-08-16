package com.duan.musicoco.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/8/14.
 * <p>
 * 指示点的半径是颜色条宽度的 4/3 ,两端会有指示点半径长的距离是空白的，这是为了给指示点滑动到端点时留出足够的显示空间，
 * 这也导致当控件宽高的差值太小时，指示点会完全覆盖住颜色条，使颜色条不可见，<strong>因此应尽量根据控件的不同模式使长
 * 边与段边的比例不小于 3 ： 1</strong>
 * <br>
 * <p>
 * 两种模式：
 * <p>
 * HORIZONTAL 水平
 * <p>
 * VERTICAL 竖直
 */

public class ColorPickerView extends View {

    /**
     * 指示点颜色
     */
    private int mIndicatorColor;
    /**
     * 是否启用指示点
     */
    private boolean mIndicatorEnable;

    /**
     * View 和 bitmapForColor 的画笔
     */
    private final Paint paint;

    /**
     * 指示点专用画笔，这样可以避免 mIndicatorColor 有 alpha 时，alpha 作用于 View
     */
    private final Paint paintForIndicator;

    private LinearGradient linearGradient;

    /**
     * 除去上下 padding 的端点坐标
     */
    private int mTop, mLeft, mRight, mBottom;

    /**
     * 颜色条圆角矩形边界
     */
    private final Rect rect = new Rect();

    /**
     * bitmapForIndicator 在 View 上的绘制位置
     */
    private final Rect rectForIndicator = new Rect();

    /**
     * 指示点半径
     */
    private int mRadius;

    /**
     * 控件方向
     */
    private Orientation orientation;

    // 默认状态下长边与短边的比例为 6 ：1
    private static final int defaultSizeShort = 70; // * 6
    private static final int defaultSizeLong = 420;

    // 不直接绘制在 View 提供的画布上的原因是：选取颜色时需要提取 Bitmap 上的颜色，View 的 Bitmap 无法获取，
    // 而且有指示点时指示点会覆盖主颜色条(重绘颜色条的颜色)
    private Bitmap bitmapForColor;
    private Bitmap bitmapForIndicator;

    /**
     * 是否需要绘制颜色条(指示点)，颜色条在选取颜色时不需要再次生成(bitmapForColor)，直接绘制就行
     */
    private boolean needReDrawColorTable = true;
    private boolean needReDrawIndicator = true;

    /**
     * 手指在颜色条上的坐标
     */
    private int curX, curY;

    private int[] colors = null;

    private int currentColor;

    /**
     * 控件方向
     */
    public enum Orientation {
        /**
         * 水平
         */
        HORIZONTAL, // 0

        /**
         * 竖直
         */
        VERTICAL // 1

    }

    {
        bitmapForColor = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmapForIndicator = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        //Android4.0（API14）之后硬件加速功能就被默认开启了,setShadowLayer 在开启硬件加速的情况下无效，需要关闭硬件加速
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintForIndicator = new Paint();
        paintForIndicator.setAntiAlias(true);

        curX = curY = Integer.MAX_VALUE;
    }

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorPickerView, defStyleAttr, 0);
        mIndicatorColor = array.getColor(R.styleable.ColorPickerView_indicatorColor, Color.WHITE);

        int or = array.getInteger(R.styleable.ColorPickerView_orientation, 0);
        orientation = or == 0 ? Orientation.HORIZONTAL : Orientation.VERTICAL;

        mIndicatorEnable = array.getBoolean(R.styleable.ColorPickerView_indicatorEnable, true);

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

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {//xml中宽度设为warp_content
            width = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();
        }

        width = Math.max(width, orientation == Orientation.HORIZONTAL ? defaultSizeLong : defaultSizeShort);
        height = Math.max(height, orientation == Orientation.HORIZONTAL ? defaultSizeShort : defaultSizeLong);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mTop = getPaddingTop();
        mLeft = getPaddingLeft();
        mBottom = getMeasuredHeight() - getPaddingBottom();
        mRight = getMeasuredWidth() - getPaddingRight();

        if (curX == curY || curY == Integer.MAX_VALUE) {
            curX = getWidth() / 2;
            curY = getHeight() / 2;
        }

        calculBounds();
        if (colors == null) {
            setColors(createDefaultColorTable());
        } else {
            setColors(colors);
        }
        createBitmap();

        if (mIndicatorEnable) {
            needReDrawIndicator = true;
        }

    }

    private void createBitmap() {

        int hc = rect.height();
        int wc = rect.width();
        int hi = mRadius * 2;
        int wi = hi;

        if (bitmapForColor != null) {
            if (!bitmapForColor.isRecycled()) {
                bitmapForColor.recycle();
                bitmapForColor = null;
            }
        }

        if (bitmapForIndicator != null) {
            if (!bitmapForIndicator.isRecycled()) {
                bitmapForIndicator.recycle();
                bitmapForIndicator = null;
            }
        }

        bitmapForColor = Bitmap.createBitmap(wc, hc, Bitmap.Config.ARGB_8888);
        bitmapForIndicator = Bitmap.createBitmap(wi, hi, Bitmap.Config.ARGB_8888);

    }

    /**
     * 计算颜色条边界
     */
    private void calculBounds() {

        /*
         * 将控件可用高度(除去上下 padding )均分为 6 份，以此计算指示点半径，颜色条宽高
         * 控件方向为 HORIZONTAL 时，从上往下依次占的份额为：
         * 1/9 留白
         * 2/9 颜色条上面部分圆
         * 3/9 颜色条宽
         * 2/9 颜色条上面部分圆
         * 1/9 留白
         */
        final int average = 9;

        /*
         * 每一份的高度
         */
        int each;

        int h = mBottom - mTop;
        int w = mRight - mLeft;
        int size = Math.min(w, h);

        if (orientation == Orientation.HORIZONTAL) {
            if (w <= h) { // HORIZONTAL 模式，然而宽却小于高，以 6 ：1 的方式重新计算高
                size = w / 6;
            }
        } else {
            if (w >= h) {
                size = h / 6;
            }
        }

        each = size / average;
        mRadius = each * 7 / 2;

        int t, l, b, r;
        final int s = each * 3 / 2;

        if (orientation == Orientation.HORIZONTAL) {
            l = mLeft + mRadius;
            r = mRight - mRadius;

            t = (getHeight() / 2) - s;
            b = (getHeight() / 2) + s;
        } else {
            t = mTop + mRadius;
            b = mBottom - mRadius;

            l = getWidth() / 2 - s;
            r = getWidth() / 2 + s;
        }

        rect.set(l, t, r, b);
    }

    /**
     * 设置颜色条的渐变颜色，不支持具有 alpha 的颜色，{@link Color#TRANSPARENT}会被当成 {@link Color#BLACK}处理
     * 如果想设置 alpha ，可以在{@link OnColorPickerChangeListener#onColorChanged(ColorPickerView, int)} 回调
     * 中调用{@link android.support.v4.graphics.ColorUtils#setAlphaComponent(int, int)}方法添加 alpha 值。
     *
     * @param colors 颜色值
     */
    public void setColors(int... colors) {
        linearGradient = null;
        this.colors = colors;

        if (orientation == Orientation.HORIZONTAL) {
            linearGradient = new LinearGradient(
                    rect.left, rect.top,
                    rect.right, rect.top,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
            );
        } else {
            linearGradient = new LinearGradient(
                    rect.left, rect.top,
                    rect.left, rect.bottom,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
            );
        }

        needReDrawColorTable = true;
        invalidate();
    }

    public int[] createDefaultColorTable() {

        int[] cs = {
                Color.rgb(255, 0, 0),
                Color.rgb(255, 255, 0),
                Color.rgb(0, 255, 0),
                Color.rgb(0, 255, 255),
                Color.rgb(0, 0, 255),
                Color.rgb(255, 0, 255),
                Color.rgb(255, 0, 0)
        };
        return cs;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (needReDrawColorTable) {
            createColorTableBitmap();
        }
        // 绘制颜色条
        canvas.drawBitmap(bitmapForColor, null, rect, paint);

        if (mIndicatorEnable) {
            if (needReDrawIndicator) {
                createIndicatorBitmap();
            }

            // 绘制指示点
            rectForIndicator.set(curX - mRadius, curY - mRadius, curX + mRadius, curY + mRadius);
            canvas.drawBitmap(bitmapForIndicator, null, rectForIndicator, paint);
        }
    }

    private void createIndicatorBitmap() {

        paintForIndicator.setColor(mIndicatorColor);
        int radius = 3;
        paintForIndicator.setShadowLayer(radius, 0, 0, Color.GRAY);

        Canvas c = new Canvas(bitmapForIndicator);
        c.drawCircle(mRadius, mRadius, mRadius - radius, paintForIndicator);

        needReDrawIndicator = false;
    }

    private void createColorTableBitmap() {

        Canvas c = new Canvas(bitmapForColor);
        RectF rf = new RectF(0, 0, bitmapForColor.getWidth(), bitmapForColor.getHeight());

        // 圆角大小
        int r;
        if (orientation == Orientation.HORIZONTAL) {
            r = bitmapForColor.getHeight() / 2;
        } else {
            r = bitmapForColor.getWidth() / 2;
        }
        // 先绘制黑色背景，否则有 alpha 时绘制不正常
        paint.setColor(Color.BLACK);
        c.drawRoundRect(rf, r, r, paint);

        paint.setShader(linearGradient);
        c.drawRoundRect(rf, r, r, paint);
        paint.setShader(null);

        needReDrawColorTable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int ex = (int) event.getX();
        int ey = (int) event.getY();

        if (!inBoundOfColorTable(ex, ey)) {
            return true;
        }

        if (orientation == Orientation.HORIZONTAL) {
            curX = ex;
            curY = getHeight() / 2;
        } else {
            curX = getWidth() / 2;
            curY = ey;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (colorPickerChangeListener != null) {
                colorPickerChangeListener.onStartTrackingTouch(this);
                calcuColor();
                colorPickerChangeListener.onColorChanged(this, currentColor);
            }

        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) { //手抬起
            if (colorPickerChangeListener != null) {
                colorPickerChangeListener.onStopTrackingTouch(this);
                calcuColor();
                colorPickerChangeListener.onColorChanged(this, currentColor);
            }

        } else { //按着+拖拽
            if (colorPickerChangeListener != null) {
                calcuColor();
                colorPickerChangeListener.onColorChanged(this, currentColor);
            }
        }

        invalidate();
        return true;
    }

    /**
     * 获得当前指示点所指颜色
     *
     * @return 颜色值
     */
    public int getColor() {
        return calcuColor();
    }

    private boolean inBoundOfColorTable(int ex, int ey) {
        if (orientation == Orientation.HORIZONTAL) {
            if (ex <= mLeft + mRadius || ex >= mRight - mRadius) {
                return false;
            }
        } else {
            if (ey <= mTop + mRadius || ey >= mBottom - mRadius) {
                return false;
            }
        }
        return true;
    }

    private int calcuColor() {
        int x, y;
        if (orientation == Orientation.HORIZONTAL) { // 水平
            y = (rect.bottom - rect.top) / 2;
            if (curX < rect.left) {
                x = 1;
            } else if (curX > rect.right) {
                x = bitmapForColor.getWidth() - 1;
            } else {
                x = curX - rect.left;
            }
        } else { // 竖直
            x = (rect.right - rect.left) / 2;
            if (curY < rect.top) {
                y = 1;
            } else if (curY > rect.bottom) {
                y = bitmapForColor.getHeight() - 1;
            } else {
                y = curY - rect.top;
            }
        }
        int pixel = bitmapForColor.getPixel(x, y);
        currentColor = pixelToColor(pixel);
        return currentColor;
    }

    private int pixelToColor(int pixel) {

        int alpha = Color.alpha(pixel);
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);

        return Color.argb(alpha, red, green, blue);
    }

    private OnColorPickerChangeListener colorPickerChangeListener;

    public void setOnColorPickerChangeListener(OnColorPickerChangeListener l) {
        this.colorPickerChangeListener = l;
    }

    public interface OnColorPickerChangeListener {

        /**
         * 选取的颜色值改变时回调
         *
         * @param picker ColorPickerView
         * @param color  颜色
         */
        void onColorChanged(ColorPickerView picker, int color);

        /**
         * 开始颜色选取
         *
         * @param picker ColorPickerView
         */
        void onStartTrackingTouch(ColorPickerView picker);

        /**
         * 停止颜色选取
         *
         * @param picker ColorPickerView
         */
        void onStopTrackingTouch(ColorPickerView picker);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.selX = curX;
        ss.selY = curY;
        ss.color = bitmapForColor;
        if (mIndicatorEnable) {
            ss.indicator = bitmapForIndicator;
        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        curX = ss.selX;
        curY = ss.selY;
        colors = ss.colors;

        bitmapForColor = ss.color;
        if (mIndicatorEnable) {
            bitmapForIndicator = ss.indicator;
            needReDrawIndicator = true;
        }
        needReDrawColorTable = true;

    }

    private class SavedState extends BaseSavedState {
        int selX, selY;
        int[] colors;
        Bitmap color;
        Bitmap indicator = null;

        public SavedState(Parcelable source) {
            super(source);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(selX);
            out.writeInt(selY);
            out.writeParcelable(color, flags);
            out.writeIntArray(colors);
            if (indicator != null) {
                out.writeParcelable(indicator, flags);
            }
        }
    }

    public void setPosition(int x, int y) {
        if (inBoundOfColorTable(x, y)) {
            curX = x;
            curY = y;
            if (mIndicatorEnable) {
                needReDrawIndicator = true;
            }
            invalidate();
        }
    }

    /**
     * 显示默认的颜色选择器
     */
    public void showDefaultColorTable() {
        setColors(createDefaultColorTable());
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public void setIndicatorColor(int color) {
        this.mIndicatorColor = color;
        needReDrawIndicator = true;
        invalidate();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        needReDrawIndicator = true;
        needReDrawColorTable = true;
        requestLayout();
    }
}
