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
    void songChanged(Song song);

    //打开 频谱监听器
    void turnOnVisualizer();

    //关闭 频谱监听器
    void turnOffVisualizer();

}
