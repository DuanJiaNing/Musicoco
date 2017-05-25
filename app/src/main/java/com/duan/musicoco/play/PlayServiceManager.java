package com.duan.musicoco.play;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.service.PlayService;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class PlayServiceManager {

    private Context mContext;

    private ServiceConnection mConnection;

    private Contract.Presenter mPresenter;

    private ArrayList<Song> songs;

    private final int BIND_SERVICE = 1;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == BIND_SERVICE) {

                Intent intent = new Intent(mContext, PlayService.class);
                intent.putParcelableArrayListExtra("songs", songs);
                mContext.bindService(intent,mConnection, Service.BIND_AUTO_CREATE);

            }
            return true;
        }
    });

    public PlayServiceManager(Context context, ServiceConnection connection, Contract.Presenter presenter){
        this.mContext = context;
        this.mConnection = connection;
        this.mPresenter = presenter;
        songs = new ArrayList<>();
    }

    public void startPlayService() {

        //启动服务
        Intent intent = new Intent(mContext, PlayService.class);
        mContext.startService(intent);

        //获取音乐数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<SongInfo> infos = new MediaManager(mContext).refreshData();
                for (SongInfo i : infos) {
                    Song s = new Song(i.getData());
                    songs.add(s);
                    Log.d("main", "run: " + i.getData());
                }

                mHandler.sendEmptyMessage(BIND_SERVICE);
            }
        }).start();

    }

}
