package com.duan.musicoco.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;

import com.duan.musicoco.app.manager.BroadcastManager;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 该服务将在独立的进程中运行
 * 只负责对播放列表中的歌曲进行播放
 * 启动该服务的应用应确保获取了文件读取权限
 */

public class PlayService extends RootService {

    private static final String TAG = "PlayService";

    public PlayServiceIBinder iBinder;
    private BroadcastManager broadcastManager;

    private BroadcastReceiver serviceQuitReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastManager = BroadcastManager.getInstance();
        iBinder = new PlayServiceIBinder(this);

        new ServiceInit(this,
                iBinder,
                mediaManager,
                playPreference,
                dbController,
                settingPreference).start();

        iBinder.notifyDataIsReady();

        initBroadcast();

    }

    private void initBroadcast() {
        serviceQuitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (iBinder.status() == PlayController.STATUS_PLAYING) {
                    iBinder.pause();
                }
                iBinder.releaseMediaPlayer();
                stopSelf();
            }
        };

        broadcastManager.registerBroadReceiver(this, serviceQuitReceiver, BroadcastManager.FILTER_PLAY_SERVICE_QUIT);
    }

    @Override
    public IBinder onBind(Intent intent) {

        int check = checkCallingOrSelfPermission("com.duan.musicoco.permission.ACCESS_PLAY_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            //客户端的 onServiceConnected 方法不会被调用
            return null;
        }

        return iBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        iBinder.releaseMediaPlayer();
        unregisterReceiver();

        // 释放 MediaPlayer 时有错误，服务端始终没有彻底关闭，【退出】应用后再次打开应用，启动服务时，
        // 调用 MediaPlayer 的 reset 方法抛出异常，java.lang.illageStatExeception
        Process.killProcess(Process.myPid());
    }

    private void unregisterReceiver() {
        if (serviceQuitReceiver != null) {
            broadcastManager.unregisterReceiver(this, serviceQuitReceiver);
        }

    }

}
