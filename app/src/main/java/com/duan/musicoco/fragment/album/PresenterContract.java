package com.duan.musicoco.fragment.album;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface PresenterContract extends BasePresenter{

    void startPlay();

    void stopPlay();

    void changeSong(Song song);
}
