package com.duan.musicoco.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
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
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class AlbumSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AlbumAlbumSurfaceView";
    private final String DEFAULT_PIC = "defalut_album_pic";

    private int mPicWidth;
    private int mStrokeWidth = 13;
    private int mStroke2Width = 10;
    private int colorGray;

    private int centerX;
    private int centerY;

    private SurfaceHolder mHolder;

    private Context context;

    private DrawThread mDrawThread;

    private int rotateAngle;
    private ValueAnimator rotateAnim;

    private SongInfo mCurrentSong;
    private Bitmap mCurrentPic;

    private int defaultColor;
    private int[] colors;

    private BitmapCache cache;

    private final int START_SPIN = 1;

    private final int STOP_SPIN = 2;

    private final int INVALIDATE = 4;

    //调用构造函数之后，应及时调用 createSurface 创建 Surface。
    public AlbumSurfaceView(Context context) {
        super(context);
        this.context = context;

        defaultColor = context.getResources().getColor(R.color.colorPrimary);
        colors = new int[]{
                defaultColor, // 亮的活力颜色
                defaultColor, // 亮的柔和颜色
        };

        colorGray = Color.parseColor("#69e1e1e1");

//        cache = new BitmapCache(context);

//        Bitmap defaultPic = BitmapUtils.bitmapResizeFromResource(context.getResources(), R.mipmap.default_album_pic, mPicWidth, mPicWidth);
//        Bitmap br = BitmapUtils.jpgToPng(defaultPic, context);
//        mCurrentPic = BitmapUtils.getCircleBitmap(br);
//        cache.add(StringUtils.stringToMd5(DEFAULT_PIC), mCurrentPic);

    }

    public AlbumSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        setZOrderOnTop(true);//使surfaceview放到最顶层
        mHolder.setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mPicWidth = (Math.min(getHeight(), getWidth()) * 4) / 5;
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

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

            String key = StringUtils.stringToMd5(song.getAlbum_path());

            Bitmap result = cache.get(key);

            if (result == null) { //处理
                Bitmap b = BitmapUtils.bitmapResizeFromFile(mCurrentSong.getAlbum_path(), mPicWidth, mPicWidth);
                Bitmap bm = BitmapUtils.jpgToPng(context, b);
                result = BitmapUtils.getCircleBitmap(bm);
            } else {
                mCurrentPic = result;
                return;
            }

            if (result != null) {
                cache.add(key, result);
                mCurrentPic = result;
            }
        } else {
            mCurrentPic = cache.get(StringUtils.stringToMd5(DEFAULT_PIC));
        }
    }

    //确保在 surfaceCreated 和 surfaceDestroyed 之间调用
    public void setSong(SongInfo song) {
        if (song != null)
            mCurrentSong = song;

        updateAlbumPic(song);

        ColorUtils.get2ColorFormBitmap(mCurrentPic, defaultColor, colors);

        mDrawThread.getHandler().sendEmptyMessage(INVALIDATE);

    }

    private class DrawHandler extends Handler {

        public DrawHandler(Looper looper) {
            super(looper);
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

                    break;
                case STOP_SPIN:
                    if (rotateAnim.isRunning())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            rotateAnim.pause();
                        } else
                            rotateAnim.cancel();

                    break;
                case INVALIDATE:
                    mDrawThread.repaint();
                    break;
            }
        }
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

            rotateAnim = ObjectAnimator.ofInt(0, 360);
            rotateAnim.setInterpolator(new LinearInterpolator());
            rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnim.setRepeatMode(ValueAnimator.RESTART);
            rotateAnim.setDuration(40000);
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rotateAngle = (int) animation.getAnimatedValue();
                    repaint();
                }
            });

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

            mCanvas.save();
            mCanvas.rotate(rotateAngle, centerX, centerY);

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            //绘制专辑图片
            drawAlbumPic();

            //绘制专辑图片四周的描边
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            int color = defaultColor;
            for (int c : colors) {
                if (c != defaultColor) {
                    color = c;
                    break;
                }
            }
            mPaint.setColor(color);
            mCanvas.drawCircle(centerX, centerY, mPicWidth / 2, mPaint);

            //绘制描边外围的灰边
            mPaint.setStrokeWidth(mStroke2Width);
            mPaint.setColor(colorGray);
            mCanvas.drawCircle(centerX, centerY, mPicWidth / 2 + mStrokeWidth, mPaint);

            mCanvas.restore();
            mHolder.unlockCanvasAndPost(mCanvas);
        }

        private void drawAlbumPic() {
            int left = centerX - mPicWidth / 2;
            int top = centerY - mPicWidth / 2;
            int right = left + mPicWidth;
            int bottom = top + mPicWidth;
            Rect des = new Rect(left, top, right, bottom);
            mPaint.setColor(Color.WHITE);
            mCanvas.drawBitmap(mCurrentPic, null, des, mPaint);
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
