package com.duan.musicoco.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public static final String SHEET_COUNT = "sheet_count"; //歌曲数目

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
                DBMusicocoController.SHEET_PLAYTIMES + " integer," +
                DBMusicocoController.SHEET_COUNT + " integer)";
        db.execSQL(sql);
    }

    /**
     * 在使用结束时应调用{@link #close()}关闭数据库连接
     */
    public DBMusicocoController(Context context, boolean writable) {
        DBHelper helper = new DBHelper(context, DATABASE);
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

    public boolean addSongToSheet(int sheetID, Song song) {
        DBSongInfo info = getSongInfo(song);
        if (info == null) {
            return false;
        }

        int[] sheets = info.sheets;
        for (int i : sheets) {
            if (sheetID == i) {
                return false;
            }
        }

        String ss = songSheetsIntArrayToString(sheets) + sheetID + " ";

        ContentValues values = new ContentValues();
        values.put(SONG_SHEETS, ss);
        String whereClause = SONG_ID + " = ?";
        String[] whereArgs = {info.id + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);

        addSheetCount(sheetID);
        Log.i(TAG, "addSongToSheet: " + song.path + " sheet:" + sheetID);
        return true;
    }

    private int addSheetCount(int sheetID) {
        Sheet sheet = getSheet(sheetID);
        if (sheet != null) {
            return updateSheetCount(sheetID, sheet.count + 1);
        } else {
            return -1;
        }
    }

    // 歌单中歌曲总数减一
    private int minusSheetCount(int sheetID) {
        Sheet sheet = getSheet(sheetID);
        if (sheet != null) {
            return updateSheetCount(sheetID, sheet.count - 1);
        } else {
            return -1;
        }
    }

    public boolean addSongToSheet(String sheetName, Song song) {
        Sheet sheet = getSheet(sheetName);
        if (sheet == null) {
            return false;
        }

        return addSongToSheet(sheet.id, song);
    }

    @Nullable
    public DBSongInfo getSongInfo(int songId) {
        String sql = "select * from " + TABLE_SONG + " where " + SONG_ID + " = " + songId;
        Cursor cursor = database.rawQuery(sql, null);

        DBSongInfo info = new DBSongInfo();
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
            info.sheets = songSheetsStringToIntArray(sh);

        }

        cursor.close();
        return info;
    }

    @Nullable
    public DBSongInfo getSongInfo(@NonNull Song song) {
        String sql = "select * from " + TABLE_SONG + " where " + SONG_PATH + " like ? ";
        // 歌曲名中有 ' 符号的会导致 SQL 语句解析出错，所以用 rawQuery 第二个参数解决
        Cursor cursor = database.rawQuery(sql, new String[]{song.path});

        DBSongInfo info = null;
        while (cursor.moveToNext()) {
            info = new DBSongInfo();

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
            info.sheets = songSheetsStringToIntArray(sh);
        }

        cursor.close();
        return info;
    }

    public List<DBSongInfo> getSongInfos() {
        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);

        List<DBSongInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            DBSongInfo info = new DBSongInfo();
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
            info.sheets = songSheetsStringToIntArray(sh);

            infos.add(info);
        }

        cursor.close();
        return infos;
    }

    public List<DBSongInfo> getSongInfos(int sheetID) {

        String sql = "select * from " + TABLE_SONG;
        Cursor cursor = database.rawQuery(sql, null);

        List<DBSongInfo> infos = new ArrayList<>();

        while (cursor.moveToNext()) {
            DBSongInfo info = new DBSongInfo();

            String sh = cursor.getString(cursor.getColumnIndex(SONG_SHEETS));
            int[] shs = songSheetsStringToIntArray(sh);

            boolean isContain = false;
            for (int i : shs) {
                if (i == sheetID) {
                    isContain = true;
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

        String ss = songSheetsIntArrayToString(sheets);

        String path = song.path;
        String lpt = String.valueOf(System.currentTimeMillis()) + "";
        ContentValues values = new ContentValues();
        values.put(SONG_CREATE, lpt);
        values.put(SONG_LASTPLAYTIME, lpt);
        values.put(SONG_PATH, path);
        values.put(SONG_PLAYTIMES, playTimes);
        values.put(SONG_REMARK, remark);
        values.put(SONG_SHEETS, ss);
        values.put(SONG_FAVORITE, favorite ? 1 : 0);

        database.insert(TABLE_SONG, null, values);
        Log.i(TAG, "addSongInfo: " + song.path);

    }

    @NonNull
    private String songSheetsIntArrayToString(int[] sheets) {

        StringBuilder builder = new StringBuilder();
        builder.append("");
        if (sheets != null && sheets.length > 0) {
            for (int i : sheets) {
                builder.append(i).append(" ");
            }
        }
        return builder.toString();
    }

    private int[] songSheetsStringToIntArray(String sheets) {
        if (TextUtils.isEmpty(sheets)) {
            return new int[]{};
        }
        String[] strs = sheets.split(" ");
        int[] shs = new int[strs.length];
        for (int i = 0; i < shs.length; i++) {
            shs[i] = Integer.parseInt(strs[i]);
        }
        return shs;
    }

    public void addSongInfo(List<Song> songs) {
        if (songs != null && songs.size() > 0) {
            database.beginTransaction();

            for (Song song : songs) {
                addSongInfo(song, 0, null, null, false);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void updateSongLastPlayTime(@NonNull Song song, long time) {
        ContentValues values = new ContentValues();
        values.put(SONG_LASTPLAYTIME, time + "");
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongLastPlayTime: " + song.path + " time:" + StringUtils.getGenDateYMDHMS(time));
    }

    public void updateSongLastPlayTime(int songID, long time) {
        ContentValues values = new ContentValues();
        values.put(SONG_LASTPLAYTIME, time + "");
        String whereClause = SONG_ID + " = ?";
        String[] whereArgs = {songID + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongLastPlayTime: id:" + songID + " time:" + StringUtils.getGenDateYMDHMS(time));
    }

    public void updateSongLastPlayTime(int songID) {
        updateSongLastPlayTime(songID, System.currentTimeMillis());
    }

    public void updateSongLastPlayTime(@NonNull Song song) {
        updateSongLastPlayTime(song, System.currentTimeMillis());
    }

    public void updateSongPlayTimes(@NonNull Song song, int times) {
        ContentValues values = new ContentValues();
        values.put(SONG_PLAYTIMES, times);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongPlayTimes: " + song.path + " times:" + times);

    }

    public void updateSongPlayTimes(int songID, int times) {
        ContentValues values = new ContentValues();
        values.put(SONG_PLAYTIMES, times);
        String whereClause = SONG_ID + " = ?";
        String[] whereArgs = {songID + ""};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongPlayTimes: id " + songID + " times:" + times);
    }

    public void updateSongPlayTimes(int songID) {
        DBSongInfo info = getSongInfo(songID);
        if (info == null) {
            return;
        }

        int times = info.playTimes + 1;
        updateSongPlayTimes(songID, times);
    }

    public void updateSongPlayTimes(@NonNull Song song) {
        DBSongInfo info = getSongInfo(song);
        if (info == null)
            return;

        int times = info.playTimes + 1;
        updateSongPlayTimes(song, times);
    }

    public void updateSongRemark(@NonNull Song song, @NonNull String remark) {
        ContentValues values = new ContentValues();
        values.put(SONG_REMARK, remark);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongRemark: " + song.path + " remark:" + remark);
    }

    public void updateSongFavorite(@NonNull Song song, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(SONG_FAVORITE, favorite ? 1 : 0);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongFavorite: " + song.path + " favorite:" + favorite);
    }

    public void updateSongSheet(Song song, int[] sheets) {
        String ss = songSheetsIntArrayToString(sheets);
        ContentValues values = new ContentValues();
        values.put(SONG_SHEETS, ss);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
        Log.i(TAG, "updateSongSheet: " + song.path + " sheets:" + ss);
    }

    /**
     * 歌曲的播放次数加一
     * 同时修改最后播放时间为当前时间
     */
    public void addSongPlayTimes(@NonNull Song song) {
        updateSongPlayTimes(song);
        updateSongLastPlayTime(song);
    }

    public String addSheet(String name, String remark, int count) {

        Sheet sheet = getSheet(name);
        if (sheet != null && sheet.name.equals(name)) {
            return context.getString(R.string.error_sheet_already_exits);
        }

        String create = System.currentTimeMillis() + "";
        if (remark == null) {
            remark = "";
        }

        String sql = String.format(Locale.CHINESE, "insert into %s values(null,'%s','%s','%s',%d,%d)",
                TABLE_SHEET, name, remark, create, 0, count);
        database.execSQL(sql);
        Log.d(TAG, "addSheet: " + name);

        return null;
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
            sheet.count = cursor.getInt(cursor.getColumnIndex(SHEET_COUNT));
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
            sheet.count = cursor.getInt(cursor.getColumnIndex(SHEET_COUNT));
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
            sheet.count = cursor.getInt(cursor.getColumnIndex(SHEET_COUNT));

            sheets.add(sheet);
        }

        cursor.close();
        return sheets;
    }

    public void updateSheetPlayTimes(@NonNull String sheetName, int times) {
        if (times < 0) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(SHEET_PLAYTIMES, times);
        String whereClause = SHEET_NAME + " like ?";
        String[] whereArgs = {sheetName};
        database.update(TABLE_SHEET, values, whereClause, whereArgs);
        Log.i(TAG, "updateSheetPlayTimes: " + sheetName + " times:" + times);
    }

    public int updateSheetCount(@NonNull String sheetName, int count) {
        if (count < 0) {
            return count;
        }
        ContentValues values = new ContentValues();
        values.put(SHEET_COUNT, count);
        String whereClause = SHEET_NAME + " like ?";
        String[] whereArgs = {sheetName};
        database.update(TABLE_SHEET, values, whereClause, whereArgs);
        Log.i(TAG, "updateSheetCount: " + sheetName + " count:" + count);
        return count;
    }

    public int updateSheetCount(int sheetID, int count) {
        if (count < 0) {
            return count;
        }
        ContentValues values = new ContentValues();
        values.put(SHEET_COUNT, count);
        String whereClause = SHEET_ID + " = ?";
        String[] whereArgs = {String.valueOf(sheetID)};
        database.update(TABLE_SHEET, values, whereClause, whereArgs);
        Log.i(TAG, "updateSheetCount: id:" + sheetID + " count:" + count);

        return count;
    }

    public void truncate(String table) {
        String sql = "drop table " + table;
        database.execSQL(sql);

        if (table.equals(TABLE_SHEET)) {
            createSheetTable(database);
        } else if (table.equals(TABLE_SONG)) {
            createSongTable(database);
        }
        Log.i(TAG, "truncate: table:" + table);

    }

    //removeSongInfoFromBothTable
    public boolean removeSongInfo(Song song) {
        DBSongInfo info = getSongInfo(song);
        boolean r = false;

        if (info != null) {
            database.beginTransaction();

            int[] sheets = info.sheets;
            for (int s : sheets) {
                minusSheetCount(s);
            }
            r = removeSongInfoFromSongTable(song);

            database.setTransactionSuccessful();
            database.endTransaction();
        }
        Log.i(TAG, "removeSongInfo: " + song.path);
        return r;
    }

    // 这将使歌曲信息从歌曲表中删除，只应在【彻底删除歌曲】时调用
    public boolean removeSongInfoFromSongTable(Song song) {

        String where = SONG_PATH + " like ? ";
        String[] whereArg = new String[]{song.path};
        database.delete(TABLE_SONG, where, whereArg);
        Log.i(TAG, "removeSongInfoFromSongTable: " + song.path);
        return true;
    }

    //从歌单中移除该歌曲
    // 1 将此歌单的 id 从歌曲的歌单字段中移除
    // 2 让歌单的歌曲数字段减一
    public boolean removeSongInfoFromSheet(Song song, int sheetID) {

        DBSongInfo info = getSongInfo(song);
        if (info == null) {
            return false;
        }

        Sheet sheet = getSheet(sheetID);
        if (sheet == null) {
            return false;
        }

        int[] sheets = info.sheets;
        int index = -1;
        for (int j = 0; j < sheets.length; j++) {
            if (sheetID == sheets[j]) {
                index = j;
                break;
            }
        }

        if (-1 == index) {
            Log.e(TAG, "removeSongInfoFromSheet: the sheet not contain the song " + song.path + " sheet:" + sheetID);
            return true;
        } else {
            int i = 0;
            int[] newSheets = new int[sheets.length - 1];
            for (int j = 0; j < sheets.length; j++) {
                if (index != j) {
                    newSheets[i] = sheets[j];
                    i++;
                }
            }

            updateSongSheet(song, newSheets);
            minusSheetCount(sheetID);
            Log.i(TAG, "removeSongInfoFromSheet:  " + song.path + " sheet:" + sheet.name);
            return true;
        }
    }

    public boolean removeSongInfoFromSheet(Song song, String sheetName) {

        DBSongInfo info = getSongInfo(song);
        if (info == null) {
            return false;
        }

        Sheet sheet = getSheet(sheetName);
        if (sheet != null) {
            return removeSongInfoFromSheet(song, sheet.id);
        } else {
            return false;
        }
    }

    public String updateSheet(int sheetID, String newName, String newRemark) {

        if (TextUtils.isEmpty(newName)) {
            return context.getString(R.string.error_name_required);
        }

        Sheet sheet = getSheet(sheetID);
        if (sheet == null) {
            return context.getString(R.string.error_non_sheet_existent);
        }

        List<Sheet> list = getSheets();
        for (Sheet s : list) {
            if (s.id != sheetID && s.name.equals(newName)) {
                return context.getString(R.string.error_sheet_already_exits);
            }
        }

        newRemark = TextUtils.isEmpty(newRemark) ? "" : newRemark;

        ContentValues values = new ContentValues();
        values.put(SHEET_NAME, newName);
        values.put(SHEET_REMARK, newRemark);
        String whereClause = SHEET_ID + " = ?";
        String[] whereArgs = {sheetID + ""};
        database.update(TABLE_SHEET, values, whereClause, whereArgs);
        Log.i(TAG, "updateSheet: " + sheet.name + " " + newName + " " + newRemark);
        return null;
    }

    public boolean removeSheet(int sheetID) {

        Sheet sheet = getSheet(sheetID);
        if (sheet != null) {
            database.beginTransaction();

            List<DBSongInfo> infos = getSongInfos();
            Song song = new Song("");
            for (DBSongInfo d : infos) {
                int[] ss = d.sheets;
                for (int i : ss) {
                    if (sheetID == i) {
                        song.path = d.path;
                        removeSongInfoFromSheet(song, sheetID);
                    }
                }
            }

            removeSheetFromSheetTableOnly(sheetID);

            database.setTransactionSuccessful();
            database.endTransaction();
            return true;
        } else {
            return false;
        }
    }

    public void removeSheetFromSheetTableOnly(int sheetID) {
        Sheet sheet = getSheet(sheetID);
        if (sheet == null) {
            return;
        }

        String where = SHEET_ID + " = ?";
        String[] whereArg = new String[]{String.valueOf(sheetID)};
        database.delete(TABLE_SHEET, where, whereArg);

        Log.i(TAG, "removeSheetFromSheetTableOnly: " + sheet.name);
    }

    public DBSongInfo getSongInfo(String data) {
        return getSongInfo(new Song(data));
    }
}
