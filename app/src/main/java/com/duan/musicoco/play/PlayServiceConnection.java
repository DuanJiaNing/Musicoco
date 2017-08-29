package com.duan.musicoco.play;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.OnDataIsReadyListener;
import com.duan.musicoco.aidl.OnPlayListChangedListener;
import com.duan.musicoco.aidl.OnPlayStatusChangedListener;
import com.duan.musicoco.aidl.OnSongChangedListener;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.service.PlayServiceCallback;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class PlayServiceConnection implements ServiceConnection {

    public boolean hasConnected = false;

    private IPlayControl mControl;
    private Activity mActivity;

    private PlayServiceCallback serviceCallback;
    private OnServiceConnect serviceConnect;

    private OnPlayStatusChangedListener mPlayStatusChangedListener;
    private OnSongChangedListener mSongChangedListener;
    private OnPlayListChangedListener mPlayListChangedListener;
    private OnDataIsReadyListener mDataIsReadyListener;

    public PlayServiceConnection(PlayServiceCallback callback, final OnServiceConnect serviceConnect, Activity activity) {
        this.mActivity = activity;
        this.serviceCallback = callback;
        this.serviceConnect = serviceConnect;
        this.mSongChangedListener = new OnSongChangedListener() {
            @Override
            public void onSongChange(Song which, int index, boolean isNext) {
                final Song s = which;
                final int in = index;
                final boolean isn = isNext;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceCallback.songChanged(s, in, isn);
                    }
                });
            }
        };

        this.mDataIsReadyListener = new OnDataIsReadyListener() {
            @Override
            public void dataIsReady() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceCallback.dataIsReady(mControl);
                    }
                });
            }
        };

        this.mPlayListChangedListener = new OnPlayListChangedListener() {
            @Override
            public void onPlayListChange(Song current, int index, int id) {
                final Song s = current;
                final int in = index;
                final int i = id;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceCallback.onPlayListChange(s, in, i);
                    }
                });
            }
        };

        this.mPlayStatusChangedListener = new OnPlayStatusChangedListener() {
            @Override
            public void playStart(Song song, int index, final int status) {
                final Song s = song;
                final int in = index;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceCallback.startPlay(s, in, status);
                    }
                });
            }

            @Override
            public void playStop(Song song, int index, final int status) {
                final Song s = song;
                final int in = index;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceCallback.stopPlay(s, in, status);
                    }
                });
            }
        };
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        hasConnected = true;

        mControl = IPlayControl.Stub.asInterface(service);

        try {
            mControl.registerOnPlayStatusChangedListener(mPlayStatusChangedListener);
            mControl.registerOnSongChangedListener(mSongChangedListener);
            mControl.registerOnPlayListChangedListener(mPlayListChangedListener);
            mControl.registerOnDataIsReadyListener(mDataIsReadyListener);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        serviceConnect.onConnected(name, service);
    }

    // 在 Service 移除终止时才会回调，所以在手动解绑时需要将 hasConnected 置为 false
    @Override
    public void onServiceDisconnected(ComponentName name) {
        hasConnected = false;
        serviceConnect.disConnected(name);
    }

    public void unregisterListener() {

        try {
            mControl.unregisterOnPlayStatusChangedListener(mPlayStatusChangedListener);
            mControl.unregisterOnSongChangedListener(mSongChangedListener);
            mControl.unregisterOnPlayListChangedListener(mPlayListChangedListener);
            mControl.unregisterOnDataIsReadyListener(mDataIsReadyListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public IPlayControl takeControl() {
        return mControl;
    }
}
