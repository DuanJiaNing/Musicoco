package com.duan.musicoco.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnViewVisibilityChange;
import com.duan.musicoco.view.PullDownLinearLayout;

/**
 * Created by DuanJiaNing on 2017/7/16.
 */

public class OptionsDialog implements
        OnViewVisibilityChange {

    private final Dialog mDialog;

    private PullDownLinearLayout contentView;
    private TextView titleText;
    private View divide;
    private ListView listView;

    private Activity activity;
    private SongInfo song;

    public OptionsDialog(Activity activity) {
        this.activity = activity;
        this.mDialog = new Dialog(activity, R.style.BottomDialog);

        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        mDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        mDialog.setCanceledOnTouchOutside(true);

        View view = LayoutInflater.from(activity).inflate(R.layout.options_container, null);
        listView = (ListView) view.findViewById(R.id.options_list);

        contentView = (PullDownLinearLayout) view.findViewById(R.id.options_container);
        contentView.isListViewExist(true);

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

    public void setAdapter(ListAdapter adapter) {
        listView.setAdapter(adapter);
        listView.post(new Runnable() {
            @Override
            public void run() {
                setDialogHeight();
            }
        });
    }

    private void setDialogHeight() {
        if (listView == null) {
            return;
        }
        //FIXME getHeight == 0
        int totalHeight = activity.getResources().getDimensionPixelSize(R.dimen.action_bar_default_height);

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

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

    public void setSong(SongInfo info) {
        String title = activity.getString(R.string.song_detail);
        if (info != null) {
            title = "歌曲：" + info.getTitle();
            this.song = info;
        }
        titleText.setText(title);
    }

    public SongInfo getSong() {
        return song;
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
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    @Override
    public void hide() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}
