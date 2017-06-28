package com.duan.musicoco.play;

import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/6/28.
 */

public interface IPlayActivity {

    void noSongsInDisk();

    void showPlayList();

    void hidePlayList();

    void showDetailDialog(Song song);

}
