package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.ViewPagerAdapter;
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

public class PlayActivity extends RootActivity implements ViewContract, View.OnClickListener {

    private final PlayServiceConnection mServiceConnection;
    private final PlayServiceManager mServiceManager;
    private final MediaManager mediaManager;

    private VisualizerFragment visualizerFragment;
    private ListFragment listFragment;
    private LyricFragment lyricFragment;

    private VisualizerPresenter visualizerPresenter;
    private ListPresenter listPresenter;
    private LyricPresenter lyricPresenter;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;

    private Chronometer mPlayProgress;
    private TextView mDuration;

    private SeekBar mSeekBar;

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

        //检查权限
        checkPermission();

        initViews(null);

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

    }

    @Override
    public void startPlay(Song song, int index) {

    }

    @Override
    public void stopPlay(Song song, int index) {

    }

    @Override
    public void onConnected() {

        try {
            mServiceConnection.takeControl().setPlayMode(PlayController.MODE_LIST_LOOP);
            mServiceConnection.takeControl().playByIndex(1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {

    }

    @Override
    public void initViews(@Nullable View view) {
        mPlayProgress = (Chronometer) findViewById(R.id.play_progress);
        mDuration = (TextView) findViewById(R.id.play_duration);
        mSeekBar = (SeekBar) findViewById(R.id.play_seekBar);
        ((ImageButton) findViewById(R.id.play_pre_song)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.play_next_song)).setOnClickListener(this);


        visualizerFragment = new VisualizerFragment();
        listFragment = new ListFragment();
        lyricFragment = new LyricFragment();

        visualizerPresenter = new VisualizerPresenter(this, visualizerFragment);
        listPresenter = new ListPresenter(this, listFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment);

        mViewPager = (ViewPager) findViewById(R.id.play_viewPager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), visualizerFragment, listFragment, lyricFragment);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(1);

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
        }
    }
}
