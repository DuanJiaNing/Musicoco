package com.duan.musicoco;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.OnSongChangedListener;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.service.PlayManager;
import com.duan.musicoco.service.PlayService;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity {

    private IPlayControl mControl;

    private final int BIND_SERVICE = 1;

    private ArrayList<Song> songs;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == BIND_SERVICE) {

                Intent intent = new Intent(PlayActivity.this, PlayService.class);
                intent.putParcelableArrayListExtra("songs", songs);
                bindService(intent, mConnection, Service.BIND_AUTO_CREATE);

            }
            return true;
        }
    });

    private IBinder.DeathRecipient mRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied: service dead");
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mControl = IPlayControl.Stub.asInterface(service);

            try {
                service.linkToDeath(mRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            initService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: service dead");

        }
    };

    private void initService() {
        try {
            mControl.registerOnSongChangedListener(new OnSongChangedListener() {

                @Override
                public void onSongChange(Song which, int index) {
                    Log.d(TAG, "onSongChange: song path=" + which.path + " index=" + index);
                    //FIXME 测试非 UI 线程操作 UI 控件
                    Toast.makeText(PlayActivity.this, "sadfafaf", Toast.LENGTH_SHORT).show();
                }
                
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        songs = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<SongInfo> infos = new MediaManager(PlayActivity.this).refreshData();
                for (SongInfo i : infos) {
                    Song s = new Song(i.getData());
                    songs.add(s);
                    Log.d(TAG, "run: " + i.getData());
                }

                mHandler.sendEmptyMessage(BIND_SERVICE);
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
