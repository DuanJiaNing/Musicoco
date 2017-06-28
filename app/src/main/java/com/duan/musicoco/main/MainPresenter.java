package com.duan.musicoco.main;

import android.content.Context;

import com.duan.musicoco.service.PlayServiceCallback;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class MainPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private PlayServiceCallback activityView;
    private Context context;

    public MainPresenter(Context context, ViewContract fragment, PlayServiceCallback activity) {
        this.context = context;
        this.fragmentView = fragment;
        this.activityView = activity;

        fragmentView.setPresenter(this);
    }

    @Override
    public void initData(Object obj) {

    }
}
