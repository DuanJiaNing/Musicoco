package com.duan.musicoco.view;

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.duan.musicoco.play.PlayVisualizer;
import com.duan.musicoco.view.bezier.BezierImpl;
import com.duan.musicoco.view.bezier.Gummy;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class PlayVisualizerSurfaceView extends SurfaceView implements SurfaceHolder.Callback ,PlayVisualizer.OnUpdateVisualizer{

    private SurfaceHolder mHolder;

    private SurfaceDrawThread mDrawThread;

    private Gummy gummy;

    {
        //获得持有者
        mHolder = this.getHolder();

        //注册功能
        mHolder.addCallback(this);

        this.setZOrderOnTop(true);
    }

    public PlayVisualizerSurfaceView(Context context) {
        super(context);
    }

    public PlayVisualizerSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayVisualizerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gummy = new Gummy(this, new BezierImpl());
        mDrawThread = new SurfaceDrawThread();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void startTurning(){
        mDrawThread.start();
    }

    public void stopTurning(){
        try {
            mDrawThread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateVisualizer(byte[] data) {

    }

    private class SurfaceDrawThread extends Thread {

        public SurfaceDrawThread() {
        }

        @Override
        public void run() {
            Looper.prepare();
            Looper.loop();
        }


    }

}
