package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.AlertDialog;
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

/**
 * Created by DuanJiaNing on 2017/7/21.
 */

public class MySheetsOperation {

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;

    public MySheetsOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
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

        manager.setOnPositiveButtonListener("确定", new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameHolder.editText.getText().toString();
                String error = null;

                if (TextUtils.isEmpty(name)) {
                    error = activity.getString(R.string.error_name_required);
                } else {
                    String remark = remarkHolder.editText.getText().toString();
                    remark = TextUtils.isEmpty(remark) ? "" : remark;

                    String res = dbMusicoco.addSheet(name, remark, 0);
                    if (res != null) {
                        error = res;
                    }
                }

                if (error != null) {
                    nameHolder.textInputLayout.setError(error);
                    nameHolder.textInputLayout.setErrorEnabled(true);
                } else {
                    String msg = activity.getString(R.string.success_create_sheet) + "[" + name + "]";
                    ToastUtils.showShortToast(activity, msg);
                    BroadcastManager.sendMyBroadcast(activity, BroadcastManager.FILTER_MY_SHEET_CHANGED);
                    dialog.dismiss();
                }
            }
        });

        manager.setOnNegativeButtonListener("取消", new DialogProvider.OnClickListener() {
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
                "歌单：" + oldName + " (" + StringUtils.getGenDateYMD(sheet.create) + ")",
                ll
        );

        manager.setOnPositiveButtonListener("确定", new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameHolder.editText.getText().toString();
                String newRemark = remarkHolder.editText.getText().toString();

                if (newName.equals(oldName) && newRemark.equals(oldRemark)) {
                    ToastUtils.showShortToast(activity, activity.getString(R.string.not_modify));
                    dialog.dismiss();
                }

                String error = null;
                if (TextUtils.isEmpty(newName)) {
                    error = activity.getString(R.string.error_name_required);
                } else {
                    newRemark = TextUtils.isEmpty(newRemark) ? "" : newRemark;

                    String res = dbMusicoco.updateSheet(sheet.id, newName, newRemark);
                    if (res != null) {
                        error = res;
                    }
                }

                if (error != null) {
                    nameHolder.textInputLayout.setError(error);
                    nameHolder.textInputLayout.setErrorEnabled(true);
                } else {
                    String msg = activity.getString(R.string.success_modify_sheet) + "[" + newName + "]";
                    ToastUtils.showShortToast(activity, msg);
                    BroadcastManager.sendMyBroadcast(activity, BroadcastManager.FILTER_MY_SHEET_CHANGED);
                    dialog.dismiss();
                }
            }
        });

        manager.setOnNegativeButtonListener("取消", new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void deleteSheet(int sheetID) {

    }

}
