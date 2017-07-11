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
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
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
    private TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private Shader shader;
    private Typeface typeface;

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

        paint.setAntiAlias(true);
        if (mBitmap != null) {
            shader = new BitmapShader(mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }
        typeface = Typeface.SERIF;

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
        Rect rect = new Rect();
        paint.getTextBounds(mText, 0, mText.length(), rect);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize + wp;
        } else {//xml中宽度设为warp_content
            width = rect.width() + wp;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize + hp;
        } else {
            height = rect.height() + hp;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            width = Math.max(width, getMinimumWidth());
            height = Math.max(height, getMinimumHeight());
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mBitmap == null || mText.isEmpty() || shader == null) {
            return;
        }

        paint.setTextSize(mTextSize);
        paint.setColor(Color.RED);
        paint.setTypeface(typeface);
        paint.setShader(shader);
//        paint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.SOLID));

        Rect targetRect = new Rect(0, 0, getWidth(), getHeight());
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mText, targetRect.centerX(), baseline, paint);
    }

    public void setText(String text) {
        if (text == null) {
            return;
        }

        this.mText = text;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        this.mBitmap = bitmap;
        shader = new BitmapShader(mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        invalidate();
    }

    public void setTextSize(float size) {
        this.mTextSize = size;
        invalidate();
    }

    public void setTypeface(Typeface typeface) {
        if (typeface == null) {
            return;
        }
        this.typeface = typeface;
        invalidate();
    }
}
