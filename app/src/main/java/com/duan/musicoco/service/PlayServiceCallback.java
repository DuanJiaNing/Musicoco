package com.duan.musicoco.service;

import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface PlayServiceCallback {

    /**
     * 当当前歌曲改变时回调
     * 此方法由服务端控制调用
     */
    void songChanged(Song song, int index, boolean isNext);

    /**
     * 此方法由服务端控制调用
     */
    void startPlay(Song song, int index, int status);

    /**
     * 此方法由服务端控制调用
     */
    void stopPlay(Song song, int index, int status);

}
