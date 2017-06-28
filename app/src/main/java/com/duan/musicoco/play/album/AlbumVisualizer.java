package com.duan.musicoco.play.album;

import android.media.audiofx.Visualizer;

/**
 * Created by DuanJiaNing on 2017/5/30.
 * 当不在需要获取频谱时应调用 stopListen 及时停止监听
 */

public class AlbumVisualizer {

    private Visualizer mVisualizer;

    int rate;
    int size;

    public interface OnUpdateVisualizerListener {
        /**
         * IAlbum 采用数据回调
         *
         * @param data 数据
         * @param rate 采样速率
         */
        void updateVisualizer(byte[] data, int rate);
    }

    private OnUpdateVisualizerListener mUpdateVisualizer;

    public void setUpdateVisualizerListener(OnUpdateVisualizerListener listener) {
        this.mUpdateVisualizer = listener;
    }

    public void startListen(int sessionId, int rate,int size) throws Exception {

        stopListen();

        this.rate = rate;
        this.size = size;

        //采样周期
        mVisualizer = new Visualizer(sessionId);
        //采样时长
        mVisualizer.setCaptureSize(size);
        //获取波形信号
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                if (mUpdateVisualizer != null)
                    mUpdateVisualizer.updateVisualizer(waveform, samplingRate);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                if (mUpdateVisualizer != null)
                    mUpdateVisualizer.updateVisualizer(fft, samplingRate);
            }
        }, rate, false, true);

        mVisualizer.setEnabled(true);
    }

    public void stopListen() {

        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }


}



