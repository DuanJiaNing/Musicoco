package com.duan.musicoco.play.lyric;

import android.content.Context;

import com.duan.musicoco.service.PlayServiceCallback;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */
// TODO
public class LyricPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private PlayServiceCallback activityView;
    private Context context;

    public LyricPresenter(Context context,ViewContract fragment,PlayServiceCallback activity) {
        this.context = context;
        this.fragmentView = fragment;
        this.activityView = activity;

        fragmentView.setPresenter(this);
    }

    @Override
    public void initData(Object obj) {

    }
}
