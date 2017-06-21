package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.fragment.album.VisualizerFragment;
import com.duan.musicoco.fragment.album.VisualizerPresenter;
import com.duan.musicoco.fragment.lyric.LyricFragment;
import com.duan.musicoco.fragment.lyric.LyricPresenter;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.Util;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements ActivityViewContract, View.OnClickListener {

    private final PlayServiceConnection mServiceConnection;
    private MediaManager mediaManager;

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;

    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private LinearLayout rootView;

    private TextView tvPlayProgress;
    private TextView tvDuration;

    private TextSwitcher tsSongName;
    private TextSwitcher tsSongArts;

    private DiscreteSeekBar sbSongProgress;

    private PlayView btPlay;
    private SkipView btPre;
    private SkipView btNext;
    private ImageButton btMore;

    private FragmentManager fragmentManager;

    private boolean isFragmentAniming = false;

    private PlayPreference playPreference;

    private Song currentSong;
    private int currentIndex;
    private int switchTo = 1; // 1 为下一曲，0 为上一曲

    public PlayActivity() {
        mServiceConnection = new PlayServiceConnection(this, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mediaManager = MediaManager.getInstance(getApplicationContext());
        playPreference = new PlayPreference(getApplicationContext());

        new Thread() {
            @Override
            public void run() {
                //FIXME 耗时
                mediaManager.refreshData();
            }
        }.start();

        initViews(null, null);

        //检查权限
        checkPermission();

    }

    private void checkPermission() {
        String[] ps = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        if (!PermissionManager.checkPermission(this, ps)) {
            PermissionManager.PerMap perMap = new PermissionManager.PerMap("存储读取权限",
                    getResources().getString(R.string.per_rw_storage),
                    PermissionManager.PerMap.CATEGORY_MEDIA_READ, ps
            );
            PermissionManager.requestPermission(perMap, this);
        } else {
            PlayServiceManager.bindService(this, mServiceConnection);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.PerMap.CATEGORY_MEDIA_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                PlayServiceManager.bindService(this, mServiceConnection);
            } else {
                finish();
            }
        }
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
        if (currentSong != null) {
            String path = currentSong.path;
            int index = currentIndex;
            int pro = sbSongProgress.getProgress();
            playPreference.updateCurrentSong(new PlayPreference.CurrentSong(path, pro, index));
        }
    }

    @Override
    public void songChanged(Song song, int index) {

        currentSong = song;
        currentIndex = index;

        SongInfo info = mediaManager.getSongInfo(song);
        int duration = (int) info.getDuration();
        tvDuration.setText(Util.getGenTime(duration));
        tvPlayProgress.setText("00:00");
        sbSongProgress.setMax(duration);

        tsSongName.setText(info.getTitle());
        tsSongArts.setText(info.getArtist());

        visualizerPresenter.songChanged(song, switchTo);

        updateColors(visualizerFragment.getCurrColors());
    }

    /**
     * 0 暗的活力颜色 主背景色<br>
     * 1 暗的活力颜色 对应适合的字体颜色 主字体色<br>
     * 2 暗的柔和颜色 辅背景色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色 辅字体色<br>
     */
    private void updateColors(int[] colors) {
        if (colors.length != 4)
            return;

        ((TextView) (tsSongName.getCurrentView())).setTextColor(colors[1]);
        ((TextView) (tsSongArts.getCurrentView())).setTextColor(colors[3]);

        rootView.setBackgroundColor(colors[0]);

        tvPlayProgress.setTextColor(colors[3]);
        tvDuration.setTextColor(colors[3]);

        btPre.setTriangleColor(colors[1]);
        btNext.setTriangleColor(colors[1]);

        btPlay.setTriangleColor(colors[1]);
        btPlay.setStrokeColor(colors[1]);
        btPlay.setPauseLineColor(colors[1]);

        sbSongProgress.setRippleColor(colors[3]);
        sbSongProgress.setScrubberColor(colors[1]);
        sbSongProgress.setThumbColor(colors[3], colors[1]);
        sbSongProgress.setTrackColor(colors[3]);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            btMore.getDrawable().setTint(colors[1]);
//        }

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        startUpdateProgressTask();
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        cancelUpdateProgressTask();

    }

    //设置主题风格
    public void setThemeMode(Theme themeMode) {

        int colors[] = new int[4];

        switch (themeMode) {
            case WHITE:
                break;
            case VARYING:
                //TODO
                break;
            case DARKGOLD:
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colors[0] = getColor(R.color.colorAccent); //主背景色
                    colors[1] = getColor(R.color.colorPrimary); // 主字体色
                    colors[2] = colors[0];
                    colors[3] = getColor(R.color.colorPrimaryDark); // 辅字体色
                } else {
                    colors[0] = getResources().getColor(R.color.colorAccent);
                    colors[1] = getResources().getColor(R.color.colorPrimary);
                    colors[2] = colors[0];
                    colors[3] = getResources().getColor(R.color.colorPrimaryDark);
                }
                break;
        }
        updateColors(colors);
    }

    private TimerTask progressUpdateTask;

    public void cancelUpdateProgressTask() {
        if (progressUpdateTask != null)
            progressUpdateTask.cancel();
    }

    public void startUpdateProgressTask() {
        Timer timer = new Timer();
        progressUpdateTask = new TimerTask() {
            int progress;

            @Override
            public void run() {
                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress = mServiceConnection.takeControl().getProgress();
                            sbSongProgress.setProgress(progress);
                            tvPlayProgress.setText(Util.getGenTime(progress));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(progressUpdateTask, 0, 800);
    }

    //服务成功连接之后才初始化数据
    @Override
    public void onConnected() {

        initSelfData();

        visualizerPresenter.initData(null);
        lyricPresenter.initData(null);

    }

    @Override
    public void disConnected() {

    }

    private void initSelfData() {

        Song song = null;
        PlayPreference.CurrentSong cur = playPreference.getCurrentSong();
        if (cur != null && cur.path != null)
            song = new Song(cur.path);

        SongInfo info = null;

        if (song == null) {
            info = mediaManager.getSongInfoList().get(0);
        } else
            info = mediaManager.getSongInfo(song);

        int duration;
        int progress;
        String title;
        String arts;
        //不是第一次访问配置文件
        if (info != null) {

            duration = (int) info.getDuration();
            title = info.getTitle();
            arts = info.getArtist();
            progress = cur.progress;

        } else { // 没有歌曲

            duration = 0;
            progress = 0;
            title = arts = "";

            btPlay.setEnabled(false);
            btPre.setEnabled(false);
            btNext.setEnabled(false);
            sbSongProgress.setEnabled(false);
        }

        tvDuration.setText(Util.getGenTime(duration));
        tvPlayProgress.setText(Util.getGenTime(progress));
        sbSongProgress.setMax(duration);
        tsSongName.setText(title);
        tsSongArts.setText(arts);

        try {

            if (progress != 0)
                mServiceConnection.takeControl().seekTo(progress);

            boolean st = mServiceConnection.takeControl().status() == PlayController.STATUS_PLAYING;
            btPlay.setPlayStatus(st);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

    }

    @Override
    public void initViews(@Nullable View view, Object obj) {

        //初始控件
        rootView = (LinearLayout) findViewById(R.id.play_root);
        tvPlayProgress = (TextView) findViewById(R.id.play_progress);
        tvDuration = (TextView) findViewById(R.id.play_duration);
        sbSongProgress = (DiscreteSeekBar) findViewById(R.id.play_seekBar);
        tsSongName = (TextSwitcher) findViewById(R.id.play_ts_song_name);
        tsSongArts = (TextSwitcher) findViewById(R.id.play_ts_song_arts);
        btPre = (SkipView) findViewById(R.id.play_pre_song);
        btNext = (SkipView) findViewById(R.id.play_next_song);
        btPlay = (PlayView) findViewById(R.id.play_song);
        btMore = (ImageButton) findViewById(R.id.play_more);
        //设置属性
        tsSongName.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_name, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/name.TTF");
                text.setTypeface(tf);
                return text;
            }
        });
        tsSongArts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_arts, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/arts.TTF");
                text.setTypeface(tf);
                return text;
            }
        });

        //设置主题
        setThemeMode(new AppPreference(this).getTheme());

        btPre.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPlay.setOnClickListener(this);
        btMore.setOnClickListener(this);
        btMore.setEnabled(false);
        btMore.setVisibility(View.INVISIBLE);

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
                tvPlayProgress.setText(Util.getGenTime(value));
                change = true;

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                cancelUpdateProgressTask();

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                if (change) {
                    try {
                        mServiceConnection.takeControl().seekTo(pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                startUpdateProgressTask();
            }
        });

        lyricFragment = new LyricFragment();
        visualizerFragment = new VisualizerFragment();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //顺序 依次叠放
        transaction.add(R.id.play_fragment_container, visualizerFragment, VisualizerFragment.TAG);
        transaction.add(R.id.play_fragment_container, lyricFragment, LyricFragment.TAG);
        transaction.hide(lyricFragment);
        transaction.commit();

    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.findFragmentByTag(LyricFragment.TAG).isVisible())
            hideLyricFragment();
        else
            super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre_song:
                try {
                    switchTo = 0;
                    mServiceConnection.takeControl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_next_song:
                try {
                    switchTo = 1;
                    mServiceConnection.takeControl().next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_song:

                try {
                    int stat = mServiceConnection.takeControl().status();
                    if (stat == PlayController.STATUS_PLAYING) {
                        mServiceConnection.takeControl().pause();
                        visualizerPresenter.stopPlay();
                        ((PlayView) v).setPlayStatus(false);
                    } else {
                        mServiceConnection.takeControl().resume();
                        visualizerPresenter.startPlay();
                        ((PlayView) v).setPlayStatus(true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_more:
                Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    //最上面的一定是 VisualizerFragment
    @Override
    public void showLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(lyricFragment);
        transaction.commit();
    }

    //最上面的一定是 LyricFragment
    @Override
    public void hideLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(visualizerFragment); // 显示但被 LyricFragment 遮挡
        transaction.commit();

    }

}
