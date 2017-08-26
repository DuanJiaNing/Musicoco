package com.duan.musicoco.db.modle;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class Sheet {

    public int id;
    public String name;
    public String remark;
    public long create;
    public int playTimes;
    public int count;

    public Sheet() {
    }

    public Sheet(String name, String remark, int count) {
        this.name = name;
        this.remark = remark;
        this.count = count;
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
