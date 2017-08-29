package com.duan.musicoco.app.interfaces;

/**
 * Created by DuanJiaNing on 2017/7/7.
 * 内容更新
 */

public interface ContentUpdatable {
    /**
     * 进行数据更新，该方法中应对数据进行判空处理，当没有数据时应实现并调用{@link #noData()}
     * 进行处理。
     */
    void update(Object obj, OnUpdateStatusChanged statusChanged);

    /**
     * 没有数据
     */
    void noData();
}
