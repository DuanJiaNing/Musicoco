package com.duan.musicoco.shared;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/6/22.
 */

public class PlayListAdapter extends BaseAdapter {

    private final List<SongInfo> data;
    private final Context context;

    private int selectItem;
    private boolean removeButtonEnable = false;

    private int mainC;
    private int vicC;
    private int accentC;

    private OnItemRemoveClickListener removeClickListener;
    private OnItemClickListener itemClickListener;

    public interface OnItemRemoveClickListener {
        void onRemove(int position, SongInfo info);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, SongInfo info);
    }

    public void setOnRemoveClickListener(OnItemRemoveClickListener removeClickListener) {
        this.removeClickListener = removeClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public PlayListAdapter(Context context, List<SongInfo> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public SongInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        SongInfo info = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.play_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(info.getTitle());
        holder.arts.setText(info.getArtist());
        holder.number.setText(String.valueOf(getItemId(position) + 1));

        if (position == selectItem) {
            setSelectItem(holder);
        } else {
            setNormalItem(holder);
        }

        if (removeButtonEnable) {
            holder.remove.setEnabled(true);
            holder.remove.setVisibility(View.VISIBLE);

            if (removeClickListener != null) {
                final int pos = position;
                final SongInfo in = info;
                holder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeClickListener.onRemove(pos, in);
                    }
                });
            }

        } else {
            holder.remove.setEnabled(false);
            holder.remove.setVisibility(View.INVISIBLE);
        }

        if (itemClickListener != null) {
            final int pos = position;
            final SongInfo in = info;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(pos, in);
                }
            });
        }

        return convertView;
    }

    private void setSelectItem(ViewHolder holder) {

        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(R.drawable.ic_volume_up_black_24dp);
            if (drawable != null) {
                drawable.setTint(accentC);
            }
        } else {
            drawable = context.getResources().getDrawable(R.drawable.ic_volume_up_black_24dp);
        }

        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.number.setCompoundDrawables(drawable, null, null, null);
            holder.number.setText("");
        }

        holder.name.setTextColor(accentC);
        holder.arts.setTextColor(accentC);
        holder.number.setTextColor(accentC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.remove.getDrawable().setTint(accentC);
        }

    }

    private void setNormalItem(ViewHolder holder) {
        holder.number.setCompoundDrawables(null, null, null, null);

        holder.name.setTextColor(mainC);
        holder.arts.setTextColor(vicC);
        holder.number.setTextColor(vicC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.remove.getDrawable().setTint(vicC);
        }

    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
        notifyDataSetChanged();
    }

    private final class ViewHolder {
        TextView name;
        TextView arts;
        TextView number;
        ImageButton remove;

        ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.play_list_item_name);
            number = (TextView) convertView.findViewById(R.id.play_list_item_number);
            arts = (TextView) convertView.findViewById(R.id.play_list_item_arts);
            remove = (ImageButton) convertView.findViewById(R.id.play_list_item_remove);
        }

    }

    public void setRemoveButtonEnable(boolean enable) {
        this.removeButtonEnable = enable;
        notifyDataSetChanged();
    }

    public int getMainTextColor() {
        return mainC;
    }

    public int getVicTextColor() {
        return vicC;
    }

    public int getSelectItemColor() {
        return accentC;
    }

    public void setMainTextColor(int color) {
        this.mainC = color;
        notifyDataSetChanged();
    }

    public void setVicTextColor(int color) {
        this.vicC = color;
        notifyDataSetChanged();
    }

    public void setSelectItemColor(int color) {
        this.accentC = color;
        notifyDataSetChanged();
    }

}
