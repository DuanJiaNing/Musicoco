package com.duan.musicoco.fragment.album;

import android.media.audiofx.Visualizer;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

//TODO
public class AlbumVisualizer {

    private Visualizer mVisualizer;

    public interface OnUpdateVisualizerListener {
        void updateVisualizer(byte[] data,int rate);
    }

    private OnUpdateVisualizerListener mUpdateVisualizer;

    public AlbumVisualizer() {
    }

    public void setUpdateVisualizerListener(OnUpdateVisualizerListener listener) {
        this.mUpdateVisualizer = listener;
    }

    public void setSessionID(int id, int rate,int size) {
        if (mVisualizer != null)
            mVisualizer = null;

        //采样周期
        mVisualizer = new Visualizer(id);
        //采样时长
        mVisualizer.setCaptureSize(size);
        //获取波形信号
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                if (mUpdateVisualizer != null)
                    mUpdateVisualizer.updateVisualizer(waveform,samplingRate);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        }, rate, true, false);
        mVisualizer.setEnabled(true);
    }


}



