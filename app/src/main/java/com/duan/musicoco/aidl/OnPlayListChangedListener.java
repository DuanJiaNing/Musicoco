package com.duan.musicoco.aidl;

import android.os.IBinder;

/**
 * Created by DuanJiaNing on 2017/5/24.
 * 服务端的播放列表改变时回调
 * 1 改变歌单时回调
 * 2 从歌单中移除歌曲时回调
 */

public abstract class OnPlayListChangedListener extends IOnPlayListChangedListener.Stub {

    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    /**
     * 服务端的播放列表改变时回调
     *
     * @param current 当前曲目
     * @param index   曲目下标
     * @param id      歌单 id
     */
    @Override
    public abstract void onPlayListChange(Song current, int index, int id);

}
