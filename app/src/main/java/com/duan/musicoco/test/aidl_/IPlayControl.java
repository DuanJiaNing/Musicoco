package com.duan.musicoco.test.aidl_;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.duan.musicoco.aidl.Song;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 手写的 aidl 自动生成的 .java 文件
 * 该接口中声明的方法只有被 {@link android.os.Binder} 实现后才能跨进程调用，已有的子类实现为 {@link PlayControlImpl}
 */

public interface IPlayControl extends IInterface {

    String DESCRIPTOR = "com.duan.musicoco.aidl.IPlayControl";

    int TRANSACTION_play = IBinder.FIRST_CALL_TRANSACTION + 0;

    int TRANSACTION_pause = IBinder.FIRST_CALL_TRANSACTION + 1;

    int TRANSACTION_resume = IBinder.FIRST_CALL_TRANSACTION + 2;

    int TRANSACTION_currentSong = IBinder.FIRST_CALL_TRANSACTION + 3;

    int TRANSACTION_status = IBinder.FIRST_CALL_TRANSACTION + 4;

    int TRANSACTION_setPlayList = IBinder.FIRST_CALL_TRANSACTION + 5;

    int TRANSACTION_getPlayList = IBinder.FIRST_CALL_TRANSACTION + 6;

    //播放指定歌曲（包含上一曲，下一曲）
    int play(Song which) throws RemoteException;

    //暂停
    int pause() throws RemoteException;

    //继续
    int resume() throws RemoteException;

    //当前播放歌曲
    Song currentSong() throws RemoteException;

    //播放状态
    int status() throws RemoteException;

    //设置播放列表，返回下一首播放歌曲
    Song setPlayList(List<Song> songs) throws RemoteException;

    //获得播放列表
    List<Song> getPlayList() throws RemoteException;

}
