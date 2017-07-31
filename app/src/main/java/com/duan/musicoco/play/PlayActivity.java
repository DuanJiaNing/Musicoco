package com.duan.musicoco.play;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.album.VisualizerPresenter;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.play.lyric.LyricPresenter;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.shared.ExceptionHandler;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements
        PlayServiceCallback,
        OnServiceConnect,
        View.OnClickListener,
        OnEmptyMediaLibrary,
        OnThemeChange {

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;
    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private PlayServiceConnection mServiceConnection;
    private IPlayControl control;

    private BottomNavigationController bottomNavigationController;
    private PlayBgDrawableController bgDrawableController;
    private PlayViewsController viewsController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection.hasConnected) {
            mServiceConnection.unregisterListener();
            unbindService(mServiceConnection);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreference();
        visualizerPresenter.stopPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (control != null) {
            synchronize(null, true);
        }
    }

    private void savePreference() {

        try {
            Song song = control.currentSong();
            String path = song.path;

            int index = control.currentSongIndex();
            int pro = control.getProgress();
            int mode = control.getPlayMode();

            playPreference.updateSong(new PlayPreference.CurrentSong(path, pro, index));
            playPreference.updatePlayMode(mode);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationController.isListShowing()) {
            bottomNavigationController.hide();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void songChanged(Song song, int index, boolean isNext) {

        //FIXME 次数计算策略完善
        dbMusicoco.addSongPlayTimes(song);
        synchronize(song, isNext);

    }

    /**
     * 同步
     * onResume null true
     * songChanged -- --
     */
    public void synchronize(@Nullable Song song, boolean isNext) {

        if (song == null) {
            try {
                song = control.currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }

        if (song == null) {
            return;
        }

        //更新文字
        int pro = 0;
        try {
            pro = control.getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        SongInfo info = mediaManager.getSongInfo(song);
        int duration = (int) info.getDuration();
        int progress = pro;
        String name = info.getTitle();
        String arts = info.getArtist();
        viewsController.updateText(duration, progress, name, arts);

        //更新状态 要在更新颜色之前更新
        updateStatus(song, isNext);

        //更新背景
        boolean updateBG = playPreference.getTheme().equals(ThemeEnum.VARYING);
        if (updateBG) {
            updateViews(visualizerFragment.getCurrColors(), song);
        }

        //在 updateViews 后调用
        bottomNavigationController.updateFavorite();

    }

    private void updateStatus(Song song, boolean isNext) {
        Log.d("update", "PlayActivity updateStatus");

        try {
            boolean playing = control.status() == PlayController.STATUS_PLAYING;
            viewsController.updatePlayBtStatus(playing);
            if (playing) {
                visualizerPresenter.startPlay();
            } else {
                visualizerPresenter.stopPlay();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        boolean updateBg = playPreference.getTheme().equals(ThemeEnum.VARYING);
        visualizerPresenter.songChanged(song, isNext, updateBg);

        bottomNavigationController.updatePlayMode();

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        viewsController.startProgressUpdateTask();
        visualizerPresenter.startPlay();
        viewsController.updatePlayBtStatus(true);
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        viewsController.stopProgressUpdateTask();
        visualizerPresenter.stopPlay();
        viewsController.updatePlayBtStatus(false);
    }

    @Override
    public void onPlayListChange(Song current, int index, int id) {
        bottomNavigationController.update(null, null);
    }

    @Override
    public void dataIsReady(IPlayControl mControl) {

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        themeEnum = playPreference.getTheme();
        int cs[];
        switch (themeEnum) {
            case DARK:
                cs = com.duan.musicoco.util.ColorUtils.get10DarkThemeColors(this);
                break;
            case VARYING:
                return; // 当主题为【随专辑变换】时，由 songChanged 控制颜色
            default:
            case WHITE:
                cs = com.duan.musicoco.util.ColorUtils.get10WhiteThemeColors(this);
                break;
        }

        updateViews(cs);
    }

    private void updateViews(int[] colors) {

        int statusC = colors[0];
        int toolbarC = colors[1];
        int accentC = colors[2];
        int mainBC = colors[3];
        int vicBC = colors[4];
        int mainTC = colors[5];
        int vicTC = colors[6];
        int navC = colors[7];
        int toolbarMainTC = colors[8];
        int toolbarVicTC = colors[9];

        viewsController.updateColors(new int[]{mainBC, mainTC, vicBC, vicTC});
        bottomNavigationController.updateColors(vicBC, false);
        bgDrawableController.initBackgroundColor(mainBC);

    }

    /**
     * 更新颜色（当主题为 随专辑变化 时）<br>
     * <p>
     * 0 暗的活力颜色 主背景色<br>
     * 1 暗的活力颜色 对应适合的字体颜色 主字体色<br>
     * 2 暗的柔和颜色 辅背景色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色 辅字体色<br>
     */
    private void updateViews(int[] colors, Song song) {
        Log.d("update", "PlayActivity updateViews");

        if (colors.length != 4 || song == null) {
            return;
        }

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        viewsController.updateColors(new int[]{mainBC, mainTC, vicBC, vicTC});
        bottomNavigationController.updateColors(vicBC, true);
        bgDrawableController.updateBackground(mainBC, mediaManager.getSongInfo(song));

    }

    @Override
    public void permissionGranted(int requestCode) {
        super.permissionGranted(requestCode);

        mServiceConnection = new PlayServiceConnection(this, this, this);
        playServiceManager.bindService(mServiceConnection);
    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    protected void initViews() {

        //初始控件
        if (bgDrawableController == null) {
            bgDrawableController = new PlayBgDrawableController(this, playPreference);
        }
        bgDrawableController.initViews();

        if (viewsController == null) {
            viewsController = new PlayViewsController(this);
        }
        viewsController.initViews();

        if (bottomNavigationController == null) {
            bottomNavigationController = new BottomNavigationController(
                    this,
                    dbMusicoco,
                    mediaManager,
                    playPreference,
                    appPreference);
        }

        initSelfViews();

    }

    private void initSelfViews() {
        FrameLayout flFragmentContainer;
        flFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);
        flFragmentContainer.setClickable(true);
        flFragmentContainer.setOnClickListener(this);
        flFragmentContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (bottomNavigationController.isListTitleHide()) {
                    bottomNavigationController.showPlayListTitle();
                    return true;
                }
                return false;
            }
        });

        View nameContainer = findViewById(R.id.play_name_container);
        nameContainer.setClickable(true);
        nameContainer.setOnClickListener(this);

        lyricFragment = new LyricFragment();
        visualizerFragment = new VisualizerFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //顺序 依次叠放
        transaction.add(R.id.play_fragment_container, visualizerFragment, VisualizerFragment.TAG);
        transaction.add(R.id.play_fragment_container, lyricFragment, LyricFragment.TAG);
        transaction.hide(lyricFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_fragment_container:
                if (!bottomNavigationController.isListShowing())
                    bottomNavigationController.show();
                break;
            case R.id.play_name_container:
                try {
                    Song song = control.currentSong();
                    ActivityManager.getInstance(this).startSongDetailActivity(song);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnected(final ComponentName name, IBinder service) {
        this.control = mServiceConnection.takeControl();

        visualizerPresenter = new VisualizerPresenter(this, control, visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

        initSelfData();
        themeChange(null, null);
        bottomNavigationController.themeChange(null, null);

        visualizerPresenter.initData(null);
        lyricPresenter.initData(null);

    }

    private void initSelfData() {
        try {
            List<Song> songs = control.getPlayList();
            if (songs == null) { //检查播放列表是否为空
                emptyMediaLibrary();
                viewsController.updateText(0, 0, "", "");
            } else {
                bottomNavigationController.initData(control);
                bottomNavigationController.update(null, null);

                viewsController.initData(playPreference, control);

                Song song = control.currentSong();
                int index = songs.indexOf(song);
                //服务端在 onCreate 时会回调 songChanged ，PlayActivity 第一次绑定可能接收不到此次回调
                songChanged(song, index, true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disConnected(ComponentName name) {
        mServiceConnection = null;
        mServiceConnection = new PlayServiceConnection(this, this, this);
        playServiceManager.bindService(mServiceConnection);
    }

    @Override
    public void emptyMediaLibrary() {
        bottomNavigationController.emptyMediaLibrary();
    }
}
