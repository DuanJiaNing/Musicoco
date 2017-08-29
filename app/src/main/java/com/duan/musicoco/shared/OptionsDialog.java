package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.util.Utils;

/**
 * Created by DuanJiaNing on 2017/7/16.
 */

public class OptionsDialog implements
        ViewVisibilityChangeable {

    private final Dialog mDialog;

    private LinearLayout contentView;
    private TextView titleText;
    private View divide;
    private ListView listView;

    private Activity activity;

    public OptionsDialog(Activity activity) {
        this.activity = activity;
        this.mDialog = new Dialog(activity, R.style.BottomDialog);

        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        mDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mDialog.setCanceledOnTouchOutside(true);

        View view = LayoutInflater.from(activity).inflate(R.layout.options_container, null);
        listView = (ListView) view.findViewById(R.id.options_list);

        contentView = (LinearLayout) view.findViewById(R.id.options_container);

        titleText = (TextView) view.findViewById(R.id.options_title);
        divide = view.findViewById(R.id.options_divide);
        mDialog.setContentView(view);
        listView.post(new Runnable() {
            @Override
            public void run() {
                setDialogHeight();
            }
        });
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        if (listener != null) {
            listView.setOnItemClickListener(listener);
        }
    }

    public void setAdapter(OptionsAdapter adapter) {
        listView.setAdapter(adapter);
        listView.post(new Runnable() {
            @Override
            public void run() {
                setDialogHeight();
            }
        });
    }

    public void reCalcuDialogHeight() {
        setDialogHeight();
    }


    private void setDialogHeight() {
        if (listView == null) {
            return;
        }
        int totalHeight = activity.getResources().getDimensionPixelSize(R.dimen.action_bar_default_height);

        totalHeight += Utils.getListViewHeight(listView);

        DisplayMetrics metrics = Utils.getMetrics(activity);
        int maxHeight = metrics.heightPixels * 3 / 5;
        totalHeight = totalHeight > maxHeight ? maxHeight : totalHeight;

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        layoutParams.height = totalHeight;
        layoutParams.width = metrics.widthPixels;
        contentView.setLayoutParams(layoutParams);
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public ListView getListView() {
        return listView;
    }

    public void setTitleTextColor(int color) {
        titleText.setTextColor(color);
    }

    public void setDivideColor(int color) {
        divide.setBackgroundColor(color);
    }

    public void setTitleBarBgColor(int color) {
        titleText.setBackgroundColor(color);
    }

    public void setContentBgColor(int color) {
        contentView.setBackgroundColor(color);
    }

    @Override
    public void show() {
        if (!visible()) {
            mDialog.show();
        }
    }

    @Override
    public void hide() {
        if (visible()) {
            mDialog.dismiss();
        }
    }

    @Override
    public boolean visible() {
        return mDialog.isShowing();
    }

    public void setTitle(String title) {
        titleText.setText(title);
    }
}
