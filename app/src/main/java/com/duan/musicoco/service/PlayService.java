package com.duan.musicoco.service;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootService;
import com.duan.musicoco.preference.PlayPreference;

import java.util.List;

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

        //获得播放列表
        //替换获取方式，从配置文件读取当前播放列表及当前播放曲目
        // 配置文件无法跨进程共享，同步工作由客户端负责
        // getSongList 耗时方法
        iBinder = new PlayServiceIBinder(getApplicationContext(), mediaManager.getSongList());

        //循环模式
        int cm = playPreference.getPlayMode();
        iBinder.setPlayMode(cm);

        //恢复上次播放状态
        Song song = null;
        PlayPreference.CurrentSong cur = playPreference.getSong();
        if (cur != null && cur.path != null)
            song = new Song(cur.path);

        List<Song> songs = iBinder.getPlayList();

        if (songs != null && songs.size() > 0) {
            if (song == null) {  //配置文件没有保存【最后播放曲目】信息（通常为第一次打开应用）
                song = songs.get(0);
            } else { //配置文件有保存
                if (!songs.contains(song)) { //确认服务端有此歌曲
                    song = songs.get(0);
                }
            }

            try {
                // songChanged 将被回调
                iBinder.setCurrentSong(song);
                Log.d(TAG, "onCreate: current song: " + song.path);

                int pro = cur.progress;
                if (pro >= 0) {
                    iBinder.seekTo(pro);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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
