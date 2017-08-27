package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.OnCompleteListener;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.util.Utils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/21.
 */

public class SheetOperation {

    public static final String DELETE_SHEET_ID = "deletel_sheet_id";
    public static final String PLAY_SHEET_RANDOM = "play_sheet_random";

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private BroadcastManager broadcastManager;

    public SheetOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.broadcastManager = BroadcastManager.getInstance();

    }

    public void addSheet() {
        ActivityManager manager = ActivityManager.getInstance();
        manager.startSheetModifyActivity(activity, Integer.MAX_VALUE);
    }

    public void modifySheet(Sheet sheet) {
        ActivityManager manager = ActivityManager.getInstance();
        manager.startSheetModifyActivity(activity, sheet.id);
    }

    public void handleDeleteSheet(final Sheet sheet, final OnCompleteListener<Boolean> onCompleteListener) {
        DialogProvider provider = new DialogProvider(activity);
        final Dialog dialog = provider.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.info_delete_confirm),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSheet(sheet, onCompleteListener);
                    }
                },
                null,
                true
        );
        dialog.show();
    }

    private void deleteSheet(final Sheet sheet, final OnCompleteListener<Boolean> onCompleteListener) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                boolean res = dbMusicoco.removeSheet(sheet.id);

                if (sheet.count != 0) {
                    Utils.pretendToRun(300);
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
                        callBack(false);
                    }

                    @Override
                    public void onNext(Boolean s) {
                        callBack(s);
                    }

                    private void callBack(boolean success) {
                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete(success);
                        }

                        // 发送广播让 MainActivity mySheetDataChangedReceiver 进行歌单信息更新
                        broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_MY_SHEET_UPDATE, null);
                    }
                });
    }

}
