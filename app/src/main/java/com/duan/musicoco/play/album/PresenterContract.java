package com.duan.musicoco.play.album;

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
    //是否更新专辑对应的颜色
    void songChanged(Song song, boolean isNext, boolean updateColors);

}
