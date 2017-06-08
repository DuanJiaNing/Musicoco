package com.duan.musicoco.fragment.album;

import android.content.Context;

import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private Context context;

    public VisualizerPresenter(Context context,ViewContract fragment) {
        this.context = context;
        this.fragmentView = fragment;

        fragmentView.setPresenter(this);
    }

    @Override
    public void initData(Object obj) {
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
    public void songChanged(Song song) {
        fragmentView.songChanged(song);
    }
}
