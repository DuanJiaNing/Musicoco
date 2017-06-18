package com.duan.musicoco.fragment.album;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface PresenterContract extends BasePresenter {

    //歌曲开始播放
    void startPlay();

    //歌曲停止播放
    void stopPlay();

    //歌曲切换
    //dir 为 0 为上一曲，为 1 为下一曲
    void songChanged(Song song,int dir);

}
