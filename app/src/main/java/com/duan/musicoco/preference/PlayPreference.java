package com.duan.musicoco.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duan.musicoco.aidl.Song;

import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by DuanJiaNing on 2017/6/2.
 */

public class PlayPreference {

    public static final String PLAY_PREFERENCE = "play_preference";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    public PlayPreference(Context context) {
        //FIXME
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
            preferences = context.getSharedPreferences(PLAY_PREFERENCE, Context.MODE_PRIVATE);
        else
            preferences = context.getSharedPreferences(PLAY_PREFERENCE, Context.MODE_MULTI_PROCESS | MODE_WORLD_READABLE);
    }

    //注意在多进程下的修改不可见问题
    public void updateCurrentSong(CurrentSong song) {
        if (song == null)
            return;

        editor = preferences.edit();
        editor.putString(CurrentSong.KEY_CURRENT_SONG_PATH, song.path);
        editor.putInt(CurrentSong.KEY_CURRENT_SONG_INDEX, song.index);
        editor.putInt(CurrentSong.KEY_CURRENT_SONG_PLAY_PROGRESS, song.progress);

        editor.apply();
    }

    @Nullable
    public CurrentSong getCurrentSong() {

        String p = preferences.getString(CurrentSong.KEY_CURRENT_SONG_PATH, null);
        int in = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_INDEX, 0);
        int pro = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_PLAY_PROGRESS, 0);

        return new CurrentSong(p, pro, in);
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
