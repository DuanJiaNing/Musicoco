package com.duan.musicoco.play.album;

import android.media.audiofx.Visualizer;

import com.duan.musicoco.util.ArrayUtils;

import java.util.Arrays;

import static android.media.audiofx.Visualizer.getMaxCaptureRate;

/**
 * Created by DuanJiaNing on 2017/5/30.
 * 当不在需要获取频谱时应调用 stopListen 及时停止监听
 */

public class PlayVisualizer {

    private Visualizer mVisualizer;

    public void stopListen() {

        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    public interface OnFftDataCaptureListener {
        void onFftCapture(float[] fft);
    }

    /**
     * 设置频谱回调
     *
     * @param size 传回的数组大小，最大为 128
     * @param max  整体频率的大小，该值越小，传回数组的平均值越大，在 50 时效果较好。
     * @param l    回调
     */
    public void setupVisualizer(final int size, int sessionId, final int max, final OnFftDataCaptureListener l) {

        // 频率分之一是时间  赫兹=1/秒
        mVisualizer = new Visualizer(sessionId);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]); //0为128；1为1024
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                //快速傅里叶变换有关的数据

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                //波形数据

                byte[] model = new byte[fft.length / 2 + 1];
                model[0] = (byte) Math.abs(fft[1]);
                int j = 1;

                for (int i = 2; i < size * 2; ) {

                    model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                    i += 2;
                    j++;
                }

                float[] data = new float[size];
                if (max != 0) {
                    for (int i = 0; i < size; i++) {
                        data[i] = (float) model[i] / max;
                        data[i] = data[i] < 0 ? 0 : data[i];
                    }
                } else {
                    Arrays.fill(data, 0);
                }

                l.onFftCapture(data);

            } // getMaxCaptureRate() -> 20000 最快
        }, getMaxCaptureRate() / 8, false, true);

        mVisualizer.setEnabled(false); //这个设置必须在参数设置之后，表示开始采样
    }

    /**
     * 设置频谱回调
     *
     * @param size 传回的数组大小，最大为 128
     * @param l    回调
     */
    public void setupVisualizer(final int size, int sessionId, final OnFftDataCaptureListener l) {

        // 频率分之一是时间  赫兹=1/秒
        mVisualizer = new Visualizer(sessionId);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]); //0为128；1为1024
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                //快速傅里叶变换有关的数据

                float[] data = new float[size];
                int s = 0;
                for (int i = 0; i < waveform.length; i++) {
                    if (s == size) break;
                    if (waveform[i] > 0) data[s++] = waveform[i];
                }
                float max = ArrayUtils.max(data);
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] / max;
                }

                float min = ArrayUtils.min(data);
                for (int i = 0; i < data.length; i++) {
                    float v = data[i] - min;
                    data[i] = v < 0 ? 0 : v;
                }

                /*float ave = ArrayUtils.average(data);
                for (int i = 0; i < data.length; i++) {
                    float d = data[i];
                    float sd = ave / 3;
                    if (d > ave) data[i] += sd;
                    else data[i] -= sd;
                }*/

                boolean valid = true;
                for (float d : data) {
                    if (Float.isNaN(d)) valid = false;
                }

                if (valid) l.onFftCapture(data);

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//                Log.e("TAG", "onFftDataCapture: " + fft.length + " " + Arrays.toString(fft));
                //波形数据

                float[] data = new float[size];
                for (int i = 0; i < size; i++) {
                    float da = fft[i];
                    data[i] = Math.abs(((da > 0 ? da : -da) - 1) / 100);
                }
//                Log.e("TAG", "onFftDataCapture-after: " + data.length + " " + Arrays.toString(data));

//                l.onFftCapture(data);

            } // getMaxCaptureRate() -> 20000 最快
        }, getMaxCaptureRate() / 8, true, true);

        mVisualizer.setEnabled(false); //这个设置必须在参数设置之后，表示开始采样
    }

    public void setVisualizerEnable(boolean visualizerEnable) {
//        if (mPlayer.isPlaying()) {
        mVisualizer.setEnabled(visualizerEnable);
//        }
    }

}



