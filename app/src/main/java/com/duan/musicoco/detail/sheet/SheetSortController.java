package com.duan.musicoco.detail.sheet;


import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by DuanJiaNing on 2019/4/6.
 */
public class SheetSortController extends ItemTouchHelper.Callback {

    private boolean enable;
    private OnDragDoneListener onDragDoneListener;
    private OnItemSwapListener onItemSwapListener;

    public void setOnItemSwapListener(OnItemSwapListener onItemSwapListener) {
        this.onItemSwapListener = onItemSwapListener;
    }

    public interface OnDragDoneListener {
        void dragDone(RecyclerView.ViewHolder holder);
    }

    public interface OnItemSwapListener {
        void swap(int posFrom, int posTo);
    }

    public void setOnDragDoneListener(OnDragDoneListener onDragDoneListener) {
        this.onDragDoneListener = onDragDoneListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 滑动或者拖拽的方向，上下左右

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        int dragFlags;
        if (manager instanceof GridLayoutManager || manager instanceof StaggeredGridLayoutManager) {
            //网格布局管理器允许上下左右拖动
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        } else {
            //其他布局管理器允许上下拖动
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }

        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 拖拽item移动时产生回调

        // 更新顺序临时数据，交换动画
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        if (onItemSwapListener != null) {
            onItemSwapListener.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 滑动删除时回调
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // 是否可以长按拖拽
        return enable;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // 是否可以滑动删除
        return super.isItemViewSwipeEnabled();
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (onDragDoneListener != null) {
            onDragDoneListener.dragDone(viewHolder);
        }

    }
}
