package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.util.SongUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.TextInputHelper;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/21.
 */

public class SheetsOperation {

    public static final String DELETE_SHEET_ID = "deletel_sheet_id";
    public static final String PLAY_SHEET_RANDOM = "play_sheet_random";

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private BroadcastManager broadcastManager;

    public SheetsOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.broadcastManager = BroadcastManager.getInstance(activity);

    }

    public void handleAddSheet() {
        ActivityManager manager = ActivityManager.getInstance(activity);
        manager.startSheetModifyActivity(Integer.MAX_VALUE);
    }

    public void handleModifySheet(Sheet sheet) {
        ActivityManager manager = ActivityManager.getInstance(activity);
        manager.startSheetModifyActivity(sheet.id);
    }

    public void deleteSheet(final Sheet sheet) {
        DialogProvider provider = new DialogProvider(activity);
        final Dialog dialog = provider.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.delete_confirm),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleDelete(sheet);
                    }
                },
                null,
                true
        );
        dialog.show();
    }

    private void handleDelete(final Sheet sheet) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                boolean res = dbMusicoco.removeSheet(sheet.id);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                subscriber.onNext(res);
                subscriber.onCompleted();
            }
        };

        DialogProvider provider = new DialogProvider(activity);
        final Dialog dialog = provider.createProgressDialog(activity.getString(R.string.deleting));
        dialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        dialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg);
                        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
                    }

                    @Override
                    public void onNext(Boolean s) {
                        if (s) {
                            String msg = activity.getString(R.string.success_delete_sheet) + " [" + sheet.name + "]";
                            ToastUtils.showShortToast(msg);
                            sendBroadcast(sheet);
                        } else {
                            String msg = activity.getString(R.string.error_delete_sheet_fail);
                            ToastUtils.showShortToast(msg);
                            broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
                        }
                    }
                });
    }

    private void sendBroadcast(Sheet sheet) {
        Bundle extras = new Bundle();
        extras.putInt(DELETE_SHEET_ID, sheet.id);
        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, extras);
    }

    public void handleAddAllSongToFavorite(final int sheetID) {
        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(activity.getString(R.string.tip),
                activity.getString(R.string.add_all_songs_to_favorite),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addAllSongToFavorite(sheetID);
                    }
                },
                null,
                true);
        promptDialog.show();

    }

    public void addAllSongToFavorite(final int sheetID) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                List<Song> songs = new ArrayList<>();
                if (sheetID < 0 && sheetID != MainSheetHelper.SHEET_FAVORITE) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbMusicoco);
                    List<DBSongInfo> info = helper.getMainSheetSongInfo(sheetID);
                    songs = SongUtils.DBSongInfoListToSongList(info);
                } else {
                    List<DBSongInfo> infos = dbMusicoco.getSongInfos(sheetID);
                    songs = SongUtils.DBSongInfoListToSongList(infos);
                }

                for (Song song : songs) {
                    DBSongInfo info = dbMusicoco.getSongInfo(song);
                    if (info != null && !info.favorite) {
                        dbMusicoco.updateSongFavorite(song, true);
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                subscriber.onCompleted();
            }
        };

        final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(activity.getString(R.string.add_songs_to_favorite));
        progressDialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.success_add_all_song_to_favorite);
                        ToastUtils.showShortToast(msg);
                        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_SHEET_DETAIL_SONGS_CHANGE, null);
                        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MAIN_SHEET_CHANGED, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg);
                        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_SHEET_DETAIL_SONGS_CHANGE, null);
                        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MAIN_SHEET_CHANGED, null);
                    }

                    @Override
                    public void onNext(Boolean s) {
                    }
                });
    }
}
