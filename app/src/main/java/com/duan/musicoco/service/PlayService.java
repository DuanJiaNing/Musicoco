package com.duan.musicoco.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 */

public class PlayService extends Service {

    private static final String TAG = "PlayService";

    private PlayServiceIBinder iBinder;

    @Override
    public void onCreate() {
        List<Song> songs = new ArrayList<>();

        //获得播放列表
        //TODO 替换获取方式
        HashSet<SongInfo> infos = MediaManager.getInstance().refreshData(getApplicationContext());
        for (SongInfo i : infos) {
            Song s = new Song(i.getData());
            songs.add(s);
        }

        iBinder = new PlayServiceIBinder(getApplicationContext(), songs);

    }

    @Override
    public IBinder onBind(Intent intent) {

        int check = checkCallingOrSelfPermission("com.duan.musicoco.permission.ACCESS_PLAY_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "you need declare permission 'com.duan.musicoco.permission.ACCESS_PLAY_SERVICE' to access this service.");
            //客户端的 onServiceConnected 方法不会被调用
            return null;
        }

        new Thread() {
            @Override
            public void run() {
                iBinder.setPlayMode(PlayController.MODE_RANDOM);
                for (; ; ) {
                    SystemClock.sleep(3000);
                    iBinder.pre();

                }
            }
        }.start();

        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (iBinder.isBinderAlive())
            iBinder.releaseMediaPlayer();
        super.onDestroy();
    }

}
