package com.duan.musicoco.util;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by DuanJiaNing on 2017/7/16.
 */
//FIXME
public class PullDownListHelper implements
        AbsListView.OnScrollListener,
        View.OnTouchListener {

    private boolean pullDownNow = false;

    private View contentView;
    private AbsListView listView;

    public PullDownListHelper(View contentView, AbsListView listView) {
        this.contentView = contentView;
        this.listView = listView;

        listView.setOnScrollListener(this);
        contentView.setOnTouchListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            View topItem = view.getChildAt(0);
            if (topItem != null) {
                int y = topItem.getTop();
                if (y == 0) {
                    pullDownNow = true;
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (pullDownNow) {
            Log.i("musicoco", "onTouch: x=" + event.getX() + " y=" + event.getY());
            return true;
        } else {
            return false;
        }
    }
}
