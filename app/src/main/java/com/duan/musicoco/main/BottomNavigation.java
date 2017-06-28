package com.duan.musicoco.main;

import android.app.Activity;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.view.media.PlayView;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class BottomNavigation implements IBottomNavigation, View.OnClickListener {

    private final Activity activity;

    private final View container;
    private final ImageView album;
    private final TextView name;
    private final TextView arts;
    private final PlayView play;
    private final ImageButton showList;
    private final IPlayControl control;

    private boolean isListShow = false;

    BottomNavigation(Activity activity, IPlayControl control) {
        this.activity = activity;
        this.control = control;

        this.album = (ImageView) activity.findViewById(R.id.list_album);
        this.name = (TextView) activity.findViewById(R.id.list_name);
        this.arts = (TextView) activity.findViewById(R.id.list_arts);
        this.play = (PlayView) activity.findViewById(R.id.list_play);
        this.showList = (ImageButton) activity.findViewById(R.id.list_list);
        this.container = activity.findViewById(R.id.list_bottom_nav_container);

        try {
            play.setPlayStatus(control.status() == PlayController.STATUS_PLAYING);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        container.setOnClickListener(this);
        showList.setOnClickListener(this);
        play.setOnClickListener(this);

    }

    @Override
    public void changeSong(Song song, int index) {

    }

    @Override
    public void showPlayList() {
        isListShow = true;

    }

    @Override
    public void hidePlayList() {
        isListShow = false;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.list_bottom_nav_container:
                break;
            case R.id.list_play:
                break;
            case R.id.list_list:
                if (isListShow)
                    hidePlayList();
                else showPlayList();
                break;
        }
    }
}
