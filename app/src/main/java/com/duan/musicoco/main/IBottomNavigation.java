package com.duan.musicoco.main;

import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public interface IBottomNavigation {

    /**
     * 切换歌曲
     */
    void changeSong(Song song, int index);

    void showPlayList();

    void hidePlayList();

}
