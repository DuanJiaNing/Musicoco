package com.duan.musicoco.play;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.BaseView;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<BasePresenter> {

    void songChanged(Song song, int index);

    void startPlay(Song song, int index);

    void stopPlay(Song song, int index);

    void onConnected();

}
