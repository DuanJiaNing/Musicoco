package com.duan.musicoco.fragment.lyric;

import android.content.Context;

import com.duan.musicoco.play.ActivityViewContract;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class LyricPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private ActivityViewContract activityView;
    private Context context;

    public LyricPresenter(Context context,ViewContract fragment,ActivityViewContract activity) {
        this.context = context;
        this.fragmentView = fragment;
        this.activityView = activity;

        fragmentView.setPresenter(this);
    }

    @Override
    public void initData(Object obj) {

    }
}
