package com.duan.musicoco.view.media;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.duan.musicoco.R;


/**
 * Created by DuanJiaNing on 2017/6/15.
 * 【上一曲】
 * 【下一曲】
 * <p>
 * 控件由如下几部分组成：
 * 1 继承自 MediaView 的圆圈部分
 * 2 内部的单竖线
 * 3 内部的等腰三角形
 * <p>
 * tip: 在 xml 中使用 rotation 属性可实现控件旋转：
 * 如：绕 z 轴旋转 180 度
 * android:rotation="180"
 */

public class SkipView extends MediaView {

    /**
     * 三角形上9个点的坐标
     * 每个顶点三个坐标点，实现圆弧效果
     */
    protected final float[][] coordinate;

    /**
     * 三角形圆弧半径
     */
    protected int triangleRadius;

    /**
     * 竖线圆弧半径
     */
    protected int innerLineRadius;

    /**
     * 单竖线与等腰三角形顶点间的距离
     */
    protected int distance;

    /**
     * 单竖线的宽度
     */
    protected int innerLineWidth;

    /**
     * 三角形是否空心
     * true 为空心，此时可通过 {@link #triangleStroke}指定描边宽度
     */
    protected boolean triangleHollow;

    /**
     * 三角形描边宽度，当{@link #triangleStroke}为 true 时才有效果
     */
    protected int triangleStroke;

    /**
     * 三角形颜色
     */
    protected int triangleColor;

    /**
     * 三角形底边的长度
     */
    protected int triangleHeight;

    /**
     * 三角形顶角到底边的距离
     */
    protected int triangleWidth;

    /**
     * 单竖线高度，该值与三角形的大小时相关的，当只修改该值时，将同时作用于竖线和三角形
     * 如果你只想修改竖线高度，而不修改三角形，应在此之前使用{@link #triangleHeight}固定住三角形的高度
     */
    protected int innerLineHeight;

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
        innerLineRadius = array.getDimensionPixelSize(R.styleable.SkipView_innerLineRadius, 0);
        distance = array.getDimensionPixelSize(R.styleable.SkipView_distance, 0);
        triangleColor = array.getColor(R.styleable.SkipView_triangleColor, defaultColor);
        triangleHollow = array.getBoolean(R.styleable.SkipView_triangleHollow, false);
        triangleStroke = array.getDimensionPixelSize(R.styleable.SkipView_triangleStroke, 2);

        innerLineWidth = array.getDimensionPixelSize(R.styleable.SkipView_innerLineWidth, 2);
        innerLineHeight = array.getDimensionPixelSize(R.styleable.SkipView_innerLineHeight, 951228);

        triangleHeight = array.getDimensionPixelSize(R.styleable.SkipView_triangleHeight, 0);
        triangleWidth = array.getDimensionPixelSize(R.styleable.SkipView_triangleWidth, 0);

        array.recycle();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (innerLineHeight <= 0 || innerLineWidth <= 0) {
            innerLineHeight = 0;
            innerLineWidth = 0;
        }

        if (triangleHeight <= 0)
            triangleHeight = radius * 2 / 3;

        if (innerLineHeight == 951228)
            innerLineHeight = triangleHeight * 3 / 4;

        if (triangleWidth <= 0) {
            //将三角形绘制成等边三角形
            int t2 = triangleHeight * triangleHeight;
            triangleWidth = (int) Math.sqrt(t2 - (t2 / 4));
        }

        //赋值不合理，进行重置（这不是此控件预期的展现形式，应根据整体比例合理赋值）
        //只对上限进行处理
        if (innerLineHeight >= radius * 2)
            innerLineHeight = radius * 2;
        if (innerLineWidth >= radius)
            innerLineWidth = radius;
        if (triangleWidth >= radius)
            triangleWidth = radius;
        if (triangleHeight > radius * 2)
            triangleHeight = radius * 2;

        //以顶点作为参考
        //圆角导致的纵坐标迁移量
        float ry = triangleRadius / 2;
        //圆角导致的横坐标迁移量
        float rx = (float) Math.sqrt(triangleRadius * triangleRadius - ry * ry);

        //计算三角形上 9 个点的坐标
        //拟使【三角形顶点与底边中点相连的线段的中点】在【圆心】上进行计算
        //计算时根据三角形在坐标轴上的位置关系简化计算
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

        //平移三角形使其居中
        //平移之前【三角形顶点与底边中点相连的线段中点】与【圆心】重合，此时内部的【整体图形】没有在圆圈内【居中】
        //平移所有点使【三角形内心】与【圆心】重合
        // ** 计算【等边三角形内心与顶点的距离】
        double a2 = triangleWidth * triangleWidth;
        double c2 = triangleHeight * triangleHeight / 4;
        double c = triangleHeight / 2;
        double a = triangleWidth;
        double x = (Math.pow(Math.sqrt(a2 + c2) - c, 2) + a2) / (2 * a);

        //往前移动【三角形内心到圆心位置】，然后往后移动【竖线宽度】和【顶点到竖线距离】之和的一半
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

    /**
     * 绘制单竖线
     */
    protected void drawLine(Canvas canvas) {

        if (innerLineHeight <= 0 || innerLineWidth <= 0)
            return;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(triangleColor);

        //绘制竖线
        //参考三角形的 顶点 绘制
        float left = coordinate[0][0] - distance - innerLineWidth;
        float top = coordinate[0][1] - innerLineHeight / 2;
        float right = left + innerLineWidth;
        float bottom = top + innerLineHeight;
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectF, innerLineRadius, innerLineRadius, paint);

    }

    /**
     * 绘制三角形
     */
    protected void drawTriangle(Canvas canvas) {

        if (triangleHollow) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(triangleStroke);
        } else
            paint.setStyle(Paint.Style.FILL);

        paint.setColor(triangleColor);

        //绘制三角形
        //顶点上的点及其两侧的点使用贝塞尔曲线连接，实现圆弧效果
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
        this.innerLineRadius = rectRadius;
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

    public void setTriangleHeight(int triangleHeight) {
        this.triangleHeight = triangleHeight;
        invalidate();
    }

    public void setTriangleWidth(int triangleWidth) {
        this.triangleWidth = triangleWidth;
        invalidate();
    }

    public void setInnerLineHeight(int innerLineHeight) {
        this.innerLineHeight = innerLineHeight;
        invalidate();
    }

    public int getTriangleHeight() {
        return triangleHeight;
    }

    public int getTriangleWidth() {
        return triangleWidth;
    }

    public int getInnerLineHeight() {
        return innerLineHeight;
    }

    public int getTriangleRadius() {
        return triangleRadius;
    }

    public int getInnerLineRadius() {
        return innerLineRadius;
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
