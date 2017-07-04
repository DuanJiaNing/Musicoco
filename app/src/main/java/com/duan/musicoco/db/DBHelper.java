package com.duan.musicoco.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/6/30.
 * 数据库将创建于 data/com.duan.musicoco/databases/ 下
 */

public class DBHelper extends SQLiteOpenHelper {

    private volatile static DBHelper DB_HELPER;

    static DBHelper getInstance(Context context, String dbName) {
        if (DB_HELPER == null) {
            synchronized (DBHelper.class) {
                if (DB_HELPER == null) {
                    //String path = context.getFilesDir().getAbsolutePath() + File.separator + dbName;
                    DB_HELPER = new DBHelper(context, dbName);
                }
            }
        }
        return DB_HELPER;
    }

    private DBHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initTableForDBMusicoco(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void initTableForDBMusicoco(SQLiteDatabase db) {
        DBMusicocoController.createSheetTable(db);
        DBMusicocoController.createSongTable(db);
    }

}
