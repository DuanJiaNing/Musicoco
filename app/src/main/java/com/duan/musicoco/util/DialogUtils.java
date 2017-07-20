package com.duan.musicoco.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.shared.DialogProvider;
import com.duan.musicoco.view.TextInputHelper;

/**
 * Created by DuanJiaNing on 2017/7/17.
 */

public class DialogUtils {


    public static void showDetailDialog(Activity activity, SongInfo info) {
        DialogProvider manager = new DialogProvider(activity);

        String[] infos = new String[7];
        infos[0] = "歌曲：" + info.getTitle();
        infos[1] = "歌手：" + info.getArtist();
        infos[2] = "专辑：" + info.getAlbum();
        infos[3] = "时长：" + StringUtils.getGenTimeMS((int) info.getDuration());
        infos[4] = "格式：" + info.getMime_type();
        infos[5] = "大小：" + String.valueOf(info.getSize() >> 10 >> 10) + " MB";
        infos[6] = "路径：" + info.getData();

        View view = activity.getLayoutInflater().inflate(R.layout.list_image, null);
        ListView listView = (ListView) view.findViewById(R.id.list_image_list);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_image_image);
        listView.setAdapter(new ArrayAdapter<String>(
                activity,
                R.layout.text_view_start,
                infos
        ));

        Bitmap b = BitmapUtils.bitmapResizeFromFile(
                info.getAlbum_path(),
                imageView.getWidth(),
                imageView.getHeight());
        if (b == null) {
            b = BitmapUtils.getDefaultAlbumPicture(activity, imageView.getWidth(), imageView.getHeight());
        }

        if (b != null) {
            imageView.setImageBitmap(b);
        }

        ColorDrawable drawable = new ColorDrawable(Color.WHITE);
        drawable.setAlpha(245);
        listView.setBackground(drawable);

        manager.createFullyCustomDialog(view, "歌曲信息").show();
    }


    public static void showAddSheetDialog(final Activity activity, final DBMusicocoController dbMusicoco) {
        DialogProvider manager = new DialogProvider(activity);
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
        nameHolder.editText.setLines(1);

        final TextInputHelper.ViewHolder remarkHolder = inputHelper.getLimitedTexInputLayoutView(
                inputRemark,
                remarkLimit,
                countOutLimit, ""
        );
        remarkHolder.editText.setMaxLines(5);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(nameHolder.view);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) remarkHolder.textInputLayout.getLayoutParams();
        param.topMargin = 30;
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
}
