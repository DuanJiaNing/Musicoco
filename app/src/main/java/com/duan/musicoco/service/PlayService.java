package com.duan.musicoco.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.MediaManager;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 */

public class PlayService extends Service {

    private static final String TAG = "PlayService";

    private PlayServiceIBinder iBinder;

    private MediaManager mediaManager;

    @Override
    public void onCreate() {

        mediaManager = MediaManager.getInstance(getApplicationContext());

        //获得播放列表
        //替换获取方式，从配置文件读取当前播放列表及当前播放曲目
        // 配置文件无法跨进程共享，同步工作由客户端负责
        // getSongList 耗时方法
        List<Song> songs = mediaManager.getSongList();
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

        return iBinder;
    }

    @Override
    public void onDestroy() {
        if (iBinder.isBinderAlive())
            iBinder.releaseMediaPlayer();
        super.onDestroy();
    }

}
