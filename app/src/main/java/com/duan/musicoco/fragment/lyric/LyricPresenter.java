package com.duan.musicoco.fragment.lyric;

import android.content.Context;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class LyricPresenter implements PresenterContract {

    private ViewContract view;

    private Context context;

    public LyricPresenter(Context context,ViewContract contract) {
        this.context = context;
        this.view = contract;
        contract.setPresenter(this);
    }

    @Override
    public void initData() {

    }
}
