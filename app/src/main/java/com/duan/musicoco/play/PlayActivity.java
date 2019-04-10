package com.duan.musicoco.play;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.InspectActivity;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.play.album.PlayVisualizer;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.bottomnav.BottomNavigationController;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.util.ArrayUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.Utils;

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

    private PlayVisualizer playVisualizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Utils.transitionStatusBar(this);
        Utils.hideNavAndStatus(getWindow().getDecorView());

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
                songChanged(song, control.currentSongIndex(), true);

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateVisualizer() {
        try {

            if (playVisualizer == null) {

                int audioSessionId = control.getAudioSessionId();
                playVisualizer = new PlayVisualizer();
                playVisualizer.setupVisualizer(160, audioSessionId, new PlayVisualizer.OnFftDataCaptureListener() {
                    @Override
                    public void onFftCapture(float[] fft) {
//                        float[] ffs = handleFFT(fft);
                        viewsController.updateBarWaveHeight(fft);
                        viewsController.updateBarWaveColors(
                                handleColors(fft, viewsController.getBarWaveColor()));
                    }
                });
            }

            playVisualizer.setVisualizerEnable(settingPreference.getDotWaveEnable() &&
                    control.status() == PlayController.STATUS_PLAYING);

            viewsController.updateBarWaveVisible(settingPreference.getDotWaveEnable());

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private int[][] handleColors(float[] fft, PlayViewsController.BarWaveColor barWaveColor) {

        int[][] cs = new int[fft.length][2];
        float average = ArrayUtils.average(fft);
        for (int i = 0; i < fft.length; i++) {
            cs[i][0] = barWaveColor.barColor;
            cs[i][1] = barWaveColor.waveColor;

        }

        return cs;
    }

    private float[] handleFFT(float[] fft) {

        int splitLength = fft.length / 3; // 20

        float average = ArrayUtils.average(fft);
        for (int i = 0; i < fft.length; i++) {
            if (fft[i] > average * 3) fft[i] = fft[i] - average;
        }

        float[] t1 = new float[20];
        float[] t2 = new float[40];
        float[] t2_ = new float[20];
        float[] t3 = new float[20];

        System.arraycopy(fft, 19, t1, 0, 20);
        t1 = ArrayUtils.reverse(t1);
        System.arraycopy(fft, 39, t3, 0, 20);

        System.arraycopy(fft, 0, t2_, 0, 20);
        int j = 0;
        for (int i = 0; i < t2_.length - 1; i++) {
            t2[j++] = t2_[i];
            t2[j++] = t2_[i] + (t2_[i + 1] - t2_[i]) / 2;
        }

//        float[] fft_ = new float[80]; // 与 play_bar_waves 一致
//        System.arraycopy(t1, 0, fft_, 0, 20);
//        System.arraycopy(t2, 0, fft_, 20, 40);
//        System.arraycopy(t3, 0, fft_, 60, 20);
//
//        float[] fft__ = new float[160];
//        j = 0;
//        for (int i = 0; i < fft_.length - 1; i++) {
//            fft__[j++] = fft_[i];
//            fft__[j++] = fft_[i] + (fft_[i + 1] - fft_[i]) / 2;
//        }

        float[] fft__ = new float[80];
        j = 0;
        for (int i = 0; i < t2.length - 1; i++) {
            fft__[j++] = t2[i];
            fft__[j++] = t2[i] + (t2[i + 1] - t2[i]) / 2;
        }

        float[] fft___ = new float[160];
        j = 0;
        for (int i = 0; i < fft__.length - 1; i++) {
            fft___[j++] = fft__[i];
            fft___[j++] = fft__[i] + (fft__[i + 1] - fft__[i]) / 2;
        }

        return fft___;
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
        if (settingPreference.getDotWaveEnable()) {
            playVisualizer.stopListen();
        }
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
        updateVisualizer();

    }

    private void savePreference() {

        try {
            Song song = control.currentSong();
            if (song == null) {
                return;
            }

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
            updateVisualizer();
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

        //在 initViewsColors 后调用，区别于 updateViewsColorsIfNeed 方法内的调用，
        // updateViewsColorsIfNeed 方法只有在 VARYING 模式下才执行。即 VARYING 模
        // 式下这里的调用会被覆盖。
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
        if (song == null || index == -1) {
            return;
        }

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
        updateVisualizer();
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        viewsController.stopProgressUpdateTask();
        viewsController.updatePlayButtonStatus(false);
        visualizerFragment.stopSpin();
        updateVisualizer();

    }

    @Override
    public void onPlayListChange(Song current, int index, int id) {
        if (current == null || index < 0) {
            return;
        }

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
        if (theme != ThemeEnum.VARYING) {
            // VARYING 模式下由 updateCurrentSongInfo 和 updateViewsColorsIfNeed 方法控制界面颜色
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
    private void updateViewsColorsIfNeed(@Nullable Song song) {

        if (!playPreference.getTheme().equals(ThemeEnum.VARYING)) {
            return;
        }

        if (visualizerFragment == null) {
            return;
        }

        int[] colors = visualizerFragment.getCurrColors();

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        viewsController.updateColors(new int[]{mainBC, mainTC, vicBC, vicTC});
        bottomNavigationController.updateColors(vicBC, true);

        Bitmap album = visualizerFragment.getAlbum();
        int defaultColor = getResources().getColor(R.color.default_play_text_color, null);
        int waveColor[] = new int[2];
        ColorUtils.get2ColorFormBitmap(album, defaultColor, waveColor);
        viewsController.updateWaveColors(waveColor[1], waveColor[0]);

        if (song == null) {
            try {
                song = control.currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (song != null) {
            bgDrawableController.updateBackground(mainBC, vicBC, mediaManager.getSongInfo(this, song));
        }

        // 区别于 updateCurrentSongInfo 方法中的调用，这里只
        // 在 VARYING 模式下才不断更新
        bottomNavigationController.updateFavorite();
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
                    if (song != null) {
                        ActivityManager.getInstance().startSongDetailActivity(this, song, true);
                    }
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
