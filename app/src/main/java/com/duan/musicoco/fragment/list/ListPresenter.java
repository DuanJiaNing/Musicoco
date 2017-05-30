package com.duan.musicoco.fragment.list;

import android.content.Context;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class ListPresenter implements PresenterContract {

    private ViewContract view;

    private Context context;

    public ListPresenter(Context context,ViewContract contract) {
        this.context = context;
        this.view = contract;
        contract.setPresenter(this);
    }

    @Override
    public void initData() {

    }
}
