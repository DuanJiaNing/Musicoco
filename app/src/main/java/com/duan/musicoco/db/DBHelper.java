package com.duan.musicoco.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.SongInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/6/30.
 */

public class DBHelper {

    private Context context;

    private SQLiteDatabase database;

    public static final String TABLE_SONG = "song";
    public static final String SONG_ID = "_id"; //主键
    public static final String SONG_PATH = "path"; //路径
    public static final String SONG_LASTPLAYTIME = "last_play"; //最后播放时间
    public static final String SONG_PLAYTIMES = "play_times"; //播放次数
    public static final String SONG_REMARK = "remarks"; //备注
    public static final String SONG_SHEET = "sheets"; //所属歌单 歌单编号，空格隔开

    public static final String TABLE_SHEET = "sheet";
    public static final String SHEET_ID = "_id"; // 主键
    public static final String SHEET_NAME = "name"; //歌单名称
    public static final String SHEET_REMARK = "remarks"; //歌单备注

    public DBHelper(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        String path = context.getFilesDir().getAbsolutePath() + File.separator + "song.db";
        File file = new File(path);
        if (!file.exists()) {
            database = SQLiteDatabase.openOrCreateDatabase(path, null);
            createTable("create table " + TABLE_SONG + "(" +
                    SONG_ID + " integer primary key autoincrement," +
                    SONG_PATH + " text unique," +
                    SONG_LASTPLAYTIME + " char(20)," +
                    SONG_PLAYTIMES + " integer," +
                    SONG_REMARK + " text)");

            createTable("create table " + TABLE_SHEET + "(" +
                    SHEET_ID + " integer primary key autoincrement," +
                    SHEET_NAME + " text unique," +
                    SHEET_REMARK + " text)");
        } else
            database = SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    public void open() {
        String path = context.getFilesDir().getAbsolutePath() + File.separator + "song.db";
        if (database != null && database.isOpen()) ;
        else
            database = SQLiteDatabase.openOrCreateDatabase(path, null);
    }

    public void close() {
        if (database.isOpen())
            database.close();
    }

    private void createTable(String sql) {
        database.execSQL(sql);
    }

    public static class SongInfo {
        public int id;
        public String path;
        public long lastPlayTime;
        public int playTimes;
        public String remark;
    }

    public static class Sheet {
        public int id;
        public String name;
        public String remark;
    }

    @Nullable
    public Sheet getSheet(int sheetId) {
        String sql = "select * from " + TABLE_SHEET + " where id = " + sheetId;
        Cursor cursor = database.rawQuery(sql, null);

        Sheet sheet = new Sheet();
        while (cursor.moveToNext()) {
            sheet.id = cursor.getInt(cursor.getColumnIndex(SHEET_ID));
            sheet.name = cursor.getString(cursor.getColumnIndex(SHEET_NAME));
            sheet.remark = cursor.getString(cursor.getColumnIndex(SHEET_REMARK));
        }

        cursor.close();
        return sheet;
    }

    public List<Sheet> getSheets() {
        String sql = "select * from " + TABLE_SHEET;
        Cursor cursor = database.rawQuery(sql, null);

        List<Sheet> sheets = new ArrayList<>();
        while (cursor.moveToNext()) {
            Sheet sheet = new Sheet();
            sheet.id = cursor.getInt(cursor.getColumnIndex(SHEET_ID));
            sheet.name = cursor.getString(cursor.getColumnIndex(SHEET_NAME));
            sheet.remark = cursor.getString(cursor.getColumnIndex(SHEET_REMARK));
            sheets.add(sheet);
        }

        cursor.close();
        return sheets;
    }

    @Nullable
    public SongInfo getSongInfo(int songId) {
        String sql = "select * from " + TABLE_SONG + " where id = " + songId;
        Cursor cursor = database.rawQuery(sql, null);

        SongInfo info = new SongInfo();
        while (cursor.moveToNext()) {
            info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
            info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));
            String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
            info.lastPlayTime = Long.valueOf(str);
            info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
            info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));
        }

        cursor.close();
        return info;
    }

    public List<SongInfo> getSongInfos() {
        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);

        List<SongInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            SongInfo info = new SongInfo();
            info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
            info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));
            String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
            info.lastPlayTime = Long.valueOf(str);
            info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
            info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));
            infos.add(info);
        }

        cursor.close();
        return infos;
    }

    public void addSongInfo(Song song, @Nullable String remark) {
        String path = song.path;
        //TODO
//        String sql = String.format("insert into %s values(null,)", TABLE_SONG, )

    }

    public void addSongInfo(Song song, @Nullable String remark, Sheet... sheets) {
        ContentValues values = new ContentValues();
        values.put(SONG_PATH, song.path);
        values.put(SONG_LASTPLAYTIME, 0);
        values.put(SONG_PLAYTIMES, 0);
        values.put(SONG_REMARK, remark);
        values.put(SONG_SHEET, );

        database.insert()

    }

    /**
     * 更新歌曲最后播放时间
     */
    public void updateSongLastPlayTime(Song song, long time) {
    }

    /**
     * 更新歌曲播放次数
     */
    public void updateSongPlayTime(Song song, long time) {
    }

    /**
     * 更新歌曲备注
     */
    public void updateSongRemark(Song song, String remark) {

    }

    /**
     * 歌曲的播放次数加一
     * 同时修改最后播放时间为当前时间
     */
    public void addTimes(Song song) {
    }

}
