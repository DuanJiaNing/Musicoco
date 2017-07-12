package com.duan.musicoco.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.main.SongSheet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Created by DuanJiaNing on 2017/7/1.
 * 每个线程只能使用一个SQLiteOpenHelper，也就使得每个线程使用一个SQLiteDatabase对象（多线程操作数据库会报错）
 */

public class DBMusicocoController {

    private final Context context;
    private final SQLiteDatabase database;

    private static final String TAG = "DBMusicocoController";

    public static final String DATABASE = "musicoco.db";

    public static final String TABLE_SONG = "song";
    public static final String SONG_ID = "_id"; //主键
    public static final String SONG_PATH = "path"; //路径
    public static final String SONG_LASTPLAYTIME = "last_play"; //最后播放时间
    public static final String SONG_PLAYTIMES = "play_times"; //播放次数
    public static final String SONG_REMARK = "remarks"; //备注
    public static final String SONG_SHEETS = "sheets"; //所属歌单 歌单编号，空格隔开
    public static final String SONG_CREATE = "create_time"; //创建时间
    public static final String SONG_FAVORITE = "song_favorite"; //是否收藏 0 否， 1 是

    public static final String TABLE_SHEET = "sheet";
    public static final String SHEET_ID = "_id"; // 主键
    public static final String SHEET_NAME = "name"; //歌单名称
    public static final String SHEET_REMARK = "remarks"; //歌单备注
    public static final String SHEET_CREATE = "create_time"; //创建时间
    public static final String SHEET_PLAYTIMES = "sheet_playtimes"; //播放次数

    static void createSongTable(SQLiteDatabase db) {
        String sql = "create table " + DBMusicocoController.TABLE_SONG + "(" +
                DBMusicocoController.SONG_ID + " integer primary key autoincrement," +
                DBMusicocoController.SONG_PATH + " text unique," +
                DBMusicocoController.SONG_LASTPLAYTIME + " char(20)," +
                DBMusicocoController.SONG_PLAYTIMES + " integer," +
                DBMusicocoController.SONG_REMARK + " text," +
                DBMusicocoController.SONG_SHEETS + " text," +
                DBMusicocoController.SONG_CREATE + " text," +
                DBMusicocoController.SONG_FAVORITE + " integer)";
        db.execSQL(sql);
    }

    static void createSheetTable(SQLiteDatabase db) {

        String sql = "create table " + DBMusicocoController.TABLE_SHEET + "(" +
                DBMusicocoController.SHEET_ID + " integer primary key autoincrement," +
                DBMusicocoController.SHEET_NAME + " text unique," +
                DBMusicocoController.SHEET_REMARK + " text," +
                DBMusicocoController.SHEET_CREATE + " text," +
                DBMusicocoController.SHEET_PLAYTIMES + " integer)";
        db.execSQL(sql);
    }

    public static class SongInfo {
        public int id;
        public String path;
        public long lastPlayTime;
        public int playTimes;
        public String remark;
        public long create;
        public int[] sheets;
        public boolean favorite;

        public SongInfo() {
        }

        public SongInfo(String path, long lastPlayTime, int playTimes, String remark, long create, int[] sheets, boolean favorite) {
            this.path = path;
            this.lastPlayTime = lastPlayTime;
            this.playTimes = playTimes;
            this.remark = remark;
            this.create = create;
            this.sheets = sheets;
            this.favorite = favorite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SongInfo info = (SongInfo) o;

            return path.equals(info.path);

        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }
    }

    public static class Sheet {
        public int id;
        public String name;
        public String remark;
        public long create;
        public int playTimes;

        public Sheet() {
        }

