package com.duan.musicoco.aidl;

import android.os.IBinder;

/**
 * Created by DuanJiaNing on 2017/5/29.
 * 自动播放时的播放状态改变回调
 */

public abstract class OnPlayStatusChangedListener extends IOnPlayStatusChangedListener.Stub {

    @Override
    public IBinder asBinder() {
        return super.asBinder();
    }

    /**
     * 自动播放时开始播放曲目时回调
     * @param song 当前开始播放曲目
     * @param index 播放列表下标
     */
    @Override
    public abstract void playStart(Song song, int index);

    /**
     * 自动播放时播放曲目播放完成时回调，一般情况下该方法调用后{@link #playStart(Song, int)}将会被调用
     * @param song 当前播放曲目
     * @param index 播放列表下标
     */
    @Override
    public abstract void playStop(Song song, int index);

}
