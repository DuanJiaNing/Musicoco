package com.duan.musicoco.app;

/**
 * Created by DuanJiaNing on 2017/6/24.
 */

interface OnPermissionGranted {

    /**
     * 检查权限，权限已经取得时直接回调该方法，否则回调 Activity 的 onRequestPermissionsResult 方法
     */
    void permissionGranted();
}
