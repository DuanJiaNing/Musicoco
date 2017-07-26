package com.duan.musicoco.detail.sheet;

import android.app.Activity;
import android.os.Build;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.SongUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/25.
 */

public class SheetSongListController implements
        View.OnClickListener,
        OnThemeChange {

    private ImageView random;
    private TextView playAllRandom;
    private View line;
    private View randomContainer;
    private RecyclerView songList;
    private FloatingActionButton fabPlayAll;

    private final Activity activity;
    private SongAdapter songAdapter;
    private IPlayControl control;

    private int sheetID;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private int currentIndex = 0;

    private final List<SongAdapter.DataHolder> data;

    public SheetSongListController(Activity activity) {
        this.activity = activity;
        this.data = new ArrayList<>();
    }

    public void initViews() {
        random = (ImageView) activity.findViewById(R.id.sheet_detail_songs_icon);
        playAllRandom = (TextView) activity.findViewById(R.id.sheet_detail_songs_play_random);
        fabPlayAll = (FloatingActionButton) activity.findViewById(R.id.sheet_detail_play_all);
        line = activity.findViewById(R.id.sheet_detail_songs_line);
        songList = (RecyclerView) activity.findViewById(R.id.sheet_detail_songs_list);
        randomContainer = activity.findViewById(R.id.sheet_detail_random_container);

        randomContainer.setOnClickListener(this);
        fabPlayAll.setOnClickListener(this);

        songList.post(new Runnable() {
            @Override
            public void run() {
                calculateRecycleViewHeight();
            }
        });
    }

    //计算 RecycleView 高度，否则无法复用 item
    private void calculateRecycleViewHeight() {
        android.support.v7.app.ActionBar bar = ((AppCompatActivity) activity).getSupportActionBar();
        if (bar != null) {
            int actionH = bar.getHeight();
            int randomCH = randomContainer.getHeight();
            int statusH = Utils.getStatusBarHeight(activity);
            int screenHeight = Utils.getMetrics(activity).heightPixels;
            int height = screenHeight - actionH - statusH - randomCH;

            ViewGroup.LayoutParams params = songList.getLayoutParams();
            params.height = height;
            songList.setLayoutParams(params);
            Log.d("musicoco", "calculateRecycleViewHeight: height=" + height);
        }
    }

    public void initData(int sheetID, DBMusicocoController dbController, MediaManager mediaManager) {
        this.sheetID = sheetID;
        this.dbController = dbController;
        this.mediaManager = mediaManager;

        control = MainActivity.getControl();

        songAdapter = new SongAdapter(activity, data);
        songList.setLayoutManager(new LinearLayoutManager(activity));
        songList.setAdapter(songAdapter);

        //在 calculateRecycleViewHeight 之后再更新
        songList.post(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }


    public void update() {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                List<DBSongInfo> ds;
                if (sheetID < 0) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbController);
                    ds = helper.getMainSheetSongInfo(sheetID);
                } else {
                    ds = dbController.getSongInfos(sheetID);
                }
                List<SongInfo> da = SongUtils.DBSongInfoToSongInfoList(ds, mediaManager);

                data.clear();
                for (int i = 0; i < da.size(); i++) {
                    SongInfo info = da.get(i);
                    boolean fa = ds.get(i).favorite;
                    SongAdapter.DataHolder dh = new SongAdapter.DataHolder(info, fa);
                    data.add(dh);
                }

                boolean isCurrentSheetPlaying = false;
                try {
                    int curID = control.getPlayListId();
                    if (curID == sheetID) {
                        isCurrentSheetPlaying = true;
                        currentIndex = control.currentSongIndex();
                    } else {
                        isCurrentSheetPlaying = false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(isCurrentSheetPlaying);
            }
        };

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isCurrentSheetPlaying) {
                        songAdapter.update(isCurrentSheetPlaying, currentIndex);
                    }
                });
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

        try {
            Song song = null;
            if (isRandom) {
                control.setPlayMode(PlayController.MODE_RANDOM);
                Random random = new Random();
                List<DBSongInfo> infos;
                if (sheetID < 0) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbController);
                    infos = helper.getMainSheetSongInfo(sheetID);
                } else {
                    infos = dbController.getSongInfos(sheetID);
                }
                song = control.setPlaySheet(sheetID, random.nextInt(infos.size()));
            } else {
                song = control.setPlaySheet(sheetID, 0);
            }
            control.resume();

            if (song != null) {
                activity.finish();
                ActivityManager.getInstance(activity).startPlayActivity();
            } else {
                ToastUtils.showShortToast(activity.getString(R.string.error_non_song_to_play));
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        songAdapter.themeChange(theme, colors);

        int mainTC = colors[0];
        int vicTC = colors[1];
        playAllRandom.setTextColor(mainTC);
        line.setBackgroundColor(vicTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            random.getDrawable().setTint(vicTC);
        }
    }
}
