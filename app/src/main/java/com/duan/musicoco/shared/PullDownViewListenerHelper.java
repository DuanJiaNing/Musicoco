package com.duan.musicoco.shared;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.duan.musicoco.view.PullDownLinearLayout;

/**
 * Created by DuanJiaNing on 2017/7/17.
 */

public abstract class PullDownViewListenerHelper implements PullDownLinearLayout.OnPullDownListener {

    private static final String TAG = "menu_main";
    private View container;
    private int originalY;
    private int distance;
    private int currentY;

    public PullDownViewListenerHelper(Context context, View container) {
        this.container = container;
        originalY = (int) container.getY();
    }

    @Override
    public void pullDownStart(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView) {
        distance = (int) event.getY();
        currentY = (int) container.getY();
    }

    @Override
    public void pullDown(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView) {

        currentY = (int) (event.getY() - distance);
        if (!isOutOfBoundary()) {
            container.setY(currentY);
        } else {
            container.setY(originalY);
        }
    }

    @Override
    public void pullDownSettle(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView) {
        pullDownFinish(currentY, event, view, listView);
    }

    public abstract void pullDownFinish(int currentY, MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView);

    public boolean isOutOfBoundary() {
        return currentY < originalY;
    }
}
