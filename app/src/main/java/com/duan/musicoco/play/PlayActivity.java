package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements Contract.View {

    private PlayServiceConnection mServiceConnection;

    private PlayServiceManager mServiceManager;

    private MediaManager mediaManager;

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
    public void setPresenter(BasePresenter presenter) {

    }

    @Override
    public void initViews(@Nullable View view) {

    }

    @Override
    public void songChanged(Song song, int index) {

        SongInfo info = mediaManager.getSongInfo(song, this);
        Log.i(TAG, "songChanged: " + song.path + " index=" + index);

    }

    @Override
    public void startPlay(Song song, int index) {
        Log.i(TAG, "startPlay: " + song.path + " index=" + index);
        final View vi = findViewById(R.id.play_main_bg);
        int color = ColorUtils.getRandomBrunetColor();
        vi.setBackgroundColor(color);

    }

    @Override
    public void stopPlay(Song song, int index) {
        Log.i(TAG, "stopPlay: " + song.path + " index=" + index);
    }

    @Override
    public void onConnected() {
        try {
            mServiceConnection.takeControl().setPlayMode(PlayController.MODE_RANDOM);
            mServiceConnection.takeControl().playByIndex(7);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
