package com.duan.musicoco.service;

import android.content.Context;

import com.duan.musicoco.aidl.PlayControlImpl;
import com.duan.musicoco.aidl.Song;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayServiceIBinder extends PlayControlImpl {

    private Context mContext;

    public PlayServiceIBinder(Context context, List<Song> songs) {
        super(songs);
        this.mContext = context;
    }
}
