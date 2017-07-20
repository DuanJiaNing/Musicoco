package com.duan.musicoco.db;

import android.content.Context;

import com.duan.musicoco.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class MainSheetHelper {

    public static final int SHEET_ALL = -10;
    public static final int SHEET_RECENT = -20;
    public static final int SHEET_FAVORITE = -30;

    private final List<DBSongInfo> all;
    private final int recentCount;

    public MainSheetHelper(Context context, DBMusicocoController dbController) {
        all = dbController.getSongInfos();
        recentCount = context.getResources().getInteger(R.integer.sheet_recent_count);

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
        return all;
    }

    public List<DBSongInfo> getRecentSongInfo() {
        List<DBSongInfo> recent = DBSongInfo.descSortByLastPlayTime(all);
        return recent.subList(0, recentCount);
    }

    public List<DBSongInfo> getFavoriteSongInfo() {

        List<DBSongInfo> favorite = new ArrayList<>();
        for (DBSongInfo i : all) {
            if (i.favorite) {
                favorite.add(i);
            }
        }
        return favorite;
    }

}
