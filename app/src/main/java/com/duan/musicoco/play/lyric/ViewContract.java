package com.duan.musicoco.play.lyric;

import com.duan.musicoco.BaseView;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<PresenterContract> {

    void showFragment();

    void hideFragment();

}
