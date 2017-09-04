package com.duan.musicoco.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.PermissionRequestCallback;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.manager.PermissionManager;
import com.duan.musicoco.app.manager.PlayServiceManager;

/**
 * Created by DuanJiaNing on 2017/3/21.
 */

public abstract class InspectActivity extends RootActivity implements PermissionRequestCallback {

    protected final static String TAG = "InspectActivity";

    protected MediaManager mediaManager;
    private PermissionManager permissionManager;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = PermissionManager.getInstance();
        mediaManager = MediaManager.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void checkPermission() {

        String[] ps = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (!permissionManager.checkPermission(this, ps)) {
            PermissionManager.PerMap perMap = new PermissionManager.PerMap(
                    getString(R.string.permission_media_read),
                    getResources().getString(R.string.permission_required),
                    PermissionManager.PerMap.CATEGORY_MEDIA_READ,
                    ps);

            permissionManager.showPermissionRequestTip(perMap, this, new PermissionManager.OnPermissionRequestRefuse() {
                @Override
                public void onRefuse() {
                    permissionDenied(PermissionManager.PerMap.CATEGORY_MEDIA_READ);
                }
            });

        } else {
            permissionGranted(PermissionManager.PerMap.CATEGORY_MEDIA_READ);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.PerMap.CATEGORY_MEDIA_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted(requestCode);
            } else {
                permissionDenied(requestCode);
            }
        }
    }

    protected void prepareData() {
        mediaManager.refreshData(this);
    }

    protected void initAppDataIfNeed() {
        if (appPreference.appOpenTimes() == 0) {
            Init.initAlbumVisualizerImageCache(this);
            Init.initMusicocoDB(this, mediaManager);
//            mediaManager.scanSdCard(this,null);
        }
    }

    /**
     * 启动服务，应确保获得文件读写权限后再启动，启动服务后再绑定，这样即使绑定这解除绑定，
     * 服务端也能继续运行 ？？？
     */
    protected void startService() {
        PlayServiceManager.startPlayService(this);
    }

}
