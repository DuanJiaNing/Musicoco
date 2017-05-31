package com.duan.musicoco.fragment.album;

import com.duan.musicoco.BaseView;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<PresenterContract> {

    void startSpin();

    void stopSpin();

    void changeSong(Song song);

}
