package com.duan.musicoco.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.PermissionRequestCallback;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.manager.PermissionManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;

/**
 * Created by DuanJiaNing on 2017/3/21.
 * 检查权限，初始化 app 数据（缓存，数据库...）
 */

public abstract class RootActivity extends AppCompatActivity implements PermissionRequestCallback {

    protected final static String TAG = "RootActivity";

    protected MediaManager mediaManager;
    protected final AppPreference appPreference;
    protected final PlayPreference playPreference;
    protected DBMusicocoController dbMusicoco;
    private PermissionManager permissionManager;
    protected PlayServiceManager playServiceManager;

    private boolean isGranted = false;

    public RootActivity() {
        appPreference = new AppPreference(this);
        playPreference = new PlayPreference(this);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkTheme();

        playServiceManager = PlayServiceManager.getInstance(this);
        permissionManager = PermissionManager.getInstance(this);
        mediaManager = MediaManager.getInstance(getApplicationContext());
        dbMusicoco = new DBMusicocoController(this, true);

        //检查权限
        checkPermission();

    }

    private void checkTheme() {
        //FIXME test
        appPreference.modifyTheme(Theme.WHITE);

        Theme theme = appPreference.getTheme();
        if (theme == Theme.DARK) {
            this.setTheme(R.style.Theme_DARK);
        } else {
            this.setTheme(R.style.Theme_WHITE);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        if (isGranted) {
            // setContentView 后回调
            permissionGranted(PermissionManager.PerMap.CATEGORY_MEDIA_READ);
        }
    }

    protected void checkPermission() {

        String[] ps = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        if (!permissionManager.checkPermission(ps)) {
            PermissionManager.PerMap perMap = new PermissionManager.PerMap(
                    getString(R.string.permission_media_read),
                    getResources().getString(R.string.permission_required),
                    PermissionManager.PerMap.CATEGORY_MEDIA_READ,
                    ps);
            permissionManager.showPermissionRequestTip(perMap, this, new PermissionManager.OnPermissionRequestRefuse() {
                @Override
                public void onRefuse() {
                    finish();
                }
            });
        } else {
            isGranted = true;
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
        moveTaskToBack(true);
    }

    //控件的初始化在获得权限后开始
    //绑定服务应在 initView 完成之后绑定
    @Override
    @CallSuper
    public void permissionGranted(int requestCode) {
        prepareData();
        initAppDataIfNeed();
        initChildViews();
    }

    private void prepareData() {
        mediaManager.refreshData();
    }

    private void initChildViews() {
        initViews();
    }

    private void initAppDataIfNeed() {
        if (appPreference.appOpenTimes() == 0) {
            Init init = new Init();
            init.initAlbumVisualizerImageCache(this);
            init.initMusicocoDB(this, mediaManager);
            mediaManager.scanSdCard(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbMusicoco != null) {
            dbMusicoco.close();
        }
    }

    protected abstract void initViews();
}