        public Sheet(String name, String remark) {
            this.name = name;
            this.remark = remark;
            this.create = System.currentTimeMillis();
            this.playTimes = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Sheet sheet = (Sheet) o;

            return name.equals(sheet.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * 在使用结束时应调用{@link #close()}关闭数据库连接
     */
    public DBMusicocoController(Context context, boolean writable) {
        DBHelper helper = DBHelper.getInstance(context, DATABASE);
        if (writable) {
            this.database = helper.getWritableDatabase();
        } else {
            this.database = helper.getReadableDatabase();
        }
        this.context = context;
    }

    public void close() {
        if (database.isOpen()) {
            database.close();
        }
    }

    @Nullable
    public SongInfo getSongInfo(int songId) {
        String sql = "select * from " + TABLE_SONG + " where " + SONG_ID + " = " + songId;
        Cursor cursor = database.rawQuery(sql, null);

        SongInfo info = new SongInfo();
        while (cursor.moveToNext()) {
            info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
            info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));

            String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
            info.lastPlayTime = Long.valueOf(str);

            info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
            info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));

            String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
            info.create = Long.valueOf(str1);

            int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
            info.favorite = far == 1;

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            String[] strs = sh.split(" ");
            int[] shs = new int[strs.length];
            for (int i = 0; i < shs.length; i++) {
                shs[i] = Integer.parseInt(strs[i]);
            }
            info.sheets = shs;

        }

        cursor.close();
        return info;
    }

    @Nullable
    public SongInfo getSongInfo(@NonNull Song song) {
        String sql = "select * from " + TABLE_SONG + " where " + SONG_PATH + " like '" + song.path + "'";
        Cursor cursor = database.rawQuery(sql, null);

        SongInfo info = null;
        while (cursor.moveToNext()) {
            info = new SongInfo();

            info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
            info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));

            String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
            info.lastPlayTime = Long.valueOf(str);

