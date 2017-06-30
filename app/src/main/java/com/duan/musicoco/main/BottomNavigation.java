package com.duan.musicoco.main;

import android.app.Activity;
import android.content.Context;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.view.media.PlayView;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class BottomNavigation implements IBottomNavigation, View.OnClickListener, PlayServiceCallback {

    private final Context context;

    private View container;
    private ImageView album;
    private TextView name;
    private TextView arts;
    private PlayView play;
    private ImageButton showList;

    private boolean isListShow = false;
    private IPlayControl controller;

    BottomNavigation(Context context) {
        this.context = context;
    }

    public void initView() {
        View rootView = LayoutInflater.from(context).inflate(R.layout.activity_list_bottom_navigation, null);

        album = (ImageView) rootView.findViewById(R.id.list_album);
        name = (TextView) rootView.findViewById(R.id.list_name);
        arts = (TextView) rootView.findViewById(R.id.list_arts);
        play = (PlayView) rootView.findViewById(R.id.list_play);
        showList = (ImageButton) rootView.findViewById(R.id.list_list);
        container = rootView.findViewById(R.id.list_bottom_nav_container);

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

    @Override
    public void songChanged(Song song, int index) {

    }

    @Override
    public void startPlay(Song song, int index, int status) {

    }

    @Override
    public void stopPlay(Song song, int index, int status) {

    }

    public void setController(IPlayControl controller) {
        this.controller = controller;
    }
}
