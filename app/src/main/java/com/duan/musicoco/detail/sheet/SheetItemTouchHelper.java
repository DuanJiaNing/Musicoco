package com.duan.musicoco.detail.sheet;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by DuanJiaNing on 2019/4/6.
 */
public class SheetItemTouchHelper extends ItemTouchHelper.Callback {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 滑动或者拖拽的方向，上下左右
        return 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 拖拽item移动时产生回调
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 滑动删除时回调
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // 是否可以长按拖拽
        return super.isLongPressDragEnabled();
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // 是否可以滑动删除
        return super.isItemViewSwipeEnabled();
    }
}
