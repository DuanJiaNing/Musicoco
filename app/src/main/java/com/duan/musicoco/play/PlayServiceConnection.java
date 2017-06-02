package com.duan.musicoco.play;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.OnPlayStatusChangedListener;
import com.duan.musicoco.aidl.OnSongChangedListener;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PlayServiceManager;

/**
 * Created by DuanJiaNing on 2017/5/25.
 * 充当控制类
 */

public class PlayServiceConnection implements ServiceConnection {

    public boolean hasConnected = false;

    private IPlayControl mControl;

    private Activity mActivity;

    private ActivityViewContract mView;

    private OnPlayStatusChangedListener mPlayStatusChangedListener;
    private OnSongChangedListener mSongChangedListener;

    public PlayServiceConnection(ActivityViewContract view, Activity activity) {
        this.mActivity = activity;
        this.mView = view;
        this.mSongChangedListener = new OnSongChangedListener() {
            @Override
            public void onSongChange(Song which, int index) {
                final Song s = which;
                final int in = index;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.songChanged(s, in);
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
                        mView.startPlay(s, in,status);
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
                        mView.stopPlay(s, in,status);
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
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mView.onConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        hasConnected = false;

        PlayServiceManager.bindService(mActivity, this);

    }

    public void unregisterListener() {

        try {
            mControl.unregisterOnPlayStatusChangedListener(mPlayStatusChangedListener);
            mControl.unregisterOnSongChangedListener(mSongChangedListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IPlayControl takeControl() {
        return mControl;
    }
}
