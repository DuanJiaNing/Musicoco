package com.duan.musicoco.play;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.BaseView;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ActivityViewContract extends BaseView<BasePresenter> {

    /**
     * 当当前歌曲改变时回调
     * 此方法由服务端控制调用
     */
    void songChanged(Song song, int index);

    /**
     * 此方法由服务端控制调用
     */
    void startPlay(Song song, int index, int status);

    /**
     * 此方法由服务端控制调用
     */
    void stopPlay(Song song, int index, int status);

    /**
     * 连接到服务端后调用
     */
    void onConnected();

    /**
     * 连接断开时调用
     */
    void disConnected();

    /**
     * 显示歌词
     */
    void showLyricFragment();

    /**
     * 隐藏歌词面板
     */
    void hideLyricFragment();

    /**
     * 存储中没有歌曲
     */
    void noSongsInDisk();

    /**
     * 显示播放列表面板
     */
    void showPlayList();

    /**
     * 隐藏播放列表面板
     */
    void hidePlayList();

}
