package com.duan.musicoco.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.duan.musicoco.R;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.play.PlayServiceConnection;

/**
 * Created by DuanJiaNing on 2017/3/21.
 * 检查权限和绑定服务
 */

public abstract class RootActivity extends AppCompatActivity implements PermissionRequestCallback, PlayServiceCallback {

    protected final static String TAG = "RootActivity";

    protected MediaManager mediaManager;

    protected final PlayServiceConnection mServiceConnection;

    protected final PlayPreference playPreference;
    protected final AppPreference appPreference;

    public RootActivity() {
        mServiceConnection = new PlayServiceConnection(this, this);
        playPreference = new PlayPreference(this);
        appPreference = new AppPreference(this);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

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
            permissionGranted(PermissionManager.PerMap.CATEGORY_MEDIA_READ);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.PerMap.CATEGORY_MEDIA_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: 权限获取成功");
                permissionGranted(requestCode);
            } else {
                permissionDenied(requestCode);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    @Override
    @CallSuper
    public void permissionGranted(int requestCode) {

        //FIXME 添加主题切换功能
        appPreference.modifyTheme(Theme.VARYING);

        PlayServiceManager.bindService(this, mServiceConnection);
        mediaManager = MediaManager.getInstance(getApplicationContext());

        new Thread() {
            @Override
            public void run() {
                mediaManager.refreshData();
                new Init().initAlbumVisualizerImageCache(RootActivity.this);
            }
        }.start();
    }

}
