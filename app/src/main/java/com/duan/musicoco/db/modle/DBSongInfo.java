package com.duan.musicoco.db.modle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class DBSongInfo {

    public int id;
    public String path;
    public long lastPlayTime;
    public int playTimes;
    public String remark;
    public long create;
    public int[] sheets;
    public boolean favorite;

    public DBSongInfo() {
    }

    public DBSongInfo(String path, long lastPlayTime, int playTimes, String remark, long create, int[] sheets, boolean favorite) {
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

        DBSongInfo info = (DBSongInfo) o;

        return path.equals(info.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * 按最后播放时间降序排序
     */
    public static List<DBSongInfo> descSortByLastPlayTime(List<DBSongInfo> list) {

        TreeSet<DBSongInfo> set = new TreeSet<>(new Comparator<DBSongInfo>() {
            @Override
            public int compare(DBSongInfo o1, DBSongInfo o2) {
                int rs;
                if (o1.lastPlayTime > o2.lastPlayTime) {
                    rs = -1;
                } else if (o1.lastPlayTime < o2.lastPlayTime) {
                    rs = 1;
                } else {
                    rs = -1; // equals set 不能重复，会丢失数据
                }
                return rs;
            }
        });

        for (DBSongInfo s : list) {
            set.add(s);
        }

        return new ArrayList<>(set);
    }

    /**
     * 按播放次数降序排序
     */
    public static ArrayList<DBSongInfo> descSortByPlayTimes(List<DBSongInfo> list) {

        TreeSet<DBSongInfo> set = new TreeSet<>(new Comparator<DBSongInfo>() {
            @Override
            public int compare(DBSongInfo o1, DBSongInfo o2) {
                int rs;
                if (o1.playTimes > o2.playTimes) {
                    rs = -1;
                } else if (o1.playTimes < o2.playTimes) {
                    rs = 1;
                } else {
                    rs = -1; // equals set 不能重复，会丢失数据
                }
                return rs;
            }
        });

        for (DBSongInfo s : list) {
            set.add(s);
        }

        return new ArrayList<>(set);
    }
}
