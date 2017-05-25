package com.duan.musicoco.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.duan.musicoco.aidl.Song;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 */

public class PlayService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        List<Song> songs = intent.getParcelableArrayListExtra("songs");
        final PlayServiceIBinder binder = new PlayServiceIBinder(this, songs);
        new Thread() {
            @Override
            public void run() {
                binder.setPlayMode(PlayController.MODE_RANDOM);
                for (; ; ) {
                    SystemClock.sleep(3000);
                    binder.pre();

                }
            }
        }.start();

        return binder;
    }


}
