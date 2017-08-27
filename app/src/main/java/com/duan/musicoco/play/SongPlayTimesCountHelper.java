package com.duan.musicoco.play;

import android.content.Context;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.DBMusicocoController;

/**
 * Created by DuanJiaNing on 2017/8/27.
 * 歌曲播放次数计算策略
 */

public class SongPlayTimesCountHelper {

    private long startTime;
    private long stopTime;

    private final Context context;
    protected final DBMusicocoController dbController;

    private Song currentSong;
    private Song previousSentenceSong;

    public SongPlayTimesCountHelper(Context context, DBMusicocoController dbController) {
        this.context = context;
        this.dbController = dbController;
    }

    private void startClocking() {
        startTime = System.currentTimeMillis();
    }

    private void stopClicking() {
        stopTime = System.currentTimeMillis();
    }

    private void sentence() {
        int sen = context.getResources().getInteger(R.integer.song_play_times_sentence_duration);
        if (stopTime - startTime > sen) {
            dbController.addSongPlayTimes(previousSentenceSong);
        }
    }

    public void songChange(Song song, boolean playing) {
        currentSong = song;
        if (playing) {
            // 正在
            sentence();
            previousSentenceSong = currentSong;

            startClocking();
        }
    }


}
