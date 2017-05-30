package com.duan.musicoco.fragment.album;

import android.content.Context;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerPresenter implements PresenterContract {

    private ViewContract view;

    private Context context;


    public VisualizerPresenter(Context context,ViewContract contract) {
        this.context = context;
        this.view = contract;
        contract.setPresenter(this);
    }

    @Override
    public void initData() {

    }
}
