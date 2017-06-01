package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.fragment.album.VisualizerFragment;
import com.duan.musicoco.fragment.album.VisualizerPresenter;
import com.duan.musicoco.fragment.list.ListFragment;
import com.duan.musicoco.fragment.list.ListPresenter;
import com.duan.musicoco.fragment.lyric.LyricFragment;
import com.duan.musicoco.fragment.lyric.LyricPresenter;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.service.PlayController;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements ActivityViewContract, View.OnClickListener {

    private final PlayServiceConnection mServiceConnection;
    private final PlayServiceManager mServiceManager;
    private final MediaManager mediaManager;

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;
    private ListFragment listFragment;

    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;
    private ListPresenter listPresenter;

    private FrameLayout mFragmentContainer;

    private Chronometer mPlayProgress;
    private TextView mDuration;

    private SeekBar mSeekBar;

    private boolean isPlaying = false;
    private ImageButton play;

    private FragmentManager fragmentManager;

    private int[] playOrPause = {
            R.drawable.ic_play_arrow_white_48dp,
            R.drawable.ic_pause_black_48dp
    };

    private boolean isFragmentAniming = false;
    private Fragment currentShowing;

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
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        visualizerPresenter.changeSong(song);
    }

    @Override
    public void startPlay(Song song, int index) {
        visualizerPresenter.changeSong(song);
    }

    @Override
    public void stopPlay(Song song, int index) {

    }

    //服务成功连接之后才初始化数据
    @Override
    public void onConnected() {

        initSelfData();

        try {
            visualizerPresenter.initData(mServiceConnection.takeControl().currentSong());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void initSelfData() {

        try {
            int draw = mServiceConnection.takeControl().status() == PlayController.STATUS_PLAYING ? playOrPause[1] : playOrPause[0];
            play.setImageResource(draw);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        visualizerPresenter = new VisualizerPresenter(this, visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);
        listPresenter = new ListPresenter(this, listFragment, this);

    }


    @Override
    public void initViews(@Nullable View view, Object obj) {
        mPlayProgress = (Chronometer) findViewById(R.id.play_progress);
        mDuration = (TextView) findViewById(R.id.play_duration);
        mSeekBar = (SeekBar) findViewById(R.id.play_seekBar);
        findViewById(R.id.play_pre_song).setOnClickListener(this);
        findViewById(R.id.play_next_song).setOnClickListener(this);
        findViewById(R.id.play_show_list).setOnClickListener(this);
        play = (ImageButton) findViewById(R.id.play_song);
        play.setOnClickListener(this);
        mFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);
        mFragmentContainer.setOnClickListener(this);
        mFragmentContainer.setOnTouchListener(new View.OnTouchListener() {
            private float y;
            private float dis = 70;
            private int touchTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //FIXME 快速双击时：动画直接到底并直接开始 hide 动画

                        if (listFragment.isVisible())
                            return false;

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

        lyricFragment = new LyricFragment();
        visualizerFragment = new VisualizerFragment();
        listFragment = new ListFragment();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //顺序 依次叠放
        transaction.add(R.id.play_fragment_container, visualizerFragment, VisualizerFragment.TAG);
        transaction.add(R.id.play_fragment_container, lyricFragment, LyricFragment.TAG);
        transaction.add(R.id.play_fragment_container, listFragment, ListFragment.TAG);
        transaction.hide(lyricFragment);
        transaction.hide(listFragment);
        transaction.commit();
        currentShowing = visualizerFragment;

    }


    @Override
    public void setPresenter(BasePresenter presenter) {

    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.findFragmentByTag(ListFragment.TAG).isVisible())
            hideListFragment();
        else if (fragmentManager.findFragmentByTag(LyricFragment.TAG).isVisible())
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
            case R.id.play_fragment_container:

                if (fragmentManager.findFragmentByTag(LyricFragment.TAG).isHidden()) {
                    showLyricFragment();
                } else {
                    hideLyricFragment();
                }
                break;
            case R.id.play_show_list:

                if (fragmentManager.findFragmentByTag(ListFragment.TAG).isHidden()) {
                    showListFragment();
                } else
                    hideListFragment();
                break;
        }
    }

    //最上面的一定是 ListFragment
    private void hideListFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(currentShowing);
        transaction.commit();

        listFragment.hideFragment(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFragmentAniming = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFragmentAniming = false;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(listFragment);
                transaction.commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //最上面的可能是 visualizerFragment 或是 LyricFragment
    private void showListFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(listFragment);
        transaction.commit();

        listFragment.showFragment(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFragmentAniming = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFragmentAniming = false;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(lyricFragment);
                transaction.hide(visualizerFragment);
                transaction.commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //最上面的一定是 VisualizerFragment
    private void showLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(lyricFragment);
        transaction.commit();
        lyricFragment.showFragment(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFragmentAniming = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFragmentAniming = false;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(visualizerFragment);
                transaction.commit();

                currentShowing = lyricFragment;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //最上面的一定是 LyricFragment
    private void hideLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(visualizerFragment); // 显示但被 LyricFragment 遮挡
        transaction.commit();
        lyricFragment.hideFragment(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isFragmentAniming = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFragmentAniming = false;
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(lyricFragment);
                transaction.commit();

                currentShowing = visualizerFragment;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
