package com.duan.musicoco.detail.sheet;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by DuanJiaNing on 2017/7/25.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements ThemeChangeable {

    private final int sheetID;
    private boolean isCurrentSheetPlaying = false;
    private int currentIndex;
    private Activity activity;

    private final List<DataHolder> data;

    private int mainTC;
    private int vicTC;
    private int accentC;

    private OnMoreClickListener moreClickListener;
    private OnItemClickListener itemClickListener;
    private View.OnLongClickListener longClickListener;
    private OnItemCheckStatusChangedListener checkStatusChangedListener;

    private ListMode listMode = ListMode.NORMAL;
    private boolean multiSelectModeSwitchAnimEnable = false;
    private List<Integer> checksIndex = null;

    public SongAdapter(Activity activity, List<DataHolder> data, int id) {
        this.activity = activity;
        this.data = data;
        this.sheetID = id;

    }

    public ListMode getListMode() {
        return listMode;
    }

    public void checkAll() {
        if (checksIndex != null) {
            for (int i = 0; i < getItemCount(); i++) {
                if (!checksIndex.contains(i)) {
                    checksIndex.add(i);
                }
            }
            setMultiSelectModeSwitchAnimEnable(false);
            notifyDataSetChanged();

            if (checkStatusChangedListener != null) {
                checkStatusChangedListener.itemCheckChanged(0, true);
            }
        }
    }

    public void clearAllCheck() {
        if (checksIndex != null) {
            checksIndex.clear();
            setMultiSelectModeSwitchAnimEnable(false);
            notifyDataSetChanged();

            if (checkStatusChangedListener != null) {
                checkStatusChangedListener.itemCheckChanged(0, true);
            }
        }
    }

    public interface OnMoreClickListener {
        void onMore(ViewHolder view, DataHolder data, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder view, DataHolder data, int position);
    }

    public interface OnItemCheckStatusChangedListener {
        void itemCheckChanged(int position, boolean check);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.moreClickListener = listener;
    }

    public void setOnCheckStatusChangedListener(OnItemCheckStatusChangedListener listener) {
        this.checkStatusChangedListener = listener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
        this.longClickListener = listener;
    }

    public DataHolder getItem(int pos) {
        return data.get(pos);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.sheet_songs_item, parent, false);
        return new ViewHolder(view);
    }

    public void updateListMode(ListMode listMode) {
        switch (listMode) {
            case SORT:
                // do nothing
                break;
            case NORMAL: {
                setMultiSelectModeSwitchAnimEnable(this.listMode == ListMode.MULTISELECTION);
                checksIndex = null;
                break;
            }
            case MULTISELECTION: {
                setMultiSelectModeSwitchAnimEnable(true);
                if (checksIndex == null) {
                    checksIndex = new ArrayList<>();
                } else {
                    checksIndex.clear();
                }
                break;
            }
        }

        this.listMode = listMode;
        notifyDataSetChanged();
    }

    public void setMultiSelectModeSwitchAnimEnable(boolean enable) {
        this.multiSelectModeSwitchAnimEnable = enable;
    }

    private int moreBtWidth;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DataHolder dataHolder = getItem(position);
        SongInfo info = dataHolder.info;
        loadDataAndView(dataHolder, holder, position);

        int tr = holder.more.getWidth();
        // 注意可能获取到值 0
        if (moreBtWidth == 0 && tr != 0) {
            moreBtWidth = tr;
        }

        switch (listMode) {
            case MULTISELECTION: {
                startMultiSelectModeSwitchAnimIfNeed(0, moreBtWidth, holder);
                holder.check.setVisibility(View.VISIBLE);
                handleSelectModeListener(holder, position);
                break;
            }
            case NORMAL: {
                startMultiSelectModeSwitchAnimIfNeed(moreBtWidth, 0, holder);
                startSortModeSwitchAnim(0.9f, 1, holder);

                holder.check.setVisibility(View.GONE);
                handleNormalListeners(info, holder, dataHolder, position);
                break;
            }
            case SORT: {
                startSortModeSwitchAnim(1, 0.9f, holder);
                break;
            }
        }

    }

    private void startMultiSelectModeSwitchAnimIfNeed(int from, final int to, final ViewHolder holder) {
        if (multiSelectModeSwitchAnimEnable) {
            ValueAnimator anim = ObjectAnimator.ofInt(from, to);
            int dur = activity.getResources().getInteger(R.integer.anim_default_duration);
            anim.setDuration(dur);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int va = (int) animation.getAnimatedValue();
                    for (View v : holder.views) {
                        if (v != null) {
                            v.setTranslationX(va);
                        }
                    }
                }
            });
            anim.start();
        } else {
            for (View v : holder.views) {
                if (v != null) {
                    v.setTranslationX(to);
                }
            }
        }
    }

    private void startSortModeSwitchAnim(float from, final float to, final ViewHolder holder) {
        ValueAnimator anim = ObjectAnimator.ofFloat(from, to);
        int dur = activity.getResources().getInteger(R.integer.anim_default_duration);
        anim.setDuration(dur);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                holder.itemView.setScaleX(va);
                holder.itemView.setScaleY(va);
            }
        });
        anim.start();
    }

    public List<Integer> getCheckItemsIndex() {
        return checksIndex;
    }

    private void handleSelectModeListener(final ViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.check.toggle();

                if (checksIndex == null) {
                    checksIndex = new ArrayList<Integer>();
                }

                Integer ele = position;
                if (holder.check.isChecked()) {
                    if (!checksIndex.contains(ele)) {
                        checksIndex.add(ele);
                        if (checkStatusChangedListener != null) {
                            checkStatusChangedListener.itemCheckChanged(position, true);
                        }
                    }
                } else {
                    if (checksIndex.contains(ele)) {
                        checksIndex.remove(ele);
                        if (checkStatusChangedListener != null) {
                            checkStatusChangedListener.itemCheckChanged(position, false);
                        }
                    }
                }
            }
        });
        holder.itemView.setLongClickable(false);
    }

    private void handleNormalListeners(SongInfo info, final ViewHolder holder, final DataHolder dataHolder, final int position) {

        if (itemClickListener != null) {
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMultiSelectModeSwitchAnimEnable(false);
                    itemClickListener.onItemClick(holder, dataHolder, position);
                }
            });
        }

        if (moreClickListener != null) {
            holder.more.setTag(info);
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreClickListener.onMore(holder, dataHolder, position);
                }
            });
        }

        if (longClickListener != null) {
            holder.itemView.setLongClickable(true);
            holder.itemView.setOnLongClickListener(longClickListener);
        }
    }

    private void loadDataAndView(DataHolder dataHolder, ViewHolder holder, int position) {
        SongInfo info = dataHolder.info;

        // any mode same
        final RoundedCornersTransformation rtf = new RoundedCornersTransformation(activity, 15, 0);
        ImageView image = holder.image;
        Glide.with(activity)
                .load(info.getAlbum_path())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_song)
                .bitmapTransform(rtf)
                .crossFade()
                .into(image);
        holder.name.setText(info.getTitle());
        holder.arts.setText(info.getArtist());
        holder.duration.setText(StringUtils.getGenTimeMS((int) info.getDuration()));

        // multiselection mode
        holder.check.setChecked(listMode == ListMode.MULTISELECTION && checksIndex.contains(position));

        // sort mode
        if (listMode == ListMode.SORT) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setScaleX(0.9f);
            holder.itemView.setScaleY(0.9f);
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setScaleX(1f);
            holder.itemView.setScaleY(1f);
        }
        holder.number.setText(listMode == ListMode.SORT ? "" : String.valueOf(position + 1));
        holder.more.setImageDrawable(activity.getDrawable(listMode == ListMode.SORT ?
                R.drawable.ic_play_list : R.drawable.ic_more_vert_black_24dp));

        bindStatAndColors(holder, position, dataHolder.isFavorite);
    }

    private void bindStatAndColors(ViewHolder holder, int position, boolean isFavorite) {

        if (isCurrentSheetPlaying && position == currentIndex && listMode == ListMode.NORMAL) {
            setNumberAsImage(true, holder.number, accentC);
        } else {
            setNumberAsImage(false, holder.number, vicTC);
        }

        holder.name.setTextColor(mainTC);
        holder.arts.setTextColor(vicTC);
        holder.duration.setTextColor(vicTC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isFavorite) {
                holder.favorite.getDrawable().setTint(accentC);
            } else {
                holder.favorite.getDrawable().setTint(vicTC);
            }

            holder.more.getDrawable().setTint(vicTC);
        } else {
            holder.favorite.setVisibility(View.GONE);
        }

    }

    private void setNumberAsImage(boolean b, TextView number, int vtc) {

        if (b) {

            Drawable drawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = activity.getDrawable(R.drawable.ic_volume_up_black_24dp);
                if (drawable != null) {
                    drawable.setTint(vtc);
                }
            } else {
                drawable = activity.getResources().getDrawable(R.drawable.ic_volume_up_black_24dp);
            }

            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                number.setCompoundDrawables(drawable, null, null, null);
                number.setText("");
            }
        } else {
            number.setTextColor(vtc);
            number.setCompoundDrawables(null, null, null, null);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        mainTC = colors[0];
        vicTC = colors[1];
        accentC = colors[2];

        notifyDataSetChanged();
    }


    public void update(Boolean currentSheet, int index) {
        isCurrentSheetPlaying = currentSheet;
        currentIndex = index;
        setMultiSelectModeSwitchAnimEnable(false);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        ImageView favorite;
        TextView duration;
        TextView number;
        TextView arts;
        ImageButton more;
        View itemView;
        CheckBox check;
        final View[] views;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = (TextView) itemView.findViewById(R.id.sheet_song_item_name);
            image = (ImageView) itemView.findViewById(R.id.sheet_song_item_image);
            favorite = (ImageView) itemView.findViewById(R.id.sheet_song_item_favorite);
            duration = (TextView) itemView.findViewById(R.id.sheet_song_item_duration);
            number = (TextView) itemView.findViewById(R.id.sheet_song_item_number);
            arts = (TextView) itemView.findViewById(R.id.sheet_song_item_arts);
            more = (ImageButton) itemView.findViewById(R.id.sheet_song_item_more);
            check = (CheckBox) itemView.findViewById(R.id.sheet_song_item_check);
            views = new View[]{
                    name,
                    image,
                    favorite,
                    duration,
                    number,
                    arts,
                    more,
                    check};

        }
    }

    public static class DataHolder {
        SongInfo info;
        boolean isFavorite;

        public DataHolder(SongInfo info, boolean isFavorite) {
            this.info = info;
            this.isFavorite = isFavorite;
        }
    }
}
