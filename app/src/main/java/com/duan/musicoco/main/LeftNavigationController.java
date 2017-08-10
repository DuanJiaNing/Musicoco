package com.duan.musicoco.main;

import android.app.Activity;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

/**
 * Created by DuanJiaNing on 2017/8/10.
 */

public class LeftNavigationController implements ViewVisibilityChangeable {

    private final Activity activity;

    private FlowingDrawer flowingDrawer;

    public LeftNavigationController(Activity activity) {
        this.activity = activity;
    }

    public void initViews() {
        flowingDrawer = (FlowingDrawer) activity.findViewById(R.id.drawer_layout);
        flowingDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_FULLSCREEN);
        flowingDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    Log.i("MainActivity", "Drawer STATE_CLOSED");
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
            }
        });

    }

    public void initData() {

    }

    public boolean onBackPressed() {
        if (visible()) {
            hide();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void show() {
        flowingDrawer.openMenu(true);
    }

    @Override
    public void hide() {
        flowingDrawer.closeMenu(true);
    }

    @Override
    public boolean visible() {
        return flowingDrawer.isMenuVisible();
    }
}
