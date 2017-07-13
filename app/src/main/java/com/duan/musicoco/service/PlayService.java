package com.duan.musicoco.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.duan.musicoco.app.RootService;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 * 启动该服务的应用应确保获取了文件读取权限
 */

public class PlayService extends RootService {

    private static final String TAG = "PlayService";

    private PlayServiceIBinder iBinder;

    @Override
    public void onCreate() {
        super.onCreate();

        iBinder = new PlayServiceIBinder(getApplicationContext());
        new ServiceInit(iBinder, mediaManager, playPreference, dbController).start();
        iBinder.notifyDataIsReady();

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
        if (iBinder.isBinderAlive()) {
            iBinder.releaseMediaPlayer();
        }
        super.onDestroy();
    }

}
