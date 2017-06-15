package com.duan.musicoco.view.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/6/15.
 */

public class SkipView extends MediaView {

    //三角形上6个点的坐标
    //顶点处实现圆弧效果
    protected final float[][] coordinate;

    protected int triangleRadius;
    protected int rectRadius;
    protected int distance;
    protected int innerLineWidth;

    protected boolean triangleHollow;
    protected int triangleStroke;
    protected int triangleColor;

    protected int innerLineHeight;
    protected int triangleHeight;
    protected int triangleWidth;

    public SkipView(Context context) {
        super(context);
        coordinate = new float[9][2];

    }

    public SkipView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        coordinate = new float[9][2];

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SkipView, defStyleAttr, 0);

        triangleRadius = array.getDimensionPixelSize(R.styleable.SkipView_triangleRadius, 0);
        rectRadius = array.getDimensionPixelSize(R.styleable.SkipView_rectRadius, 0);
        distance = array.getDimensionPixelSize(R.styleable.SkipView_distance, 0);
        triangleColor = array.getColor(R.styleable.SkipView_triangleColor, strokeColor);
        innerLineWidth = array.getDimensionPixelSize(R.styleable.SkipView_innerLineWidth, strokeWidth * 2);
        triangleHollow = array.getBoolean(R.styleable.SkipView_triangleHollow, false);
        triangleStroke = array.getDimensionPixelSize(R.styleable.SkipView_triangleStroke, 2);
        innerLineHeight = array.getDimensionPixelSize(R.styleable.SkipView_innerLineHeight, 0);
        triangleHeight = array.getDimensionPixelSize(R.styleable.SkipView_triangleHeight, 0);
        triangleWidth = array.getDimensionPixelSize(R.styleable.SkipView_triangleWidth, 0);

        array.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        //竖线高度为半径的 1/3
        if (innerLineHeight <= 0)
            innerLineHeight = radius * 2 / 3;

        if (triangleHeight <= 0)
            triangleHeight = innerLineHeight;

        if (triangleWidth <= 0) {
            int t2 = triangleHeight * triangleHeight;
            triangleWidth = (int) Math.sqrt(t2 - (t2 / 4));
        }

        //以最左边点作为参考
        float ry = triangleRadius / 2;//圆角导致的纵坐标迁移量
        float rx = (float) Math.sqrt(triangleRadius * triangleRadius - ry * ry); //圆角导致的横坐标迁移量

        // 使 triangleWidth / 2 处与圆心重合计算各点坐标
        int halfW = triangleWidth / 2;
        coordinate[0][0] = centerX - halfW;
        coordinate[0][1] = centerY;
        coordinate[1][0] = (centerX - halfW) + rx;
        coordinate[1][1] = centerY - ry;
        coordinate[8][0] = coordinate[1][0];
        coordinate[8][1] = centerY + ry;

        coordinate[3][0] = centerX + halfW;
        coordinate[3][1] = centerY - triangleHeight / 2;
        coordinate[2][0] = (centerX + halfW) - rx;
        coordinate[2][1] = centerY - (triangleHeight / 2 - ry);
        coordinate[7][0] = coordinate[2][0];
        coordinate[7][1] = centerY + (triangleHeight / 2 - ry);

        coordinate[6][0] = coordinate[3][0];
        coordinate[6][1] = centerY + triangleHeight / 2;
        coordinate[4][0] = centerX + halfW;
        coordinate[4][1] = centerY - (triangleHeight / 2 - triangleRadius);
        coordinate[5][0] = coordinate[4][0];
        coordinate[5][1] = centerY + (triangleHeight / 2 - triangleRadius);

        //平移所有点使【三角形内心】与圆心重合
        // ** 计算等边三角形内心与顶角的距离
        double a2 = triangleWidth * triangleWidth;
        double c2 = triangleHeight * triangleHeight / 4;
        double c = triangleHeight / 2;
        double a = triangleWidth;
        double x = (Math.pow(Math.sqrt(a2 + c2) - c, 2) + a2) / (2 * a);

        //往前移动【三角形内心到圆心位置】，【往后移动竖线宽度和顶点到竖线距离之和的一半】
        double tr = (x - halfW) - (distance + innerLineWidth) / 2;
        for (int i = 0; i < coordinate.length; i++) {
            coordinate[i][0] -= tr;
        }

    }

    @Override
    public void drawInside(Canvas canvas) {
        drawLine(canvas);
        drawTriangle(canvas);
    }

    protected void drawLine(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(triangleColor);

        //绘制竖线
        float left = coordinate[0][0] - distance - innerLineWidth;
        float top = coordinate[0][1] - innerLineHeight / 2;
        float right = left + innerLineWidth;
        float bottom = top + innerLineHeight;
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, paint);

    }

    protected void drawTriangle(Canvas canvas) {

        if (triangleHollow) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(triangleStroke);
        } else
            paint.setStyle(Paint.Style.FILL);

        //绘制三角形
        Path path = new Path();
        path.moveTo(coordinate[1][0], coordinate[1][1]);
        path.lineTo(coordinate[2][0], coordinate[2][1]);
        path.cubicTo(coordinate[2][0], coordinate[2][1], coordinate[3][0], coordinate[3][1], coordinate[4][0], coordinate[4][1]);
        path.lineTo(coordinate[5][0], coordinate[5][1]);
        path.cubicTo(coordinate[5][0], coordinate[5][1], coordinate[6][0], coordinate[6][1], coordinate[7][0], coordinate[7][1]);
        path.lineTo(coordinate[8][0], coordinate[8][1]);
        path.cubicTo(coordinate[8][0], coordinate[8][1], coordinate[0][0], coordinate[0][1], coordinate[1][0], coordinate[1][1]);
        path.close();

        canvas.drawPath(path, paint);
    }

    public void setTriangleRadius(int triangleRadius) {
        this.triangleRadius = triangleRadius;
        invalidate();
    }

    public void setRectRadius(int rectRadius) {
        this.rectRadius = rectRadius;
        invalidate();

    }

    public void setDistance(int distance) {
        this.distance = distance;
        invalidate();

    }

    public void setInnerLineWidth(int innerLineWidth) {
        this.innerLineWidth = innerLineWidth;
        invalidate();

    }

    public void setTriangleHollow(boolean triangleHollow) {
        this.triangleHollow = triangleHollow;
        invalidate();

    }

    public void setTriangleStroke(int triangleStroke) {
        this.triangleStroke = triangleStroke;
        invalidate();

    }

    public void setTriangleColor(int triangleColor) {
        this.triangleColor = triangleColor;
        invalidate();

    }

    public void setLineHeight(int lineHeight) {
        this.innerLineHeight = lineHeight;
        invalidate();

    }

    public int getTriangleRadius() {
        return triangleRadius;
    }

    public int getRectRadius() {
        return rectRadius;
    }

    public int getDistance() {
        return distance;
    }

    public int getInnerLineWidth() {
        return innerLineWidth;
    }

    public boolean isTriangleHollow() {
        return triangleHollow;
    }

    public int getTriangleStroke() {
        return triangleStroke;
    }

    public int getTriangleColor() {
        return triangleColor;
    }

    public int getLineHeight() {
        return innerLineHeight;
    }
}
