package com.duan.musicoco.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/7/10.
 */

public class ImageTextView extends View {

    private String mText;
    private Bitmap mBitmap;
    private float mTextSize;
    private Paint paint = new Paint();

    public ImageTextView(Context context) {
        super(context);
    }

    public ImageTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ImageTextView, defStyleAttr, 0);
        mText = "";
        mText = array.getString(R.styleable.ImageTextView_text);
        mTextSize = array.getDimension(R.styleable.ImageTextView_textSize, 10.0f);
        int id = array.getResourceId(R.styleable.ImageTextView_bitmap, R.drawable.default_album);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), id);
        array.recycle();

        if (mBitmap != null) {
            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album);
            shader = new BitmapShader(b, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;

        int wp = getPaddingLeft() + getPaddingRight();
        int hp = getPaddingTop() + getPaddingBottom();

        paint.setTextSize(mTextSize);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {//xml中宽度设为warp_content
            width = (int) (paint.measureText(mText) + wp);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = (int) (mTextSize + hp);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            width = Math.max(width, getMinimumWidth());
            height = Math.max(height, getMinimumHeight());
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    private Shader shader;

    @Override
    protected void onDraw(Canvas canvas) {

        if (mBitmap == null || mText.isEmpty() || shader == null) {
            return;
        }
        paint.setColor(Color.RED);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setShader(shader);
        paint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
        canvas.drawText(mText, 0, getHeight(), paint);

    }

    public void setText(String text) {
        this.mText = text;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        invalidate();
    }

    public void setTextSize(float size) {
        this.mTextSize = size;
        invalidate();
    }
}
