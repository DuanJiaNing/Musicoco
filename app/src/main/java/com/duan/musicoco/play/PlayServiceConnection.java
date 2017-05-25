package com.duan.musicoco.play;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.OnSongChangedListener;
import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class PlayServiceConnection implements ServiceConnection {

    public boolean hasConnected = false;

    public IPlayControl mControl;

    private Contract.Presenter mPresenter;

    private Activity mActivity;

    public PlayServiceConnection(Contract.Presenter presenter, Activity activity) {
        this.mActivity = activity;
        this.mPresenter = presenter;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        hasConnected = true;

        mControl = IPlayControl.Stub.asInterface(service);

        try {
            mControl.registerOnSongChangedListener(new OnSongChangedListener() {
                @Override
                public void onSongChange(Song which, int index) {
                    final Song s = which;
                    final int in = index;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.songChanged(s, in);
                        }
                    });
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        hasConnected = false;
    }
}
