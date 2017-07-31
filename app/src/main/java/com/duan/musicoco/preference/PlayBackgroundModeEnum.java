package com.duan.musicoco.preference;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * 以下三种模式只在 PlayPreference 的 them 为 VARYING 时有效
 */

public enum PlayBackgroundModeEnum {

    COLOR,  // 纯色

    PICTUREWITHMASK, // 带遮罩(黑边)的专辑图片

    /**
     * 虚化的专辑图片
     */
    PICTUREWITHBLUR,
}
