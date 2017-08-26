package com.duan.musicoco.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * Created by DuanJiaNing on 2017/7/17.
 * 以该 ViewGroup 作为父容器的视图，视图中有 ListView 时，ListView 滑动到顶部时继续下拉会触发回调（如果设置了监听的话）
 * 没有 ListView 或不在 ListView 范围内下滑时，也会触发下拉回调
 */
// UPDATE: 2017/8/26 更新 没有实现功能
public class PullDownLinearLayout extends LinearLayout {

    private AbsListView listView;
    private float lastY = 0;
    private float lastYForListView = 0;
    private final int touchSlop;
    private OnPullDownListener onPullDownListener;
    private boolean listViewExist;

    public interface OnPullDownListener {
        void pullDownStart(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView);

        void pullDown(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView);

        void pullDownSettle(MotionEvent event, PullDownLinearLayout view, @Nullable AbsListView listView);
    }

    public PullDownLinearLayout(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public PullDownLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullDownLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullDownLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnPullDownListener(OnPullDownListener pullDownListener) {
        this.onPullDownListener = pullDownListener;
    }

    public void isListViewExist(boolean exist) {
        listViewExist = exist;
    }

    public void setListView(AbsListView listView) {
        if (-1 == this.indexOfChild(listView)) {
            return;
        }

        this.listView = listView;
        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getY();
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //手指往下滑，列表往下滚，
                    if ((y - lastYForListView) > touchSlop) {
                        //此时 ListView 接受了事件序列，但用户在下滑列表，
                        // 此时需要在该次事件序列下一个事件到达时再次检查是否需要截断 onInterceptTouchEvent
                        PullDownLinearLayout.this.requestDisallowInterceptTouchEvent(false);
                        lastYForListView = y;
                    }
                }
                return false;
            }
        });
    }

    private boolean isTop() {
        if (listView == null) {
            return false;
        }

        if (listView.getFirstVisiblePosition() == 0) {
            View topItem = listView.getChildAt(0);
            if (topItem != null) {
                int y = topItem.getTop();
                if (y == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercepted = false;

        float y = ev.getY();
        float x = ev.getX();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                lastYForListView = y;
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if ((y - lastY) > touchSlop) { //手指往下滑，列表往下滚

                    if (listView != null) {

                        if (x > listView.getLeft() && x < listView.getRight() &&
                                y > listView.getTop() && y < listView.getBottom()) {
                            //在 ListView 中滑动
                            if (isTop()) { //滑动到顶部
                                intercepted = true;
                            } else {
                                intercepted = false;
                            }
                        } else {
                            //在容器中列表以外的部分滑动
                            intercepted = true;
                        }
                    } else {
                        intercepted = !listViewExist;
                    }

                } else { // 手指往上滑
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }

        return intercepted;
    }

    private boolean isStart = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            if (isStart) {
                isStart = false;
                if (onPullDownListener != null) {
                    onPullDownListener.pullDownStart(event, this, listView);
                }
            }
            if (onPullDownListener != null) {
                onPullDownListener.pullDown(event, this, listView);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            isStart = true;
            if (onPullDownListener != null) {
                onPullDownListener.pullDownSettle(event, this, listView);
            }
        }

        //ACTION_DOWN 事件时不拦截，点击空白处时事件没人处理最后会交回给自己，此时要让接下来的事件继续交给自己
        return false;
    }
}
