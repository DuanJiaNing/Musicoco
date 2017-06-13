package com.duan.musicoco.list;

import android.content.Context;

import com.duan.musicoco.play.ActivityViewContract;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class ListPresenter implements PresenterContract {

    private ViewContract fragmentView;

    private ActivityViewContract activityView;
    private Context context;

    public ListPresenter(Context context, ViewContract fragment, ActivityViewContract activity) {
        this.context = context;
        this.fragmentView = fragment;
        this.activityView = activity;

        fragmentView.setPresenter(this);
    }

    @Override
    public void initData(Object obj) {

    }
}
