package com.duan.musicoco.service;

import android.content.Context;

import com.duan.musicoco.aidl.PlayControlImpl;
import com.duan.musicoco.aidl.Song;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 仅有从 {@link com.duan.musicoco.aidl.IPlayControl} 或 {@link com.duan.musicoco.aidl_.IPlayControl}
 * 继承的方法在跨进程调用时有效，
 * 这些方法运行在线程池中的，所以需要你自己完成线程同步的工作
 */

public class PlayServiceIBinder extends PlayControlImpl {

    private Context mContext;

    public PlayServiceIBinder(Context context, List<Song> songs) {
        super(songs);
        this.mContext = context;
    }
}
