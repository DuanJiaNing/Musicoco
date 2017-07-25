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
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.TextInputHelper;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/21.
 */

public class MySheetsOperation {

    public static final String DELETE_SHEET_ID = "deletel_sheet_id";
    public static final String PLAY_SHEET_RANDOM = "play_sheet_random";

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private BroadcastManager broadcastManager;

    public MySheetsOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.broadcastManager = BroadcastManager.getInstance(activity);

    }

    public void handleAddSheet() {
        DialogProvider manager = new DialogProvider(activity);
        TextInputHelper inputHelper = new TextInputHelper(activity);

        String newSheet = activity.getString(R.string.new_sheet);
        String inputName = activity.getString(R.string.sheet_name);
        String inputRemark = activity.getString(R.string.sheet_remark);
        String countOutLimit = activity.getString(R.string.error_text_count_out_of_limit);
        String inputMessage = activity.getString(R.string.new_sheet_input_message);
        int nameLimit = activity.getResources().getInteger(R.integer.sheet_name_text_limit);
        int remarkLimit = activity.getResources().getInteger(R.integer.sheet_remark_text_limit);

        final TextInputHelper.ViewHolder nameHolder = inputHelper.getLimitedTexInputLayoutView(
                inputName,
                nameLimit,
                countOutLimit, ""
        );
        nameHolder.editText.setLines(1);

        final TextInputHelper.ViewHolder remarkHolder = inputHelper.getLimitedTexInputLayoutView(
                inputRemark,
                remarkLimit,
                countOutLimit, ""
        );
        int remarkInputMaxLine = activity.getResources().getInteger(R.integer.sheet_remark_input_max_lines);
        remarkHolder.editText.setMaxLines(remarkInputMaxLine);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(nameHolder.view);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) remarkHolder.textInputLayout.getLayoutParams();
        param.topMargin = 50;
        remarkHolder.textInputLayout.setLayoutParams(param);

        ll.addView(remarkHolder.view);
        final AlertDialog dialog = manager.createCustomInsiderDialog(
                newSheet,
                inputMessage,
                ll
        );

        manager.setOnPositiveButtonListener(activity.getString(R.string.ensure), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameHolder.editText.getText().toString();
                String error = null;

                if (TextUtils.isEmpty(name)) {
                    error = activity.getString(R.string.error_name_required);
                } else {
                    String remark = remarkHolder.editText.getText().toString();
                    remark = TextUtils.isEmpty(remark) ? "" : remark;

                    if (nameHolder.textInputLayout.isErrorEnabled()) {
                        TextInputHelper.textInputErrorTwinkle(nameHolder.textInputLayout, "!");
                        return;
                    } else {
                        String res = dbMusicoco.addSheet(name, remark, 0);
                        if (res != null) {
                            error = res;
                        }
                    }
                }

                if (error != null) {
                    nameHolder.textInputLayout.setError(error);
                    nameHolder.textInputLayout.setErrorEnabled(true);
                } else {
                    String msg = activity.getString(R.string.success_create_sheet) + " [" + name + "]";
                    ToastUtils.showShortToast(msg);
                    broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
                    dialog.dismiss();
                }
            }
        });

        manager.setOnNegativeButtonListener(activity.getString(R.string.cancel), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void handleModifySheet(final Sheet sheet) {
        DialogProvider manager = new DialogProvider(activity);
        TextInputHelper inputHelper = new TextInputHelper(activity);

        String newSheet = activity.getString(R.string.new_sheet);
        String inputName = activity.getString(R.string.sheet_name);
        String inputRemark = activity.getString(R.string.sheet_remark);
        String countOutLimit = activity.getString(R.string.error_text_count_out_of_limit);
        int nameLimit = activity.getResources().getInteger(R.integer.sheet_name_text_limit);
        int remarkLimit = activity.getResources().getInteger(R.integer.sheet_remark_text_limit);

        final String oldName = sheet.name;
        final String oldRemark = sheet.remark;

        final TextInputHelper.ViewHolder nameHolder = inputHelper.getLimitedTexInputLayoutView(
                inputName,
                nameLimit,
                countOutLimit, oldName
        );
        nameHolder.editText.setLines(1);

        final TextInputHelper.ViewHolder remarkHolder = inputHelper.getLimitedTexInputLayoutView(
                inputRemark,
                remarkLimit,
                countOutLimit, oldRemark
        );
        int remarkInputMaxLine = activity.getResources().getInteger(R.integer.sheet_remark_input_max_lines);
        remarkHolder.editText.setMaxLines(remarkInputMaxLine);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(nameHolder.view);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) remarkHolder.textInputLayout.getLayoutParams();
        param.topMargin = 50;
        remarkHolder.textInputLayout.setLayoutParams(param);

        ll.addView(remarkHolder.view);
        final AlertDialog dialog = manager.createCustomInsiderDialog(
                newSheet,
                activity.getString(R.string.sheet) + ": " + oldName + " (" + StringUtils.getGenDateYMD(sheet.create) + ")",
                ll
        );

        manager.setOnPositiveButtonListener(activity.getString(R.string.ensure), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameHolder.editText.getText().toString();
                String newRemark = remarkHolder.editText.getText().toString();

                if (newName.equals(oldName) && newRemark.equals(oldRemark)) {
                    ToastUtils.showShortToast(activity.getString(R.string.not_modify));
                    dialog.dismiss();
                }

                String error = null;
                if (TextUtils.isEmpty(newName)) {
                    error = activity.getString(R.string.error_name_required);
                } else {
                    newRemark = TextUtils.isEmpty(newRemark) ? "" : newRemark;

                    if (nameHolder.textInputLayout.isErrorEnabled()) {
                        TextInputHelper.textInputErrorTwinkle(nameHolder.textInputLayout, "!");
                        return;
                    } else {
                        String res = dbMusicoco.updateSheet(sheet.id, newName, newRemark);
                        if (res != null) {
                            error = res;
                        }
                    }
                }

                if (error != null) {
                    nameHolder.textInputLayout.setError(error);
                    nameHolder.textInputLayout.setErrorEnabled(true);
                } else {
                    String msg = activity.getString(R.string.success_modify_sheet) + " [" + newName + "]";
                    ToastUtils.showShortToast(msg);
                    broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
                    dialog.dismiss();
                }
            }
        });

        manager.setOnNegativeButtonListener(activity.getString(R.string.cancel), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void deleteSheet(final Sheet sheet) {
        DialogProvider provider = new DialogProvider(activity);
        final Dialog dialog = provider.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.delete_confirm));

        provider.setOnPositiveButtonListener(activity.getString(R.string.ensure), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDelete(sheet);
                dialog.hide();
            }
        });
        provider.setOnNegativeButtonListener(activity.getString(R.string.cancel), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });

        dialog.show();
    }

    private void handleDelete(final Sheet sheet) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                boolean res = dbMusicoco.removeSheet(sheet.id);

                try {
                    Thread.sleep(300);
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
