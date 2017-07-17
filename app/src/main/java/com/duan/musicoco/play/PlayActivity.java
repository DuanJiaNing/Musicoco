package com.duan.musicoco.play;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.ExceptionHandler;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.album.VisualizerPresenter;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.play.lyric.LyricPresenter;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.PeriodicTask;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import java.util.List;

import static com.duan.musicoco.preference.Theme.WHITE;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements
        PlayServiceCallback,
        OnServiceConnect,
        View.OnClickListener,
        View.OnLongClickListener,
        OnEmptyMediaLibrary,
        OnThemeChange {

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;
    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private TextView tvPlayProgress;
    private TextView tvDuration;

    private View nameContainer;
    private TextSwitcher tsSongName;
    private TextSwitcher tsSongArts;

    private DiscreteSeekBar sbSongProgress;
    private FrameLayout flFragmentContainer;
    private FrameLayout flRootView;
    private PlayView btPlay;
    private SkipView btPre;
    private SkipView btNext;

    private PlayServiceConnection mServiceConnection;
    private PeriodicTask periodicTask;
    private final PlayPreference playPreference;
    private BottomNavigationController bottomNavigationController;

    public PlayActivity() {
        playPreference = new PlayPreference(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (mServiceConnection.takeControl() != null) {
            synchronize(null, true);
        }
    }

    private void savePreference() {

        try {
            IPlayControl control = mServiceConnection.takeControl();
            Song song = control.currentSong();
            String path = song.path;
            int index = control.currentSongIndex();
            int pro = sbSongProgress.getProgress();
            int mode = control.getPlayMode();

            playPreference.updateSong(new PlayPreference.CurrentSong(path, pro, index));
            playPreference.updatePlayMode(mode);

        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
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
     * 同步当前播放歌曲
     */
    public void synchronize(@Nullable Song song, boolean isNext) {

        if (song == null) {
            try {
                song = mServiceConnection.takeControl().currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
                new ExceptionHandler().handleRemoteException(this,
                        this.getString(R.string.exception_remote), null
                );
                return;
            }
        }

        if (song == null) {
            return;
        }

        //更新文字
        int pro = 0;
        try {
            pro = mServiceConnection.takeControl().getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }
        SongInfo info = mediaManager.getSongInfo(song);
        int duration = (int) info.getDuration();
        int progress = pro;
        String name = info.getTitle();
        String arts = info.getArtist();
        updateData(duration, progress, name, arts);

        //更新状态 要在更新颜色之前更新
        updateStatus(song, isNext);

        //更新颜色
        boolean updateColor = playPreference.getTheme().equals(Theme.VARYING);
        if (updateColor) {
            updateColors(visualizerFragment.getCurrColors());
        }

        //在 updateColors 后调用
        bottomNavigationController.updateFavorite();

    }

    private void updateStatus(Song song, boolean isNext) {

        IPlayControl control = mServiceConnection.takeControl();
        try {
            boolean playing = control.status() == PlayController.STATUS_PLAYING;
            btPlay.setPlayStatus(playing);
            if (playing) {
                visualizerPresenter.startPlay();
            } else {
                visualizerPresenter.stopPlay();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        boolean updateColor = playPreference.getTheme().equals(Theme.VARYING);
        visualizerPresenter.songChanged(song, isNext, updateColor);

        bottomNavigationController.updatePlayMode();

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        periodicTask.start();

        visualizerPresenter.startPlay();
        btPlay.setPlayStatus(true);
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        periodicTask.stop();

        visualizerPresenter.stopPlay();
        btPlay.setPlayStatus(false);
    }

    @Override
    public void onPlayListChange(Song current, int index, int id) {

    }

    @Override
    public void dataIsReady(IPlayControl mControl) {

    }

    /**
     * 更新颜色（当主题为 随专辑变化 时）<br>
     * 1 背景颜色<br>
     * 2 歌曲名字，艺术家字体颜色<br>
     * 3 进度条颜色，进度条下文字颜色<br>
     * 4 上、下曲，暂停按钮颜色<br>
     * 5 播放列表头颜色，背景色<br>
     * 6 调用方法更新播放列表颜色模式（亮，暗）<br>
     * <p>
     * 0 暗的活力颜色 主背景色<br>
     * 1 暗的活力颜色 对应适合的字体颜色 主字体色<br>
     * 2 暗的柔和颜色 辅背景色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色 辅字体色<br>
     */
    private void updateColors(int[] colors) {
        if (colors.length != 4) {
            return;
        }

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        ((TextView) (tsSongName.getCurrentView())).setTextColor(mainTC);
        ((TextView) (tsSongArts.getCurrentView())).setTextColor(vicTC);

        int colorTo = mainBC;
        ColorDrawable cd = (ColorDrawable) flRootView.getBackground();
        if (cd != null) {
            if (cd.getColor() != colorTo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AnimationUtils.startColorGradientAnim(1000, flRootView, cd.getColor(), colorTo);
                } else {
                    flRootView.setBackgroundColor(colorTo);
                }
            }
        } else {
            flRootView.setBackgroundColor(colorTo);
        }

        tvPlayProgress.setTextColor(vicTC);
        tvDuration.setTextColor(vicTC);

        btPre.setTriangleColor(mainTC);
        btNext.setTriangleColor(mainTC);

        btPlay.setTriangleColor(mainTC);
        btPlay.setSolidColor(mainTC);
        btPlay.setPauseLineColor(mainTC);

        sbSongProgress.setRippleColor(vicTC);
        sbSongProgress.setScrubberColor(mainTC);
        sbSongProgress.setThumbColor(vicTC, mainTC);
        sbSongProgress.setTrackColor(vicTC);

        flFragmentContainer.setBackgroundColor(Color.TRANSPARENT);

        bottomNavigationController.update(vicBC, null);
        bottomNavigationController.themeChange(null, colors);

    }


    /**
     * 更新数值和文字<br>
     * 1 进度条当前进度和最大值<br>
     * 2 进度条左右文字（进度，总时长）<br>
     * 3 歌曲名，艺术家<br>
     */
    private void updateData(int duration, int progress, String title, String arts) {

        tvDuration.setText(Utils.getGenTime(duration));
        tvPlayProgress.setText(Utils.getGenTime(progress));

        sbSongProgress.setMax(duration);
        sbSongProgress.setProgress(progress);

        tsSongName.setText(title);
        tsSongArts.setText(arts);
    }

    @Override
    public void permissionGranted(int requestCode) {
        super.permissionGranted(requestCode);

        mServiceConnection = new PlayServiceConnection(this, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);
    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    protected void initViews() {

        //FIXME test
        playPreference.updateTheme(Theme.VARYING);

        //初始控件
        flRootView = (FrameLayout) findViewById(R.id.play_root);
        tvPlayProgress = (TextView) findViewById(R.id.play_progress);
        tvDuration = (TextView) findViewById(R.id.play_duration);
        sbSongProgress = (DiscreteSeekBar) findViewById(R.id.play_seekBar);

        nameContainer = findViewById(R.id.play_name);
        tsSongName = (TextSwitcher) findViewById(R.id.play_ts_song_name);
        tsSongArts = (TextSwitcher) findViewById(R.id.play_ts_song_arts);

        btPre = (SkipView) findViewById(R.id.play_pre_song);
        btNext = (SkipView) findViewById(R.id.play_next_song);
        btPlay = (PlayView) findViewById(R.id.play_song);
        flFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);

        Theme theme = playPreference.getTheme();
        int mainTextColor = Color.DKGRAY;
        int vicTextColor = Color.GRAY;
        if (theme == Theme.DARK) {
            mainTextColor = getResources().getColor(R.color.theme_dark_main_text);
            vicTextColor = getResources().getColor(R.color.theme_dark_vic_text);
        } else if (theme == WHITE) {
            mainTextColor = getResources().getColor(R.color.theme_white_main_text);
            vicTextColor = getResources().getColor(R.color.theme_white_vic_text);
        }

        //设置属性
        final int finalMainTextColor = mainTextColor;
        tsSongName.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.song_name, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/name.TTF");
                text.setTypeface(tf);
                text.setTextColor(finalMainTextColor);
                return text;
            }
        });
        final int finalVicTextColor = vicTextColor;
        tsSongArts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.song_arts, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/arts.TTF");
                text.setTypeface(tf);
                text.setTextColor(finalVicTextColor);
                return text;
            }
        });

        nameContainer.setOnClickListener(this);
        flFragmentContainer.setOnClickListener(this);
        flFragmentContainer.setOnLongClickListener(this);
        btPre.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPlay.setOnClickListener(this);

        sbSongProgress.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value / 1000;
            }
        });
        sbSongProgress.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            int pos;
            boolean change = false;

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

                pos = value;
                tvPlayProgress.setText(Utils.getGenTime(value));
                change = true;

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                periodicTask.stop();

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                if (change) {
                    try {
                        mServiceConnection.takeControl().seekTo(pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        new ExceptionHandler().handleRemoteException(getApplicationContext(),
                                getApplicationContext().getString(R.string.exception_remote), null
                        );
                    }
                }
                periodicTask.start();
            }
        });

        lyricFragment = new LyricFragment();
        visualizerFragment = new VisualizerFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //顺序 依次叠放
        transaction.add(R.id.play_fragment_container, visualizerFragment, VisualizerFragment.TAG);
        transaction.add(R.id.play_fragment_container, lyricFragment, LyricFragment.TAG);
        transaction.hide(lyricFragment);
        transaction.commit();

        bottomNavigationController = new BottomNavigationController(this, dbMusicoco, mediaManager);
        //更新主题
        themeChange(theme, null);

    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.play_fragment_container:
                if (bottomNavigationController.isListTitleHide())
                    bottomNavigationController.showPlayListTitle();
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre_song:
                try {
                    mServiceConnection.takeControl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
                break;
            case R.id.play_next_song:
                try {
                    mServiceConnection.takeControl().next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
                break;
            case R.id.play_song:

                try {
                    int stat = mServiceConnection.takeControl().status();
                    if (stat == PlayController.STATUS_PLAYING) {
                        mServiceConnection.takeControl().pause();
                    } else {
                        mServiceConnection.takeControl().resume();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
                break;
            case R.id.play_fragment_container:
                if (!bottomNavigationController.isListShowing())
                    bottomNavigationController.show();
                break;
            case R.id.play_name:
                //TODO
                ToastUtils.showToast(this, "click");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnected(ComponentName name, IBinder service) {

        initSelfData();
        visualizerPresenter.initData(null);
        lyricPresenter.initData(null);

    }

    private void initSelfData() {

        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

        btPlay.setEnabled(true);
        btPre.setEnabled(true);
        btNext.setEnabled(true);
        sbSongProgress.setEnabled(true);
        flFragmentContainer.setClickable(true);
        nameContainer.setClickable(true);

        periodicTask = new PeriodicTask(new PeriodicTask.Task() {
            int progress;

            @Override
            public void execute() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress = mServiceConnection.takeControl().getProgress();
                            sbSongProgress.setProgress(progress);
                            tvPlayProgress.setText(Utils.getGenTime(progress));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            new ExceptionHandler().handleRemoteException(getApplicationContext(),
                                    getApplicationContext().getString(R.string.exception_remote), null
                            );
                        }
                    }
                });
            }
        }, 800);

        try {
            List<Song> songs = mServiceConnection.takeControl().getPlayList();
            if (songs == null) { //检查播放列表是否为空
                emptyMediaLibrary();
                updateData(0, 0, "", "");
            } else {
                bottomNavigationController.initData(mServiceConnection.takeControl());
                Song song = mServiceConnection.takeControl().currentSong();
                int index = songs.indexOf(song);
                //服务端在 onCreate 时会回调 songChanged ，PlayActivity 第一次绑定可能接收不到此次回调
                songChanged(song, index, true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }
    }

    @Override
    public void disConnected(ComponentName name) {
        mServiceConnection = null;
        mServiceConnection = new PlayServiceConnection(this, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        int cs[] = new int[4];

        switch (theme) {
            case WHITE:
                cs = com.duan.musicoco.util.ColorUtils.get4WhiteThemeColors(this);
                break;
            case VARYING:
                break;
            case DARK:
            default:
                cs = com.duan.musicoco.util.ColorUtils.get4DarkThemeColors(this);
                break;
        }
        updateColors(cs);
    }

    @Override
    public void emptyMediaLibrary() {
        btPlay.setEnabled(false);
        btPre.setEnabled(false);
        btNext.setEnabled(false);

        sbSongProgress.setEnabled(false);

        flFragmentContainer.setClickable(false);
        nameContainer.setClickable(false);

        bottomNavigationController.emptyMediaLibrary();
    }
}
