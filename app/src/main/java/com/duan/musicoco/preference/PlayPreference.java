package com.duan.musicoco.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.service.PlayController;

import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by DuanJiaNing on 2017/6/2.
 */

public class PlayPreference {

    public static final String PLAY_PREFERENCE = "play_preference";
    public static final String KEY_PLAY_MODE = "key_play_mode";
    public static final String KEY_THEME = "key_theme";
    public static final String KEY_SHEET = "key_sheet";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private Context context;

    public PlayPreference(Context context) {
        this.context = context;
    }

    private void check() {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PLAY_PREFERENCE, Context.MODE_PRIVATE);
        }
    }

    public void updateTheme(Theme theme) {
        check();
        editor = preferences.edit();
        editor.putString(KEY_THEME, theme.name());
        editor.apply();
        Log.d("musicoco", "updateColors: theme " + theme.name());
    }

    public Theme getTheme() {
        check();
        String pa = preferences.getString(KEY_THEME, Theme.VARYING.name());
        return Theme.valueOf(pa);
    }

    public int updatePlayMode(int mode) {
        check();
        //不考虑默认模式
        // 21 列表循环
        // 22 单曲循环
        // 23 随机播放
        if (mode < 21 || mode > 23)
            mode = 21;
        editor = preferences.edit();
        editor.putInt(KEY_PLAY_MODE, mode);
        editor.apply();
        return mode;
    }

    public int getPlayMode() {
        check();
        return preferences.getInt(KEY_PLAY_MODE, PlayController.MODE_LIST_LOOP);
    }

    //注意在多进程下的修改不可见问题
    public void updateSong(CurrentSong song) {
        check();

        if (song == null) {
            return;
        }

        editor = preferences.edit();
        editor.putString(CurrentSong.KEY_CURRENT_SONG_PATH, song.path);
        editor.putInt(CurrentSong.KEY_CURRENT_SONG_INDEX, song.index);
        editor.putInt(CurrentSong.KEY_CURRENT_SONG_PLAY_PROGRESS, song.progress);
        editor.apply();

    }

    @Nullable
    public CurrentSong getSong() {
        check();

        String p = preferences.getString(CurrentSong.KEY_CURRENT_SONG_PATH, null);
        int in = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_INDEX, 0);
        int pro = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_PLAY_PROGRESS, 0);

        return new CurrentSong(p, pro, in);
    }

    /**
     * 0 为全部，其它的歌单 信息 可从 {@link com.duan.musicoco.db.DBMusicocoController#getSheet(String)}等方法获得
     */
    public void updateSheet(int sheetID) {
        check();
        if (sheetID < 0) {
            return;
        }

        editor = preferences.edit();
        editor.putInt(KEY_SHEET, sheetID);
        editor.apply();
    }

    /**
     * 0 为全部，其它的歌单 信息 可从 {@link com.duan.musicoco.db.DBMusicocoController#getSheet(String)}等方法获得
     */
    public int getSheetID() {
        check();
        return preferences.getInt(KEY_SHEET, MainSheetHelper.SHEET_ALL);
    }

    public static class CurrentSong {

        public static final String KEY_CURRENT_SONG_PATH = "key_current_song";
        public static final String KEY_CURRENT_SONG_PLAY_PROGRESS = "key_current_song_play_progress";
        public static final String KEY_CURRENT_SONG_INDEX = "key_current_song_index";

        public String path;
        public int progress;
        public int index;

        public CurrentSong(String path, int progress, int index) {
            this.path = path;
            this.progress = progress;
            this.index = index;
        }
    }

}
