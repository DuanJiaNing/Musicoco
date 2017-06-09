package com.duan.musicoco.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;

import com.duan.musicoco.R;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.fragment.album.AlbumVisualizer;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.util.BitmapUtil;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtil;
import com.duan.musicoco.view.bezier.BezierImpl;
import com.duan.musicoco.view.bezier.Gummy;

import java.io.IOException;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class AlbumVisualizerSurfaceView extends SurfaceView implements SurfaceHolder.Callback,
        AlbumVisualizer.OnUpdateVisualizerListener {

    private static final String TAG = "AlbumVisualizerSurfaceView";
    private final String DEFAULT_PIC = "defalut_album_pic";

    private int mPicWidth;
    private int mPicStrokeWidth = 10;
    private int mGapWidth = 80;

    private SurfaceHolder mHolder;

    private Context context;

    private DrawThread mDrawThread;

    private Gummy gummy;

    private ValueAnimator rotateAnim;
    private ValueAnimator colorAnim;

    private float[] lengths;

    private SongInfo mCurrentSong;
    private Bitmap mCurrentPic;

    private int defaultColor;
    private int[] colors;

    private BitmapCache cache;

    private final int START_SPIN = 1;

    private final int STOP_SPIN = 2;

    private final int VISUALIZER_UPDATE = 3;

    private final int INVALIDATE = 4;

    private int lot = 40;

    //调用构造函数之后，应及时调用 createSurface 创建 Surface。
    public AlbumVisualizerSurfaceView(Context context) {
        super(context);
        this.context = context;

        defaultColor = context.getResources().getColor(R.color.colorPrimary);
        colors = new int[]{
                defaultColor,
                defaultColor,
                defaultColor,
                defaultColor,
                defaultColor,
                defaultColor
        };

        cache = new BitmapCache(context);

        Bitmap defaultPic = BitmapUtil.bitmapResizeFromResource(context.getResources(), R.mipmap.default_album_pic, mPicWidth, mPicWidth);
        Bitmap br = BitmapUtil.jpgTopng(defaultPic, context);
        mCurrentPic = BitmapUtil.getCircleBitmap(br);
        cache.add(StringUtil.stringToMd5(DEFAULT_PIC), mCurrentPic);

    }

    public AlbumVisualizerSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumVisualizerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    //当 Surface 被 destory 之后或在第一次创建时，调用该方法创建 Surface
    public void createSurface(SongInfo info) {
        this.mCurrentSong = info;

        //获得持有者
        mHolder = this.getHolder();
        //注册功能
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mPicWidth = (getWidth() * 3) / 5 - mGapWidth;

        gummy = new Gummy(this, new BezierImpl());
        gummy.setAutoInvalidateWhenAnim(false);
        float radius = mPicWidth / 2 + mGapWidth;
        gummy.setLot(lot);
        gummy.setCenterX(getWidth() / 2);
        gummy.setCenterY(getHeight() / 2);
        gummy.setRadius(radius);
        gummy.setInnerLineLengthForAll(radius);

        mDrawThread = new DrawThread();
        updateAlbumPic(mCurrentSong);
        mDrawThread.start();

        setSong(mCurrentSong);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThread.quit();
    }

    //确保在 surfaceCreated 和 surfaceDestroyed 之间调用
    public void startSpin() {
        mDrawThread.getHandler().sendEmptyMessage(START_SPIN);
    }

    //确保在 surfaceCreated 和 surfaceDestroyed 之间调用
    public void stopSpin() {
        mDrawThread.getHandler().sendEmptyMessage(STOP_SPIN);
    }

    //缓存中有专辑图片则加载，没有则使用默认，同时压缩图片尺寸
    private void updateAlbumPic(SongInfo song) {

        if (song != null && song.getAlbum_path() != null) {

            String key = StringUtil.stringToMd5(song.getAlbum_path());

            Bitmap result = cache.get(key);

            if (result == null) { //处理
                Bitmap b = BitmapUtil.bitmapResizeFromFile(mCurrentSong.getAlbum_path(), mPicWidth, mPicWidth);
                Bitmap bm = BitmapUtil.jpgTopng(b, context);
                result = BitmapUtil.getCircleBitmap(bm);
            } else {
                mCurrentPic = result;
                return;
            }

            if (result != null) {
                cache.add(key, result);
                mCurrentPic = result;
            }
        } else {
            mCurrentPic = cache.get(StringUtil.stringToMd5(DEFAULT_PIC));
        }
    }

    //确保在 surfaceCreated 和 surfaceDestroyed 之间调用
    public void setSong(SongInfo song) {
        if (song != null)
            mCurrentSong = song;

        updateAlbumPic(song);

        ColorUtils.getColorFormBitmap(mCurrentPic, defaultColor, colors);

        gummy.setColor(colors[0]);

        if (colorAnim != null)
            colorAnim = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnim = ObjectAnimator.ofArgb(gummy, "color", colors);
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.setDuration(60 * 1000 * 4); //4 分钟
            colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDrawThread.getHandler().sendEmptyMessage(INVALIDATE);
                }
            });
        }

        mDrawThread.getHandler().sendEmptyMessage(INVALIDATE);

    }


    private class DrawHandler extends Handler {

        public DrawHandler(Looper looper) {
            super(looper);
            rotateAnim = ObjectAnimator.ofFloat(gummy, "angleOffStart", 0, (float) Math.PI * 2);
            rotateAnim.setInterpolator(new LinearInterpolator());
            rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnim.setRepeatMode(ValueAnimator.RESTART);
            rotateAnim.setDuration(40000);
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDrawThread.getHandler().sendEmptyMessage(INVALIDATE);
                }
            });

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SPIN:
                    if (rotateAnim.isStarted()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            rotateAnim.resume();
                        }
                    } else rotateAnim.start();

                    if (colorAnim != null) {
                        if (colorAnim.isStarted()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                colorAnim.resume();
                            }
                        } else colorAnim.start();
                    }
                    break;
                case STOP_SPIN:
                    if (rotateAnim.isRunning())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            rotateAnim.pause();
                        } else
                            rotateAnim.cancel();

                    if (colorAnim != null) {
                        if (colorAnim.isRunning()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                colorAnim.pause();
                            }
                        } else colorAnim.cancel();
                    }

                    break;
                case VISUALIZER_UPDATE:
                    gummy.setOutLineLength(0, lengths);
                    mDrawThread.repaint();
                    break;
                case INVALIDATE:
                    mDrawThread.repaint();
                    break;
            }
        }
    }

    //确保在 surfaceCreated 和 surfaceDestroyed 之间调用
    @Override
    public void updateVisualizer(byte[] data, int rate) {

        lengths = new float[lot / 2];
        //放大频谱
        int enlarge = 4;

        lengths[0] = 0;
        for (int i = 2, j = 1; j < lengths.length; ) {
            float a = data[i];
            float b = data[i + 1];
            //强转会直接为 0 ？？？？ (> x <)
            double d = Math.sqrt(a * a + b * b);
            lengths[j] = (float) d * enlarge;
            i += 2;
            j++;

        }

        mDrawThread.getHandler().sendEmptyMessage(VISUALIZER_UPDATE);

    }

    private class DrawThread extends Thread {

        private DrawHandler handler;

        private Canvas mCanvas;

        private Paint mPaint;

        private Looper mLooper;

        public DrawThread() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }

        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            handler = new DrawHandler(Looper.myLooper());
            repaint();
            Looper.loop();
        }

        public void repaint() {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas == null)
                return;

            mCanvas.drawColor(Color.WHITE);

            //计算出所有的控制点
            float[][] points = gummy.calcuCoordinates();

            //计算出贝塞尔曲线上的点并绘制
            gummy.setColor(gummy.getColor());
            mPaint.setStyle(Paint.Style.FILL);
            float[][] pos = gummy.calcuBeziers(points, 200);
            gummy.drawBeziers(mCanvas, mPaint, pos);

            //绘制专辑图片
            drawAlbumPic();

            //绘制专辑图片四周的描边
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mPicStrokeWidth);
            mPaint.setColor(colors[1]);
            mCanvas.drawCircle(gummy.getCenterX(), gummy.getCenterY(), mPicWidth / 2, mPaint);

