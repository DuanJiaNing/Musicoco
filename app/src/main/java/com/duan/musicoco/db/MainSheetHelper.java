package com.duan.musicoco.db;

import android.content.Context;

import com.duan.musicoco.R;
import com.duan.musicoco.db.modle.DBSongInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class MainSheetHelper {

    public static final int SHEET_ALL = -10;
    public static final int SHEET_RECENT = -20;
    public static final int SHEET_FAVORITE = -30;

    private List<DBSongInfo> all;
    private Context context;
    private DBMusicocoController dbController;
    private final int recentCount;

    public MainSheetHelper(Context context, DBMusicocoController dbController) {
        this.recentCount = context.getResources().getInteger(R.integer.sheet_recent_count);
        this.dbController = dbController;
        refreshData();

    }

    private void refreshData() {
        all = dbController.getSongInfos();
    }

    public static String getMainSheetName(Context context, int which) {
        switch (which) {
            case SHEET_RECENT:
                return context.getString(R.string.main_sheet_recent);
            case SHEET_FAVORITE:
                return context.getString(R.string.main_sheet_favorite);
            case SHEET_ALL:
            default:
                return context.getString(R.string.main_sheet_all);
        }
    }

    public List<DBSongInfo> getMainSheetSongInfo(int which) {
        switch (which) {
            case SHEET_RECENT:
                return getRecentSongInfo();
            case SHEET_FAVORITE:
                return getFavoriteSongInfo();
            case SHEET_ALL:
            default:
                return getAllSongInfo();
        }
    }

    public List<DBSongInfo> getAllSongInfo() {
        refreshData();
        return all;
    }

    public List<DBSongInfo> getRecentSongInfo() {
        refreshData();
        List<DBSongInfo> recent = DBSongInfo.descSortByLastPlayTime(all);
        return recent.subList(0, recentCount >= all.size() ? all.size() : recentCount);
    }

    public List<DBSongInfo> getFavoriteSongInfo() {
        refreshData();
        List<DBSongInfo> favorite = new ArrayList<>();
        for (DBSongInfo i : all) {
            if (i.favorite) {
                favorite.add(i);
            }
        }
        return favorite;
    }

}
