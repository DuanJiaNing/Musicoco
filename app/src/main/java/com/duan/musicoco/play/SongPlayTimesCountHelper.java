package com.duan.musicoco.play;

import android.content.Context;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.DBMusicocoController;


/**
 * Created by DuanJiaNing on 2017/8/27.
 * 歌曲播放次数计算策略
 */
// UPDATE: 2017/8/27 更新 计数策略完善
public class SongPlayTimesCountHelper {

    private long startTime;
    private long stopTime;

    private final Context context;
    protected final DBMusicocoController dbController;

    private Song sentenceSong;

    public SongPlayTimesCountHelper(Context context, DBMusicocoController dbController) {
        this.context = context;
        this.dbController = dbController;
    }

    public void startClocking() {
        startTime = System.currentTimeMillis();
    }

    public void stopClicking() {
        stopTime = System.currentTimeMillis();
    }

    private void sentence() {
        int sen = context.getResources().getInteger(R.integer.song_play_times_sentence_duration);
        if (stopTime - startTime > sen) {
            dbController.addSongPlayTimes(sentenceSong);
        }
    }

    public void songChange(Song song, boolean playing) {
        if (sentenceSong != null && playing) {
            stopClicking();
        }

        sentenceSong = song;
        if (playing) {
            startClocking();
        }
    }

}
