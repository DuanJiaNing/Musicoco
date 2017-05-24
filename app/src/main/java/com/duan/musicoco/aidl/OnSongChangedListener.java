package com.duan.musicoco.aidl;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by DuanJiaNing on 2017/5/24.
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
    public abstract void onSongChange(Song which, int index);
}
