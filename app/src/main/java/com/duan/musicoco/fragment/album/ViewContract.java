package com.duan.musicoco.fragment.album;

import com.duan.musicoco.BaseView;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public interface ViewContract extends BaseView<PresenterContract> {

    //开始更新图片展示台
    void startSpin();

    //停止更新图片展示台
    void stopSpin();

    //歌曲切换
    //dir 为 0 为上一曲，为 1 为下一曲
    void songChanged(Song song,int dir);

    //activity 从暂停或停止状态恢复是恢复展台状态
    void updateSpinner();

}
