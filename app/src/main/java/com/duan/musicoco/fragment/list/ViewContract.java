package com.duan.musicoco.fragment.list;

import android.view.animation.Animation;

import com.duan.musicoco.BaseView;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<PresenterContract> {

    void showFragment(Animation.AnimationListener f);

    void hideFragment(Animation.AnimationListener f);

}
