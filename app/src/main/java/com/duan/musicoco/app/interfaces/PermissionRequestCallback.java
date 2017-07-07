package com.duan.musicoco.app.interfaces;

/**
 * Created by DuanJiaNing on 2017/6/24.
 */

public interface PermissionRequestCallback {

    /**
     * 检查权限，成功获取权限后回调，不要在这里操作控件，因为此时可能 setContentView 还未执行
     * @param requestCode
     */
    void permissionGranted(int requestCode);

    /**
     * 检查权限，获取权限失败后回调
     * @param requestCode
     */
    void permissionDenied(int requestCode);


}
