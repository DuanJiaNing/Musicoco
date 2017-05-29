package com.duan.musicoco.play;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements Contract.View {

    private PlayServiceConnection mServiceConnection;

    private PlayServiceManager mServiceManager;

    private MediaManager mediaManager;

    private Contract.Presenter mPresenter;

    public PlayActivity() {
        mPresenter = new PresenterImpl(this);
        mServiceConnection = new PlayServiceConnection(mPresenter, this);
        mServiceManager = new PlayServiceManager(this, mServiceConnection, mPresenter);
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
            PlayServiceManager.bindService(this,mServiceConnection);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.PerMap.CATEGORY_MEDIA_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获取权限成功", Toast.LENGTH_SHORT).show();
                PlayServiceManager.bindService(this,mServiceConnection);
            } else {
                finish();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection.hasConnected)
            unbindService(mServiceConnection);
    }

    @Override
    public void setPresenter(Contract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void initViews(@Nullable View view) {

    }

    @Override
    public void songChanged(Song song, int index) {
        final View vi = findViewById(R.id.play_main_bg);
        int color = ColorUtils.getRandomBrunetColor();
        vi.setBackgroundColor(color);

        SongInfo info = mediaManager.getSongInfo(song, this);

    }
}
