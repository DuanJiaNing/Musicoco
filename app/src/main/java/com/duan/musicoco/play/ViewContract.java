package com.duan.musicoco.play;

import com.duan.musicoco.BaseView;
import com.duan.musicoco.main.PresenterContract;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<PresenterContract> {

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

    void hideLyricFragment();

    void showLyricFragment();

}
