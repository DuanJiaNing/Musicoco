package com.duan.musicoco;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.service.PlayService;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity {

    private IPlayControl mControl;

    private IBinder.DeathRecipient mRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Toast.makeText(PlayActivity.this, "服务 death", Toast.LENGTH_SHORT).show();
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mControl = IPlayControl.Stub.asInterface(service);

            try {
                for (Song s :
                        mControl.getPlayList())
                    Log.i("Main", "onServiceConnected: " + s.path);
                service.linkToDeath(mRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mConnection, Service.BIND_AUTO_CREATE);

    }
}