            info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
            info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));

            String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
            info.create = Long.valueOf(str1);

            int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
            info.favorite = far == 1;

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            String[] strs = sh.split(" ");
            int[] shs = new int[strs.length];
            for (int i = 0; i < shs.length; i++) {
                shs[i] = Integer.parseInt(strs[i]);
            }
            info.sheets = shs;
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

            String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
            info.create = Long.valueOf(str1);

            int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
            info.favorite = far == 1;

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            String[] strs = sh.split(" ");
            int[] shs = new int[strs.length];
            for (int i = 0; i < shs.length; i++) {
                shs[i] = Integer.parseInt(strs[i]);
            }
            info.sheets = shs;

            infos.add(info);
        }

        cursor.close();
        return infos;
    }

    public List<SongInfo> getSongInfos(int sheetID) {

        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);

        List<SongInfo> infos = new ArrayList<>();

        while (cursor.moveToNext()) {
            SongInfo info = new SongInfo();

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            String[] strs = sh.split(" ");
            int[] shs = new int[strs.length];
            for (int i = 0; i < shs.length; i++) {
                shs[i] = Integer.parseInt(strs[i]);
            }

            boolean isContain = false;
            for (int i : shs) {
                if (i == sheetID) {
                    isContain = true;
                    Log.d(TAG, "getSongInfos: sheet " + sheetID + " contain:true " + info.path);
                }
            }

            if (isContain) {
                info.sheets = shs;

                info.id = cursor.getInt(cursor.getColumnIndex(SONG_ID));
                info.path = cursor.getString(cursor.getColumnIndex(SONG_PATH));

                String str = cursor.getString(cursor.getColumnIndex(SONG_LASTPLAYTIME));
                info.lastPlayTime = Long.valueOf(str);

                info.playTimes = cursor.getInt(cursor.getColumnIndex(SONG_PLAYTIMES));
                info.remark = cursor.getString(cursor.getColumnIndex(SONG_REMARK));

                String str1 = cursor.getString(cursor.getColumnIndex(SONG_CREATE));
                info.create = Long.valueOf(str1);

                int far = cursor.getInt(cursor.getColumnIndex(SONG_FAVORITE));
                info.favorite = far == 1;

                infos.add(info);
            }
        }

        cursor.close();
        return infos;
    }

    public void addSongInfo(@NonNull Song song, int playTimes, @Nullable String remark, @Nullable int[] sheets, boolean favorite) {

        if (remark == null) {
            remark = " ";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("0 "); //任何一首歌都在 全部歌曲 歌单中
        if (sheets != null && sheets.length > 0) {
            for (int i : sheets) {
                builder.append(i).append(" ");
            }
        }

        String path = song.path;
        String lpt = String.valueOf(System.currentTimeMillis()) + "";
        ContentValues values = new ContentValues();
        values.put(SONG_CREATE, lpt);
        values.put(SONG_LASTPLAYTIME, lpt);
        values.put(SONG_PATH, path);
        values.put(SONG_PLAYTIMES, playTimes);
        values.put(SONG_REMARK, remark);
        values.put(SONG_SHEETS, builder.toString());
        values.put(SONG_FAVORITE, favorite ? 1 : 0);

        database.insert(TABLE_SONG, null, values);
        Log.d(TAG, "addSongInfo: insert " + path);

    }

    public void addSongInfo(List<Song> songs) {
        if (songs != null && songs.size() > 0) {
            for (Song song : songs) {
                addSongInfo(song, 0, null, null, false);
            }
        }
    }

    /**
     * 更新歌曲最后播放时间
     */
    public void updateSongLastPlayTime(@NonNull Song song, long time) {
        ContentValues values = new ContentValues();
        values.put(SONG_LASTPLAYTIME, time + "");
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
    }

    public void updateSongLastPlayTime(int songID, long time) {
        ContentValues values = new ContentValues();
        values.put(SONG_LASTPLAYTIME, time + "");
        String whereClause = SONG_ID + " = ?";
        String[] whereArgs = {songID + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
    }

    public void updateSongLastPlayTime(int songID) {
        updateSongLastPlayTime(songID, System.currentTimeMillis());
    }

    public void updateSongLastPlayTime(@NonNull Song song) {
        updateSongLastPlayTime(song, System.currentTimeMillis());
    }

    /**
     * 更新歌曲播放次数
     */
    public void updateSongPlayTimes(@NonNull Song song, int times) {
        ContentValues values = new ContentValues();
        values.put(SONG_PLAYTIMES, times);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);

    }

    public void updateSongPlayTimes(int songID, int times) {
        ContentValues values = new ContentValues();
        values.put(SONG_PLAYTIMES, times);
        String whereClause = SONG_ID + " = ?";
        String[] whereArgs = {songID + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
    }

    public void updateSongPlayTimes(int songID) {
        SongInfo info = getSongInfo(songID);
        if (info == null) {
            return;
        }

        int times = info.playTimes + 1;
        updateSongPlayTimes(songID, times);
    }

    public void updateSongPlayTimes(@NonNull Song song) {
        SongInfo info = getSongInfo(song);
        if (info == null)
            return;

        int times = info.playTimes + 1;
        updateSongPlayTimes(song, times);
    }

    /**
     * 更新歌曲备注
     */
    public void updateSongRemark(@NonNull Song song, @NonNull String remark) {
        ContentValues values = new ContentValues();
        values.put(SONG_REMARK, remark);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
    }

    public void updateSongFavorite(@NonNull Song song, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(SONG_FAVORITE, favorite ? 1 : 0);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.d(TAG, "updateSongFavorite: " + song.path + " favorite:" + favorite);
    }

    /**
     * 歌曲的播放次数加一
     * 同时修改最后播放时间为当前时间
     */
    public void addSongPlayTimes(@NonNull Song song) {
        updateSongPlayTimes(song);
        updateSongLastPlayTime(song);

        SongInfo info = getSongInfo(song);
        if (info != null) {
            Log.d(TAG, "addSongPlayTimes: song=" + info.path + " lastPlayTime=" + info.lastPlayTime + " times=" + info.playTimes);
        }

    }


    public void addSheet(String name, String remark) {
        String create = System.currentTimeMillis() + "";
        if (remark == null)
            remark = "";

        String sql = String.format(Locale.CHINESE, "insert into %s values(null,'%s','%s','%s',%d)",
                TABLE_SHEET, name, remark, create, 0);
        database.execSQL(sql);
        Log.d(TAG, "addSheet: insert " + sql);
    }

    public int addSheetPlayTimes(int sheedId) {
        Sheet s = getSheet(sheedId);
        updateSheetPlayTimes(s.name, s.playTimes + 1);
        return s.playTimes + 1;
    }

    public int addSheetPlayTimes(String name) {
        Sheet s = getSheet(name);
        updateSheetPlayTimes(s.name, s.playTimes + 1);
        return s.playTimes + 1;
    }

    @Nullable
    public Sheet getSheet(String name) {
        String sql = "select * from " + TABLE_SHEET + " where " + SHEET_NAME + " like '" + name + "'";
        Cursor cursor = database.rawQuery(sql, null);

        Sheet sheet = null;
        while (cursor.moveToNext()) {
            sheet = new Sheet();
            sheet.id = cursor.getInt(cursor.getColumnIndex(SHEET_ID));
            sheet.name = cursor.getString(cursor.getColumnIndex(SHEET_NAME));
            sheet.remark = cursor.getString(cursor.getColumnIndex(SHEET_REMARK));
            String str = cursor.getString(cursor.getColumnIndex(SHEET_CREATE));
            sheet.create = Long.valueOf(str);
            sheet.playTimes = cursor.getInt(cursor.getColumnIndex(SHEET_PLAYTIMES));
        }

        cursor.close();
        return sheet;
    }

    @Nullable
    public Sheet getSheet(int sheetId) {
        String sql = "select * from " + TABLE_SHEET + " where " + SHEET_ID + " = " + sheetId;
        Cursor cursor = database.rawQuery(sql, null);

        Sheet sheet = null;
        while (cursor.moveToNext()) {
            sheet = new Sheet();
            sheet.id = cursor.getInt(cursor.getColumnIndex(SHEET_ID));
            sheet.name = cursor.getString(cursor.getColumnIndex(SHEET_NAME));
            sheet.remark = cursor.getString(cursor.getColumnIndex(SHEET_REMARK));
            String str = cursor.getString(cursor.getColumnIndex(SHEET_CREATE));
            sheet.create = Long.valueOf(str);
            sheet.playTimes = cursor.getInt(cursor.getColumnIndex(SHEET_PLAYTIMES));
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
            String str = cursor.getString(cursor.getColumnIndex(SHEET_CREATE));
            sheet.create = Long.valueOf(str);
            sheet.playTimes = cursor.getInt(cursor.getColumnIndex(SHEET_PLAYTIMES));
            sheets.add(sheet);
        }

        cursor.close();
        return sheets;
    }

    public void updateSheetPlayTimes(@NonNull String sheetName, int times) {
        ContentValues values = new ContentValues();
        values.put(SHEET_PLAYTIMES, times);
        String whereClause = SHEET_NAME + " like ?";
        String[] whereArgs = {sheetName};
        database.update(TABLE_SHEET, values, whereClause, whereArgs);
    }


    public void truncate(String table) {
        String sql = "drop table " + table;
        database.execSQL(sql);

        if (table.equals(TABLE_SHEET))
            createSheetTable(database);
        else if (table.equals(TABLE_SONG))
            createSongTable(database);

    }


    private TreeSet<DBMusicocoController.SongInfo> treeSet = new TreeSet<>(new Comparator<SongInfo>() {
        @Override
        public int compare(DBMusicocoController.SongInfo o1, DBMusicocoController.SongInfo o2) {
            int rs = 0;
            if (o1.lastPlayTime > o2.lastPlayTime) {
                rs = -1;
            } else if (o1.lastPlayTime < o2.lastPlayTime) {
                rs = 1;
            }
            return rs;
        }
    });

    /**
     * 按最后播放时间降序排序
     */
    public TreeSet<SongInfo> descSortByLastPlayTime(List<SongInfo> list) {
        treeSet.clear();
        for (DBMusicocoController.SongInfo s : list) {
            treeSet.add(s);
        }
        return treeSet;
    }

}
