package com.duan.musicoco.aidl;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.duan.musicoco.service.PlayController;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.<br><br>
 * 仅有从 {@link com.duan.musicoco.aidl.IPlayControl}.aidl 继承的方法在跨进程调用时有效<br>
 * 1. 该类中的方法运行在服务端 Binder 线程池中，所有需要处理线程同步<br>
 * 2. 这些方法被客户端调用时客户端线程会被挂起，如果客户端的线程为 UI 线程，注意处理耗时操作以避免出现的 ANR<br>
 */

public class PlayControlImpl extends com.duan.musicoco.aidl.IPlayControl.Stub {

    protected RemoteCallbackList<IOnSongChangedListener> mListeners;

    private PlayController manager;

    public PlayControlImpl(List<Song> songs) {
        this.mListeners = new RemoteCallbackList<>();
        this.manager = PlayController.getMediaController(songs);
    }

    @Override
    public synchronized int play(Song which) {
        if (manager.getCurrentSong() != which)
            notifySongChange(which, manager.getSongsList().indexOf(which));
        int re = manager.play(which);
        return re;
    }

    @Override
    public synchronized Song pre() {
        Song pre = manager.getCurrentSong();
        Song s = manager.preSong();
        if (s != pre)
            notifySongChange(s, manager.getSongsList().indexOf(s));
        return s;
    }

    @Override
    public synchronized Song next() {
        Song pre = manager.getCurrentSong();
        Song next = manager.nextSong();
        if (next != pre)
            notifySongChange(next, manager.getSongsList().indexOf(next));
        return next;
    }

    @Override
    public synchronized int pause() {
        return manager.pause();
    }

    @Override
    public synchronized int resume() {
        return manager.resume();
    }

    @Override
    public Song currentSong() {
        return manager.getCurrentSong();
    }

    @Override
    public int status() {
        return manager.getPlayState();
    }

    @Override
    public synchronized Song setPlayList(List<Song> songs) {
        Song n = manager.setPlayList(songs);
        notifySongChange(n,0);
        return n;
    }

    @Override
    public List<Song> getPlayList() {
        return manager.getSongsList();
    }

    @Override
    public synchronized void setPlayMode(int mode) {
        if (mode >= PlayController.MODE_DEFAULT && mode <= PlayController.MODE_RANDOM)
            manager.setPlayMode(mode);
    }

    @Override
    public int getProgress() {
        return manager.getProgress();
    }

    @Override
    public synchronized int seekTo(int pos) {
        return manager.seekTo(pos);
    }

    @Override
    public void registerOnSongChangedListener(IOnSongChangedListener li) {
        mListeners.register(li);
    }

    @Override
    public void unregisterOnSongCHangedListener(IOnSongChangedListener li) {
        mListeners.unregister(li);
    }

    private void notifySongChange(Song song, int index) {
        final int N = mListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnSongChangedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onSongChange(song, index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListeners.finishBroadcast();
    }

    public void releaseMediaPlayer(){
        manager.releaseMediaPlayer();
    }

}
