package com.duan.musicoco.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DuanJiaNing on 2017/6/30.
 * 数据库将创建于 data/com.duan.musicoco/databases/ 下
 */

public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context, String name) {
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
