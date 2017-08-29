package com.duan.musicoco.play.bottomnav;

import android.app.Activity;
import android.os.Build;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.util.AnimationUtils;

/**
 * Created by DuanJiaNing on 2017/8/12.
 */

public class ListOption implements
        View.OnClickListener,
        ViewVisibilityChangeable,
        ContentUpdatable {

    private Activity activity;

    private ViewGroup container;
    private ImageButton mLocation;

    private TextView mSheet;
    private int currentDrawableColor;

    private IPlayControl control;
    private ListView mPlayList;
    private DBMusicocoController dbMusicoco;

    public ListOption(Activity activity) {
        this.activity = activity;

    }

    void initViews() {

        container = (ViewGroup) activity.findViewById(R.id.play_list_show_bar);
        mLocation = (ImageButton) activity.findViewById(R.id.play_location);
        mSheet = (TextView) activity.findViewById(R.id.play_sheet);

        mLocation.setOnClickListener(this);
    }

    void initData(IPlayControl control, ListView mPlayList, DBMusicocoController dbMusicoco) {
        this.control = control;
        this.mPlayList = mPlayList;
        this.dbMusicoco = dbMusicoco;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_location:
                try {
                    int index = control.currentSongIndex();
                    mPlayList.smoothScrollToPosition(index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void updateCurrentSheet() {
        try {
            int id = control.getPlayListId();
            if (id < 0) {
                String name = MainSheetHelper.getMainSheetName(activity, id);
                mSheet.setText(name);
            } else {
                Sheet sheet = dbMusicoco.getSheet(id);
                String name = activity.getString(R.string.sheet) + ": " + sheet.name + " ("
                        + sheet.count + activity.getString(R.string.head) + ")";
                mSheet.setText(name);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        container.setVisibility(View.VISIBLE);
        AnimationUtils.startAlphaAnim(container, 500, null, 0.0f, 1.0f);
    }

    @Override
    public void hide() {
        container.setVisibility(View.GONE);
    }

    @Override
    public boolean visible() {
        return false;
    }

    public void updateColors() {

        mSheet.setTextColor(currentDrawableColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLocation.getDrawable().setTint(currentDrawableColor);
        }
    }


    @Override
    public void update(Object obj, OnUpdateStatusChanged statusChanged) {
        updateCurrentSheet();
    }

    @Override
    public void noData() {
    }

    void setDrawableColor(int currentDrawableColor) {
        this.currentDrawableColor = currentDrawableColor;
    }
}