package com.duan.musicoco.play;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.BaseView;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface Contract {

    //TODO 补充

    interface View extends BaseView<Presenter> {

        void songChanged(Song song, int index);

    }

    interface Presenter extends BasePresenter {

        /**
         * 当前播放曲目改变
         * @param song 改变后曲目
         * @param index 播放列表对应下标
         */
        void songChanged(Song song, int index);

    }
}
