package com.duan.musicoco.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;

// 全部音乐
// 最近播放
// 收藏

//歌单....

public class MainActivity extends RootActivity {

    private IBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(null, null);

    }

    @Override
    public void onBackPressed() {
    }

    public void initViews(@Nullable View view, @Nullable Object obj) {

    }

    @Override
    public void songChanged(Song song, int index) {

    }

    @Override
    public void startPlay(Song song, int index, int status) {

    }

    @Override
    public void stopPlay(Song song, int index, int status) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void disConnected() {

    }

    @Override
    public void permissionDenied(int requestCode) {

    }
}
