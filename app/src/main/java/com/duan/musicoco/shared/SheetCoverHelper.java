package com.duan.musicoco.shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.util.MediaUtils;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/23.
 */

public class SheetCoverHelper {


    private Context context;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    public SheetCoverHelper(Context context, DBMusicocoController dbController, MediaManager mediaManager) {
        this.context = context;
        this.dbController = dbController;
        this.mediaManager = mediaManager;
    }

    public void findCoverForSheetAll(OnFindCompleted completed) {
        find(completed, MainSheetHelper.SHEET_ALL);
    }


    public void findCoverForSheetRecent(OnFindCompleted completed) {
        find(completed, MainSheetHelper.SHEET_RECENT);
    }


    public void findCoverForSheetFavorite(OnFindCompleted completed) {
        find(completed, MainSheetHelper.SHEET_FAVORITE);
    }

    public void findCoverForSheet(int sheetID, OnFindCompleted completed) {
        find(completed, sheetID);
    }

    public interface OnFindCompleted {
        void completed(@Nullable SongInfo info);
    }

    public void find(final OnFindCompleted completed, final int sheetID) {
        Observable.create(new Observable.OnSubscribe<SongInfo>() {
            @Override
            public void call(Subscriber<? super SongInfo> subscriber) {

                MainSheetHelper helper = new MainSheetHelper(context, dbController);
                SongInfo songInfo = null;
                if (sheetID < 0) {
                    switch (sheetID) {
                        case MainSheetHelper.SHEET_ALL: {
                            List<SongInfo> infos = MediaUtils.DBSongInfoToSongInfoList(context, helper.getAllSongInfo(), mediaManager);
                            songInfo = find(infos);
                            break;
                        }
                        case MainSheetHelper.SHEET_RECENT: {
                            List<SongInfo> infos = MediaUtils.DBSongInfoToSongInfoList(context, helper.getRecentSongInfo(), mediaManager);
                            songInfo = find(infos);
                            break;
                        }
                        case MainSheetHelper.SHEET_FAVORITE: {
                            List<SongInfo> infos = MediaUtils.DBSongInfoToSongInfoList(context, helper.getFavoriteSongInfo(), mediaManager);
                            songInfo = find(infos);
                            break;
                        }
                    }
                } else {
                    List<DBSongInfo> ss = dbController.getSongInfos(sheetID);
                    List<SongInfo> infos = MediaUtils.DBSongInfoToSongInfoList(context, ss, mediaManager);
                    songInfo = find(infos);
                }
                subscriber.onNext(songInfo);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SongInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        completed.completed(null);
                    }

                    @Override
                    public void onNext(SongInfo SongInfo) {
                        completed.completed(SongInfo);
                    }
                });
    }

    @Nullable
    public static SongInfo find(List<SongInfo> infos) {
        SongInfo si = null;
        for (int i = 0; i < infos.size(); i++) {
            si = infos.get(i);
            String path = si.getAlbum_path();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                break;
            }
        }
        return si;
    }

}
