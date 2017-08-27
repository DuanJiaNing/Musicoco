package com.duan.musicoco.play;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.InspectActivity;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.bottomnav.BottomNavigationController;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends InspectActivity implements
        PlayServiceCallback,
        OnServiceConnect,
        View.OnClickListener,
        ThemeChangeable {

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;
    protected IPlayControl control;

    private PlayServiceConnection mServiceConnection;
    private PlayServiceManager playServiceManager;

    private BottomNavigationController bottomNavigationController;
    private PlayBgDrawableController bgDrawableController;
    private PlayViewsController viewsController;

    private BroadcastReceiver themeChangeReceiver;
    private BroadcastReceiver songFavoriteChangeReceiver;
    private BroadcastManager broadcastManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);

        //权限检查完成后回调 permissionGranted 或 permissionDenied
        checkPermission();

    }

    @Override
    public void permissionGranted(int requestCode) {

        broadcastManager = BroadcastManager.getInstance();
        playServiceManager = new PlayServiceManager(this);
        bgDrawableController = new PlayBgDrawableController(this, playPreference);
        viewsController = new PlayViewsController(this);
        bottomNavigationController = new BottomNavigationController(this, dbController, mediaManager, playPreference, appPreference);

        initViews();
        bindService();

    }

    private void initViews() {

        bgDrawableController.initViews();
        viewsController.initViews();
        bottomNavigationController.initViews();
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

    private void bindService() {
        mServiceConnection = new PlayServiceConnection(this, this, this);
        // 绑定成功后回调 onConnected
        playServiceManager.bindService(mServiceConnection);
    }

    @Override
    public void onConnected(final ComponentName name, IBinder service) {
        this.control = IPlayControl.Stub.asInterface(service);

        initSelfData();
        initBroadcastReceivers();

    }

    private void initSelfData() {
        try {
            Song song = control.currentSong();

            if (song == null) { //检查播放列表是否为空
                noSongInService();
                viewsController.updateText(0, 0, "", "");
            } else {

                bottomNavigationController.initData(control);
                viewsController.initData(playPreference, control);

                // 在 updateCurrentSongInfo 之前，initData 之后调用，updateCurrentSongInfo 会模拟歌曲切换（仅在 VARYING 时）重新设置颜色
                // 即非 VARYING 时，界面颜色是在这里设置的
                themeChange(null, null);
                initViewsColors();

                // 服务端在 onCreate 时会回调 songChanged ，PlayActivity 第一次绑定可能接收不到此次回调
                // 手动同步歌曲信息
                updateCurrentSongInfo(song, true);
                updateViewsColorsIfNeed(song);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    // 服务器的歌单是空的，这里不采用实现 ContentUpdatable 接口的原因是考虑到，将来播放界面可以单独以其他方式启动
    // 如：在文件夹中选择了一首歌并播放，选择我们的播放器播放时，可以不启动 MainActivity ，只启动 PlayService 和
    // PlayActivity 播放歌曲
    public void noSongInService() {
        bottomNavigationController.noSongInService();
    }

    private void initBroadcastReceivers() {
        themeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int which = intent.getIntExtra(BroadcastManager.Play.PLAY_THEME_CHANGE_TOKEN, Integer.MAX_VALUE);
                if (which == BroadcastManager.Play.PLAY_APP_THEME_CHANGE) {
                    themeChange(null, null);
                } else if (which == BroadcastManager.Play.PLAY_PLAY_THEME_CHANGE) {
                    updateViewsColorsIfNeed(null);
                    initViewsColors();
                }
            }
        };

        songFavoriteChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bottomNavigationController.updateFavorite();
            }
        };

        broadcastManager.registerBroadReceiver(this, songFavoriteChangeReceiver, BroadcastManager.FILTER_MAIN_SHEET_UPDATE);
        broadcastManager.registerBroadReceiver(this, themeChangeReceiver, BroadcastManager.FILTER_PLAY_UI_MODE_CHANGE);
    }

    //--------------------------------------------------------------------//--------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        unregisterReceiver();
    }

    private void unbindService() {
        if (mServiceConnection != null && mServiceConnection.hasConnected) {
            mServiceConnection.unregisterListener();
            unbindService(mServiceConnection);
            mServiceConnection.hasConnected = false;
        }
    }

    private void unregisterReceiver() {
        if (themeChangeReceiver != null) {
            broadcastManager.unregisterReceiver(this, themeChangeReceiver);
        }

        if (songFavoriteChangeReceiver != null) {
            broadcastManager.unregisterReceiver(this, songFavoriteChangeReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreference();
        visualizerFragment.stopSpin();
        viewsController.stopProgressUpdateTask();
    }

    private void savePreference() {

        try {
            Song song = control.currentSong();
            String path = song.path;

            int index = control.currentSongIndex();
            int pro = control.getProgress();
            int mode = control.getPlayMode();

            playPreference.updateLastPlaySong(new PlayPreference.CurrentSong(path, pro, index));
            playPreference.updatePlayMode(mode);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationController.visible()) {
            bottomNavigationController.hide();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (control != null) {
            updateCurrentSongInfo(null, true);
            updateViewsColorsIfNeed(null);
        }
    }

    /**
     * 同步当前播放歌曲信息，状态
     *
     * @param song   当前播放歌曲，在第一次打开 PlayActivity 时 传 null ，之后该方法只应该被 songChanged 回调
     * @param isNext true 为下一首，false为上一首，由该值决定切歌时专辑图片进出方向
     */
    public void updateCurrentSongInfo(@Nullable Song song, boolean isNext) {

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

        SongInfo info = mediaManager.getSongInfo(this, song);
        if (info == null) {
            return;
        }

        //更新文字
        int pro = 0;
        try {
            pro = control.getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        int duration = (int) info.getDuration();
        int progress = pro;
        String name = info.getTitle();
        String arts = info.getArtist();
        viewsController.updateText(duration, progress, name, arts);

        //更新状态
        updateStatus();
        bottomNavigationController.updatePlayMode();

        // 更新专辑图片，需要的话（当前歌曲已经切换了）
        boolean updateBg = playPreference.getTheme().equals(ThemeEnum.VARYING);
        visualizerFragment.songChanged(song, isNext, updateBg);

        //在 initViewsColors 后调用
        bottomNavigationController.updateFavorite();
        bottomNavigationController.update(null, null);

    }

    private void updateStatus() {

        try {
            boolean playing = control.status() == PlayController.STATUS_PLAYING;

            viewsController.updatePlayButtonStatus(playing);
            if (playing) {
                visualizerFragment.startSpin();
                viewsController.startProgressUpdateTask();
            } else {
                visualizerFragment.stopSpin();
                viewsController.stopProgressUpdateTask();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void songChanged(Song song, int index, boolean isNext) {

        try {
            // UPDATE: 2017/8/26 更新 次数计算策略完善
            if (control.status() == PlayController.STATUS_PLAYING) {
                dbController.addSongPlayTimes(song);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        updateCurrentSongInfo(song, isNext);
        updateViewsColorsIfNeed(song);

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        viewsController.startProgressUpdateTask();
        viewsController.updatePlayButtonStatus(true);
        visualizerFragment.startSpin();
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        viewsController.stopProgressUpdateTask();
        viewsController.updatePlayButtonStatus(false);
        visualizerFragment.stopSpin();
    }

    @Override
    public void onPlayListChange(Song current, int index, int id) {
        bottomNavigationController.update(null, null);
    }

    @Override
    public void dataIsReady(IPlayControl mControl) {

    }

    // App 主题改变
    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        bottomNavigationController.themeChange(null, null);
    }

    // 初始化由 playPreference 指定的主题
    private void initViewsColors() {

        ThemeEnum theme = playPreference.getTheme();
        if (theme != ThemeEnum.VARYING) { // VARYING 模式下由 updateCurrentSongInfo 和 updateViewsColorsIfNeed 方法控制界面
            int colors[] = ColorUtils.get10ThemeColors(this, theme);

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

    }

    /**
     * 更新控件颜色，背景。当主题为【随专辑变化】时才生效）<br>
     * 要在 updateCurrentSongInfo 之后调用<br>
     * <p>
     * 0 暗的活力颜色 主背景色<br>
     * 1 暗的活力颜色 对应适合的字体颜色 主字体色<br>
     * 2 暗的柔和颜色 辅背景色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色 辅字体色<br>
     */
    private void updateViewsColorsIfNeed(Song song) {

        if (!playPreference.getTheme().equals(ThemeEnum.VARYING)) {
            return;
        }

        if (visualizerFragment == null) {
            return;
        }

        int[] colors = visualizerFragment.getCurrColors();

        if (song == null) {
            try {
                song = control.currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        viewsController.updateColors(new int[]{mainBC, mainTC, vicBC, vicTC});
        bottomNavigationController.updateColors(vicBC, true);
        bgDrawableController.updateBackground(mainBC, vicBC, mediaManager.getSongInfo(this, song));

    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_fragment_container:
                if (!bottomNavigationController.visible() && !bottomNavigationController.isAniming()) {
                    bottomNavigationController.show();
                }
                break;
            case R.id.play_name_container:
                try {
                    Song song = control.currentSong();
                    ActivityManager.getInstance().startSongDetailActivity(this, song, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void disConnected(ComponentName name) {
        mServiceConnection = null;
        mServiceConnection = new PlayServiceConnection(this, this, this);
        playServiceManager.bindService(mServiceConnection);
    }

}
