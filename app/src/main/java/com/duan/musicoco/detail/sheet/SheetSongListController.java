package com.duan.musicoco.detail.sheet;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.Theme;

/**
 * Created by DuanJiaNing on 2017/7/25.
 */

public class SheetSongListController implements
        View.OnClickListener,
        OnThemeChange {

    private View container;
    private ImageView random;
    private TextView playAllRandom;
    private View line;
    private View randomContainer;
    private RecyclerView songList;
    private FloatingActionButton fabPlayAll;

    private final Activity activity;
    private IPlayControl control;

    private int sheetID;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    public SheetSongListController(Activity activity) {
        this.activity = activity;
    }

    public void initViews() {
        container = activity.findViewById(R.id.sheet_detail_song_list_container);
        random = (ImageView) activity.findViewById(R.id.sheet_detail_songs_icon);
        playAllRandom = (TextView) activity.findViewById(R.id.sheet_detail_songs_play_random);
        fabPlayAll = (FloatingActionButton) activity.findViewById(R.id.sheet_detail_play_all);
        line = activity.findViewById(R.id.sheet_detail_songs_line);
        songList = (RecyclerView) activity.findViewById(R.id.sheet_detail_songs_list);
        randomContainer = activity.findViewById(R.id.sheet_detail_random_container);

        randomContainer.setOnClickListener(this);
        fabPlayAll.setOnClickListener(this);
    }

    public void initData(int sheetID, DBMusicocoController dbController, MediaManager mediaManager) {
        this.sheetID = sheetID;
        this.dbController = dbController;
        this.mediaManager = mediaManager;

        control = MainActivity.getControl();

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sheet_detail_random_container:
                playAll(true);
                break;
            case R.id.sheet_detail_play_all:
                playAll(false);
                break;
        }
    }

    private void playAll(boolean isRandom) {

        //FIXME

//        try {
//            if (isRandom) {
//                control.setPlayMode(PlayController.MODE_RANDOM);
//                Random random = new Random();
//                control.setPlayList(songs, random.nextInt(songs.size()), sheetID);
//            } else {
//                control.setPlayList(songs, 0, sheetID);
//            }
//            control.resume();
//
//            activity.finish();
//            ActivityManager.getInstance(activity).startPlayActivity();
//            ToastUtils.showShortToast(activity.getString(R.string.error_non_song_to_play));
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

    }
}
