package com.duan.musicoco.play.album;

import android.content.Context;
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

    public VisualizerPresenter(Context context, IPlayControl control, ViewContract fragment) {
        this.context = context;
        this.fragmentView = fragment;
        this.control = control;

        fragmentView.setPresenter(this);

    }

    @Override
    public void initData(Object obj) {
        try {

            if (control.status() == PlayController.STATUS_PLAYING) {
                startPlay();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startPlay() {
        fragmentView.startSpin();
    }

    @Override
    public void stopPlay() {
        fragmentView.stopSpin();
    }

    @Override
    public void songChanged(Song song,int dir,boolean updateColors) {
        fragmentView.songChanged(song,dir,updateColors);
    }

}
