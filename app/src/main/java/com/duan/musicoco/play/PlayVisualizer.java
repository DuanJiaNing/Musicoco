package com.duan.musicoco.play;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

//TODO
public class PlayVisualizer {

    private Visualizer mVisualizer;

    public interface OnUpdateVisualizer {
        void updateVisualizer(byte[] data);
    }

    private OnUpdateVisualizer mUpdateVisualizer;

    public PlayVisualizer(OnUpdateVisualizer uv) {
        this.mUpdateVisualizer = uv;
    }

    public void setSessionID(int id, int rate) {
        if (mVisualizer != null)
            mVisualizer = null;

        mVisualizer = new Visualizer(id);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                mUpdateVisualizer.updateVisualizer(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        }, rate, true, false);
        mVisualizer.setEnabled(true);

    }

}



