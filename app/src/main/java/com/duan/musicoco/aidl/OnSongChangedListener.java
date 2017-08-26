package com.duan.musicoco.aidl;

import android.os.IBinder;

/**
 * Created by DuanJiaNing on 2017/5/24.
 * 用户主动切换歌曲时回调，包含如下几种情况：
 * 1 播放相同播放列表中指定曲目
 * 2 切换到 前一首
 * 3 切换到后一首
 * 4 切换播放列表
 */

public abstract class OnSongChangedListener extends IOnSongChangedListener.Stub {

    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    @Override
    /**
     * 该方法在服务端线程的 Binder 线程池中运行，客户端调用时不能操作 UI 控件
     */
    public abstract void onSongChange(Song which, int index, boolean isNext);
}