//            //绘制连接控制点的线
//            float x0, y0, x, y;
//            x0 = points[0][0];
//            y0 = points[0][1];
//            mPaint.setColor(Color.RED);
//            mPaint.setStrokeWidth(5.0f);
//            for (int i = 1; i < points.length; i++) {
//                x = points[i][0];
//                y = points[i][1];
//                mCanvas.drawLine(x0, y0, x, y, mPaint);
//                x0 = x;
//                y0 = y;
//            }
//
//            //绘制圆
//            mPaint.setColor(Color.BLACK);
//            mPaint.setAlpha(50); //要在 setColor 后调用，否则无效
//            mCanvas.drawCircle(gummy.getCenterX(), gummy.getCenterY(), gummy.getRadius(), mPaint);
//
//            //绘制过圆心的两条线
//            mPaint.setColor(Color.GRAY);
//            mPaint.setAlpha(100);
//            mCanvas.drawLine(gummy.getCenterX() - gummy.getRadius(), gummy.getCenterY(),
//                    gummy.getCenterX() + gummy.getRadius(), gummy.getCenterY(), mPaint);
//            mCanvas.drawLine(gummy.getCenterX(), gummy.getCenterY() - gummy.getRadius(),
//                    gummy.getCenterX(), gummy.getCenterY() + gummy.getRadius(), mPaint);

            mHolder.unlockCanvasAndPost(mCanvas);
        }

        private void drawAlbumPic() {
            int left = (int) (gummy.getCenterX() - mPicWidth / 2);
            int top = (int) (gummy.getCenterY() - mPicWidth / 2);
            int right = left + mPicWidth;
            int bottom = top + mPicWidth;
            Rect des = new Rect(left, top, right, bottom);
            mCanvas.save();
            mCanvas.rotate((float) Math.toDegrees(gummy.getAngleOffStart()), gummy.getCenterX(), gummy.getCenterY());
            mCanvas.drawBitmap(mCurrentPic, null, des, mPaint);
            mCanvas.restore();

        }

        private DrawHandler getHandler() {
            return handler;
        }

        private void quit() {
            if (rotateAnim.isRunning())
                rotateAnim.cancel();
            mLooper.quit();
        }

    }


}
