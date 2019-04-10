package com.duan.musicoco.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.db.modle.SongSheetRela;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    public static final String SONG_CREATE = "create_time"; //创建时间
    public static final String SONG_FAVORITE = "song_favorite"; //是否收藏 0 否， 1 是

    public static final String TABLE_SHEET = "sheet";
    public static final String SHEET_ID = "_id"; // 主键
    public static final String SHEET_NAME = "name"; //歌单名称
    public static final String SHEET_REMARK = "remarks"; //歌单备注
    public static final String SHEET_CREATE = "create_time"; //创建时间
    public static final String SHEET_PLAYTIMES = "sheet_playtimes"; //播放次数
    public static final String SHEET_COUNT = "sheet_count"; //歌曲数目

    public static final String TABLE_SONG_SHEET_RELA = "song_sheet_rela";
    public static final String RELA_ID = "_id";
    public static final String RELA_SONG_ID = "song_id";
    public static final String RELA_SHEET_ID = "sheet_id";
    public static final String RELA_SORT = "sort";

    static void createSongTable(SQLiteDatabase db) {
        String sql = "create table if not exists " + DBMusicocoController.TABLE_SONG + "(" +
                DBMusicocoController.SONG_ID + " integer primary key autoincrement," +
                DBMusicocoController.SONG_PATH + " text unique," +
                DBMusicocoController.SONG_LASTPLAYTIME + " char(20)," +
                DBMusicocoController.SONG_PLAYTIMES + " integer," +
                DBMusicocoController.SONG_REMARK + " text," +
                DBMusicocoController.SONG_CREATE + " text," +
                DBMusicocoController.SONG_FAVORITE + " integer)";
        db.execSQL(sql);
    }

    static void createSongSheetRelaTable(SQLiteDatabase db) {
        String sql = "create table if not exists " + DBMusicocoController.TABLE_SONG_SHEET_RELA + "(" +
                DBMusicocoController.RELA_ID + " integer primary key autoincrement," +
                DBMusicocoController.RELA_SONG_ID + " integer, " +
                DBMusicocoController.RELA_SORT + " integer, " +
                DBMusicocoController.RELA_SHEET_ID + " integer)";
        db.execSQL(sql);
    }

    static void createSheetTable(SQLiteDatabase db) {

        String sql = "create table if not exists " + DBMusicocoController.TABLE_SHEET + "(" +
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

        ContentValues values = new ContentValues();
        values.put(RELA_SHEET_ID, sheetID);
        values.put(RELA_SONG_ID, info.id);
        values.put(RELA_SORT, getCurrentMaxSort(sheetID) + 1);

        database.insert(TABLE_SONG_SHEET_RELA, null, values);

        addSheetCount(sheetID);
        return true;
    }

    private int getCurrentMaxSort(int sheetID) {
        List<SongSheetRela> rela = getSongSheetRela(sheetID);
        int max = 0;

        if (rela.size() > 0) {
            for (SongSheetRela sheetRela : rela) {
                if (sheetRela.sort > max)
                    max = sheetRela.sort;
            }
        }

        return max;
    }

    private List<SongSheetRela> getSongSheetRela(int sheetId) {
        String sql = "select * from " + TABLE_SONG_SHEET_RELA + " where " + RELA_SHEET_ID + " = " + sheetId;
        Cursor cursor = database.rawQuery(sql, null);

        List<SongSheetRela> relas = new ArrayList<>();
        while (cursor.moveToNext()) {
            SongSheetRela rela = new SongSheetRela();

            rela.id = cursor.getInt(cursor.getColumnIndex(RELA_ID));
            rela.songId = cursor.getInt(cursor.getColumnIndex(RELA_SONG_ID));
            rela.sheetId = cursor.getInt(cursor.getColumnIndex(RELA_SHEET_ID));
            rela.sort = cursor.getInt(cursor.getColumnIndex(RELA_SORT));

            relas.add(rela);
        }
        cursor.close();

        return relas;
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

            infos.add(info);
        }

        cursor.close();
        return infos;
    }

    public List<DBSongInfo> getSongInfos(int sheetID) {

        String sql = "select * from " + TABLE_SONG_SHEET_RELA + " where " +
                RELA_SHEET_ID + "=" + sheetID + " order by " + RELA_SORT + " asc ";
        Cursor cursor = database.rawQuery(sql, null);

        List<DBSongInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            DBSongInfo info = new DBSongInfo();
            info.id = cursor.getInt(cursor.getColumnIndex(RELA_SONG_ID));
            info.sort = cursor.getInt(cursor.getColumnIndex(RELA_SORT));

            DBSongInfo songInfo = getSongInfo(info.id);
            if (songInfo != null) {
                info.path = songInfo.path;
                info.lastPlayTime = songInfo.lastPlayTime;
                info.playTimes = songInfo.playTimes;
                info.remark = songInfo.remark;
                info.create = songInfo.create;
                info.favorite = songInfo.favorite;
            }

            infos.add(info);
        }

        cursor.close();
        return infos;
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

                String path = song.path;
                String lpt = String.valueOf(System.currentTimeMillis()) + "";
                ContentValues values = new ContentValues();
                values.put(SONG_CREATE, lpt);
                values.put(SONG_LASTPLAYTIME, lpt);
                values.put(SONG_PATH, path);
                values.put(SONG_PLAYTIMES, 0);
                values.put(SONG_REMARK, "");
                values.put(SONG_FAVORITE, 0);

                database.insert(TABLE_SONG, null, values);

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
    }

    public void updateSongFavorite(@NonNull Song song, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(SONG_FAVORITE, favorite ? 1 : 0);
        String whereClause = SONG_PATH + " like ?";
        String[] whereArgs = {song.path};
        database.update(TABLE_SONG, values, whereClause, whereArgs);
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

        return count;
    }

    public void truncate(String table) {
        String sql = "drop table " + table;
        database.execSQL(sql);

        if (table.equals(TABLE_SHEET)) {
            createSheetTable(database);
        } else if (table.equals(TABLE_SONG)) {
            createSongTable(database);
        } else if (table.equals(TABLE_SONG_SHEET_RELA)) {
            createSongSheetRelaTable(database);
        }

    }

    //removeSongInfoFromBothTable
    public boolean removeSongInfo(Song song) {

        DBSongInfo songInfo = getSongInfo(song);
        if (songInfo != null && database.delete(TABLE_SONG,
                SONG_PATH + " = ?", new String[]{String.valueOf(song.path)}) == 1) {

            Set<Integer> ss = getSongSheetIds(songInfo.id);
            for (Integer s : ss) {
                minusSheetCount(s);
            }

            return database.delete(TABLE_SONG_SHEET_RELA,
                    RELA_SONG_ID + " = ?", new String[]{String.valueOf(songInfo.id)}) > 0;
        }

        return false;
    }

    private Set<Integer> getSongSheetIds(int songId) {
        String sql = "select * from " + TABLE_SONG_SHEET_RELA + " where " + RELA_SONG_ID + "=" + songId;
        Cursor cursor = database.rawQuery(sql, null);

        Set<Integer> ids = new HashSet<>();
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(cursor.getColumnIndex(RELA_SHEET_ID)));
        }

        cursor.close();
        return ids;
    }

    // 这将使歌曲信息从歌曲表中删除，只应在【彻底删除歌曲】时调用
    public boolean removeSongInfoFromSongTable(Song song) {

        String where = SONG_PATH + " like ? ";
        String[] whereArg = new String[]{song.path};
        database.delete(TABLE_SONG, where, whereArg);
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

        SongSheetRela rela = getSongSheetRela(info.id, sheetID);
        if (rela != null) {
            int eff = database.delete(TABLE_SONG_SHEET_RELA,
                    RELA_SONG_ID + "=? and " + RELA_SHEET_ID + "=?",
                    new String[]{String.valueOf(info.id), String.valueOf(sheetID)});

            if (eff == 1) {
                minusSheetCount(sheetID);
                recvExeAfterSort(rela.sort + 1, sheetID, -1);
                return true;
            }

        }

        return false;

    }

    // 歌单从 startSort 开始向后每一条记录都加 ops
    private void recvExeAfterSort(int startSort, int sheetId, int ops) {
        String sql = "select * from " + TABLE_SONG_SHEET_RELA + " where " +
                RELA_SHEET_ID + " = " + sheetId + " and sort >= " + startSort;
        Cursor cursor = database.rawQuery(sql, null);

        List<SongSheetRela> relas = new ArrayList<>();
        while (cursor.moveToNext()) {
            SongSheetRela rela = new SongSheetRela();
            rela.id = cursor.getInt(cursor.getColumnIndex(RELA_ID));
            rela.sort = cursor.getInt(cursor.getColumnIndex(RELA_SORT));
            relas.add(rela);
        }
        cursor.close();

        if (relas.size() > 0) {
            for (SongSheetRela rela : relas) {
                ContentValues vs = new ContentValues();
                vs.put(RELA_SORT, rela.sort + ops);
                database.update(TABLE_SONG_SHEET_RELA, vs,
                        RELA_ID + "=?", new String[]{String.valueOf(rela.id)});
            }
        }

    }

    private SongSheetRela getSongSheetRela(int songId, int sheetId) {
        String sql = "select * from " + TABLE_SONG_SHEET_RELA + " where " +
                RELA_SONG_ID + " = " + songId + " and " + RELA_SHEET_ID + " = " + sheetId;
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            SongSheetRela rela = new SongSheetRela();

            rela.id = cursor.getInt(cursor.getColumnIndex(RELA_ID));
            rela.songId = cursor.getInt(cursor.getColumnIndex(RELA_SONG_ID));
            rela.sheetId = cursor.getInt(cursor.getColumnIndex(RELA_SHEET_ID));
            rela.sort = cursor.getInt(cursor.getColumnIndex(RELA_SORT));
            cursor.close();

            return rela;
        }

        return null;
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
        return null;
    }

    public boolean removeSheet(int sheetID) {

        if (database.delete(TABLE_SHEET,
                SHEET_ID + " = ?", new String[]{String.valueOf(sheetID)}) == 1) {

            return database.delete(TABLE_SONG_SHEET_RELA,
                    RELA_SHEET_ID + " = ?", new String[]{String.valueOf(sheetID)}) > 0;
        }

        return false;
    }

    public DBSongInfo getSongInfo(String data) {
        return getSongInfo(new Song(data));
    }

    public List<Sheet> getSongSheets(int songId) {
        Set<Integer> ids = getSongSheetIds(songId);
        if (ids.size() > 0) {
            List<Sheet> sheets = new ArrayList<>();
            for (Integer id : ids) {
                sheets.add(getSheet(id));
            }

            return sheets;
        }

        return null;
    }

    public void updateSheetSort(List<SongSheetRela> relas) {
        for (SongSheetRela rela : relas) {
            ContentValues vs = new ContentValues();
            vs.put(RELA_SORT, rela.sort);
            database.update(TABLE_SONG_SHEET_RELA, vs,
                    RELA_SHEET_ID + "=? and " + RELA_SONG_ID + "=?",
                    new String[]{String.valueOf(rela.sheetId), String.valueOf(rela.songId)});
        }

    }
}
