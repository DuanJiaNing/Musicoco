package com.duan.musicoco.main.leftnav.imagewall;

import android.os.Bundle;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.preference.ThemeEnum;

public class ImageWallActivity extends RootActivity implements ThemeChangeable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_wall);
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

    }
}
