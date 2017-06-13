package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.fragment.album.VisualizerFragment;
import com.duan.musicoco.fragment.album.VisualizerPresenter;
import com.duan.musicoco.list.ListActivity;
import com.duan.musicoco.list.ListPresenter;
import com.duan.musicoco.fragment.lyric.LyricFragment;
import com.duan.musicoco.fragment.lyric.LyricPresenter;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.StringUtil;
import com.duan.musicoco.util.Util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements ActivityViewContract, View.OnClickListener {

    private final PlayServiceConnection mServiceConnection;
    private final PlayServiceManager mServiceManager;
    private final MediaManager mediaManager;

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;

    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private FrameLayout mFragmentContainer;

    private LinearLayout rootView;

    private TextView mPlayProgress;
    private TextView mDuration;

    private TextSwitcher songName;
    private TextSwitcher songArts;

    private SeekBar mSeekBar;

    private ImageButton play;
    private ImageButton pre;
    private ImageButton next;
    private ImageButton more;

    private FragmentManager fragmentManager;

    private int[] playOrPause = {
            R.drawable.ic_play_arrow_white_48dp,
            R.drawable.ic_pause_black_48dp
    };

    private boolean isFragmentAniming = false;

    private int rootColor = Color.WHITE;
    private int viceTextColor = Color.DKGRAY;
    private int mainTextColor = Color.BLACK;

    public PlayActivity() {
        mServiceConnection = new PlayServiceConnection(this, this);
        mServiceManager = new PlayServiceManager(this, mServiceConnection);
        mediaManager = MediaManager.getInstance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //FIXME 耗时
        mediaManager.refreshData(this);

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
                Toast.makeText(this, "获取权限成功", Toast.LENGTH_SHORT).show();
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
    public void songChanged(Song song, int index) {
        visualizerPresenter.songChanged(song);

        SongInfo info = MediaManager.getInstance().getSongInfo(song, this);
        int duration = (int) info.getDuration();
        mDuration.setText(Util.getGenTime(duration));
        mPlayProgress.setText("00:00");
        mSeekBar.setMax(duration);

        songName.setText(info.getTitle());
        songArts.setText(info.getArtist());

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        startUpdateProgressTask();

    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        cancelUpdateProgressTask();

    }

    public void setThemeMode(Theme themeMode) {
        switch (themeMode) {
            case WHITE:
                break;
            case VARYING:
                //TODO
                break;
            case DARKGOLD:
            default:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rootColor = getColor(R.color.colorAccent);
                    viceTextColor = getColor(R.color.colorPrimaryDark);
                    mainTextColor = getColor(R.color.colorPrimary);
                } else {
                    rootColor = getResources().getColor(R.color.colorAccent);
                    viceTextColor = getResources().getColor(R.color.colorPrimaryDark);
                    mainTextColor = getResources().getColor(R.color.colorPrimary);
                }

                rootView.setBackgroundColor(rootColor);
                mPlayProgress.setTextColor(viceTextColor);
                mDuration.setTextColor(viceTextColor);

                break;
        }
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
                            mSeekBar.setProgress(progress);
                            mPlayProgress.setText(Util.getGenTime(progress));

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
        try {
            song = mServiceConnection.takeControl().currentSong();
            if (song == null) {
                mediaResIsEmpty();
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        SongInfo info = MediaManager.getInstance().getSongInfo(song, this);
        int duration = (int) info.getDuration();
        mDuration.setText(Util.getGenTime(duration));
        mPlayProgress.setText("00:00");
        mSeekBar.setMax(duration);
        songName.setText(info.getTitle());
        songArts.setText(info.getArtist());

        try {
            int draw = mServiceConnection.takeControl().status() == PlayController.STATUS_PLAYING ? playOrPause[1] : playOrPause[0];
            play.setImageResource(draw);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

    }

    private void mediaResIsEmpty() {
        mDuration.setText("00:00");
        mPlayProgress.setText("00:00");
        play.setImageResource(playOrPause[0]);

        //FIXME
        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

        play.setEnabled(false);
        pre.setEnabled(false);
        next.setEnabled(false);
        mSeekBar.setEnabled(false);

        songName.setText("");
        songArts.setText("");

    }

    @Override
    public void initViews(@Nullable View view, Object obj) {
        //初始控件
        rootView = (LinearLayout) findViewById(R.id.play_root);
        mPlayProgress = (TextView) findViewById(R.id.play_progress);
        mDuration = (TextView) findViewById(R.id.play_duration);
        mSeekBar = (SeekBar) findViewById(R.id.play_seekBar);
        songName = (TextSwitcher) findViewById(R.id.play_ts_song_name);
        songArts = (TextSwitcher) findViewById(R.id.play_ts_song_arts);
        pre = (ImageButton) findViewById(R.id.play_pre_song);
        next = (ImageButton) findViewById(R.id.play_next_song);
        play = (ImageButton) findViewById(R.id.play_song);
        more = (ImageButton) findViewById(R.id.play_more);
        mFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);

        //设置主题
        setThemeMode(new AppPreference(this).getTheme());

        //设置属性
        songName.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_name, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/name.TTF");
                text.setTypeface(tf);
                text.setTextColor(mainTextColor);
                return text;
            }
        });
        songArts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_arts, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/arts.TTF");
                text.setTypeface(tf);
                text.setTextColor(viceTextColor);
                return text;
            }
        });
        pre.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        more.setOnClickListener(this);
        mFragmentContainer.setOnTouchListener(new View.OnTouchListener() {
            private float y;
            private float dis = 70;
            private int touchTime = 0;

            //FIXME
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        y = event.getY();
                        touchTime = 1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() - y - dis > 0.0001) {
                            if (visualizerFragment.isVisible()) {
                                showLyricFragment();
                                return false;
                            }
                        }

                        touchTime++;
                        return false;
                    case MotionEvent.ACTION_UP:

                        //用户的点击会触发多个中间事件
                        if (touchTime < 6)
                            onClick(v);
                        break;
                }
                return false;
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int pos;
            boolean change = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pos = progress;
                mPlayProgress.setText(Util.getGenTime(progress));
                change = true;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cancelUpdateProgressTask();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
                    mServiceConnection.takeControl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_next_song:
                try {
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
                        ((ImageButton) v).setImageResource(playOrPause[0]);
                    } else {
                        mServiceConnection.takeControl().resume();
                        visualizerPresenter.startPlay();
                        ((ImageButton) v).setImageResource(playOrPause[1]);
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
