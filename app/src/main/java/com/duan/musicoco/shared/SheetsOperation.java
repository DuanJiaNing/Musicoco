package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.util.ToastUtils;

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

}
