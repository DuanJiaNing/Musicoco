package com.duan.musicoco.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.duan.musicoco.aidl.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 */

public class PlayService extends Service {

    private PlayServiceIBinder mIBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        //FIXME
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("a"));
        songs.add(new Song("b"));
        songs.add(new Song("c"));
        songs.add(new Song("d"));
        this.mIBinder = new PlayServiceIBinder(getApplicationContext(),songs);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }


}
