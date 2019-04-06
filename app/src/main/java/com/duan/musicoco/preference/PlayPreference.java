package com.duan.musicoco.preference;

import android.content.Context;
import android.support.annotation.Nullable;

import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.service.PlayController;

/**
 * Created by DuanJiaNing on 2017/6/2.
 * 播放界面配置：播放界面主题，播放模式，播放曲目、所属歌单、播放进度
 */

public class PlayPreference extends BasePreference {

    public static final String KEY_PLAY_MODE = "key_play_mode";
    public static final String KEY_THEME = "key_theme";
    public static final String KEY_SHEET = "key_sheet";
    public static final String KEY_PLAY_BG_MODE = "key_play_bg_mode";

    public PlayPreference(Context context) {
        super(context, Preference.PLAY_PREFERENCE);
    }

    public void updateTheme(ThemeEnum themeEnum) {
        editor = preferences.edit();
        editor.putString(KEY_THEME, themeEnum.name());
        editor.apply();
    }

    public void updatePlayBgMode(PlayBackgroundModeEnum mode) {
        editor = preferences.edit();
        editor.putString(KEY_PLAY_BG_MODE, mode.name());
        editor.apply();
    }

    public ThemeEnum getTheme() {
        String pa = preferences.getString(KEY_THEME, ThemeEnum.VARYING.name());
        return ThemeEnum.valueOf(pa);
    }

    public PlayBackgroundModeEnum getPlayBgMode() {
        String str = preferences.getString(KEY_PLAY_BG_MODE, PlayBackgroundModeEnum.COLOR.name());
        return PlayBackgroundModeEnum.valueOf(str);
    }


    public int updatePlayMode(int mode) {
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
        return preferences.getInt(KEY_PLAY_MODE, PlayController.MODE_LIST_LOOP);
    }

    //注意在多进程下的修改不可见问题
    public void updateLastPlaySong(CurrentSong song) {

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
    public CurrentSong getLastPlaySong() {

        String p = preferences.getString(CurrentSong.KEY_CURRENT_SONG_PATH, null);
        int in = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_INDEX, 0);
        int pro = preferences.getInt(CurrentSong.KEY_CURRENT_SONG_PLAY_PROGRESS, 0);

        return new CurrentSong(p, pro, in);
    }

    /**
     * 0 为全部，其它的歌单 信息 可从 {@link com.duan.musicoco.db.DBMusicocoController#getSheet(String)}等方法获得
     */
    public void updateSheet(int sheetID) {

        editor = preferences.edit();
        editor.putInt(KEY_SHEET, sheetID);
        editor.apply();
    }

    /**
     * 0 为全部，其它的歌单 信息 可从 {@link com.duan.musicoco.db.DBMusicocoController#getSheet(String)}等方法获得
     */
    public int getSheetID() {
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
