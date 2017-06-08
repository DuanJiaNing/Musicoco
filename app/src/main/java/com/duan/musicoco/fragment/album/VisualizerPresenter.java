package com.duan.musicoco.fragment.album;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.RemoteException;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.service.PlayController;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private Context context;

    private IPlayControl control;

    private AlbumVisualizer mVisualizer;

    public VisualizerPresenter(Context context, IPlayControl control, ViewContract fragment) {
        this.context = context;
        this.fragmentView = fragment;
        this.control = control;

        fragmentView.setPresenter(this);

        mVisualizer = new AlbumVisualizer();
        mVisualizer.setUpdateVisualizerListener(fragmentView.getVisualizerListener());
    }

    @Override
    public void initData(Object obj) {
        try {

            if (control.status() == PlayController.STATUS_PLAYING) {
                startPlay();
                turnOnVisualizer();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startPlay() {
        fragmentView.startSpin();
        //FIXME Visualizer 导致 ANR
        turnOnVisualizer();
    }

    @Override
    public void stopPlay() {
        fragmentView.stopSpin();
        turnOffVisualizer();
    }

    @Override
    public void songChanged(Song song) {
        fragmentView.songChanged(song);
    }

    @Override
    public void turnOnVisualizer() {
        try {

            final int sessionid = control.getAudioSessionId();

            //采样周期 即隔多久采样一次
            final int rate = Visualizer.getMaxCaptureRate() / 2;
            final int size = 128;

            mVisualizer.startListen(sessionid, rate,size);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void turnOffVisualizer() {
        mVisualizer.stopListen();
    }

}
