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
    public static final String KEY_CURRENT_SONG = "key_current_song";

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    public PlayPreference(Context context) {
        //FIXME
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
            preferences = context.getSharedPreferences(PLAY_PREFERENCE, Context.MODE_PRIVATE);
        else
            preferences = context.getSharedPreferences(PLAY_PREFERENCE, Context.MODE_MULTI_PROCESS | MODE_WORLD_READABLE);
    }

    //该方法在 PlayService 进程中调用，无法保证与 getCurrntSong 同步
    public void updateCurrentSong(@NonNull String fillPath) {
        editor = preferences.edit();
        editor.putString(KEY_CURRENT_SONG, fillPath);
        editor.commit();
    }

    @Nullable
    public Song getCurrntSong() {
        String pa = preferences.getString(KEY_CURRENT_SONG, null);
        Song song = new Song(pa);
        return song;
    }

}
