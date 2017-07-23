package com.duan.musicoco.shared;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.SongUtils;

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

    private int requestHeight;
    private int requestWidth;

    public SheetCoverHelper(Context context, DBMusicocoController dbController, MediaManager mediaManager, int requestHeight, int requestWidth) {
        this.context = context;
        this.dbController = dbController;
        this.mediaManager = mediaManager;
        this.requestHeight = requestHeight;
        this.requestWidth = requestWidth;
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
        void completed(Bitmap bitmap);
    }

    public void find(final OnFindCompleted completed, final int sheetID) {
        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {

                MainSheetHelper helper = new MainSheetHelper(context, dbController);
                Bitmap bitmap = null;
                if (sheetID < 0) {
                    switch (sheetID) {
                        case MainSheetHelper.SHEET_ALL: {
                            List<SongInfo> infos = SongUtils.DBSongInfoToSongInfoList(helper.getAllSongInfo(), mediaManager);
                            bitmap = find(infos);
                            if (bitmap == null) {
                                bitmap = BitmapUtils.getDefaultPictureForAllSheet(context, requestWidth, requestHeight);
                            }
                            break;
                        }
                        case MainSheetHelper.SHEET_RECENT: {
                            List<SongInfo> infos = SongUtils.DBSongInfoToSongInfoList(helper.getRecentSongInfo(), mediaManager);
                            bitmap = find(infos);
                            if (bitmap == null) {
                                bitmap = BitmapUtils.getDefaultPictureForRecentSheet(context, requestWidth, requestHeight);
                            }
                            break;
                        }
                        case MainSheetHelper.SHEET_FAVORITE: {
                            List<SongInfo> infos = SongUtils.DBSongInfoToSongInfoList(helper.getFavoriteSongInfo(), mediaManager);
                            bitmap = find(infos);
                            if (bitmap == null) {
                                bitmap = BitmapUtils.getDefaultPictureForFavoriteSheet(context, requestWidth, requestHeight);
                            }
                            break;
                        }
                    }
                } else {
                    List<DBSongInfo> is = dbController.getSongInfos(sheetID);
                    List<SongInfo> infos = SongUtils.DBSongInfoToSongInfoList(is, mediaManager);
                    bitmap = find(infos);
                    if (bitmap == null) {
                        bitmap = BitmapUtils.getDefaultPictureForAlbum(context, requestWidth, requestHeight);
                    }
                }
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        completed.completed(null);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        completed.completed(bitmap);
                    }
                });
    }

    @Nullable
    private Bitmap find(List<SongInfo> infos) {
        Bitmap bitmap = null;
        for (int i = 0; i < infos.size(); i++) {
            SongInfo info = infos.get(i);
            bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), requestWidth, requestHeight);
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

}
