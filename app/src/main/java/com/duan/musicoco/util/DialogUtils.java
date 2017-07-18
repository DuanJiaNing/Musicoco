package com.duan.musicoco.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.view.TextInputHelper;

/**
 * Created by DuanJiaNing on 2017/7/17.
 */

public class DialogUtils {


    public static void showDetailDialog(SongInfo info) {
        //TODO
    }


    public static void showAddSheetDialog(final Activity activity, final DBMusicocoController dbMusicoco) {
        DialogManager manager = new DialogManager(activity);
        TextInputHelper inputHelper = new TextInputHelper(activity);

        String newSheet = activity.getString(R.string.new_sheet);
        String inputName = activity.getString(R.string.new_sheet_input_name);
        String inputRemark = activity.getString(R.string.new_sheet_input_remark);
        String countOutLimit = activity.getString(R.string.error_text_count_out_of_limit);
        String inputMessage = activity.getString(R.string.new_sheet_input_message);
        int nameLimit = activity.getResources().getInteger(R.integer.sheet_name_text_limit);
        int remarkLimit = activity.getResources().getInteger(R.integer.sheet_remark_text_limit);

        final TextInputHelper.ViewHolder nameHolder = inputHelper.getLimitedTexInputLayoutView(
                inputName,
                nameLimit,
                countOutLimit, ""
        );
        final TextInputHelper.ViewHolder remarkHolder = inputHelper.getLimitedTexInputLayoutView(
                inputRemark,
                remarkLimit,
                countOutLimit, ""
        );

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(nameHolder.view);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) remarkHolder.textInputLayout.getLayoutParams();
        param.topMargin = 30;
        remarkHolder.textInputLayout.setLayoutParams(param);

        ll.addView(remarkHolder.view);
        final AlertDialog dialog = manager.createCustomInsiderDialog(
                inputMessage,
                newSheet, ll
        );

        manager.setOnPositiveButtonListener("确定", new DialogManager.OnClickListener() {
            @Override
            public void onClick(Button view) {
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
                    ToastUtils.showToast(activity, msg);
                }
            }
        });

        manager.setOnNegativeButtonListener("取消", new DialogManager.OnClickListener() {
            @Override
            public void onClick(Button view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
