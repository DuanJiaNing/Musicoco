package com.duan.musicoco.main.leftnav.imagewall;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.Utils;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/8/18.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final List<SongInfo> data;
    private final Activity activity;

    private static int size;

    public ImageAdapter(List<SongInfo> data, Activity activity) {
        this.data = data;
        this.activity = activity;
        size = Utils.getMetrics(activity).widthPixels / 3;
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ViewHolder view, SongInfo d, int position);
    }

    private OnItemMoreClickListener itemMoreClickListener;

    public interface OnItemMoreClickListener {
        void onItemMore(ViewHolder view, SongInfo d, int position);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.itemClickListener = l;
    }

    public void setOnItemMoreClickListener(OnItemMoreClickListener l) {
        this.itemMoreClickListener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.image_wall_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SongInfo d = getItem(position);
        String path = d.getAlbum_path();

        Glide.with(activity)
                .load(path)
                .placeholder(R.drawable.default_song)
                .into(holder.image);

        final int pos = position;
        final ViewHolder h = holder;
        if (itemClickListener != null) {
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(h, d, pos);
                }
            });
        }

        if (itemMoreClickListener != null) {
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemMoreClickListener.onItemMore(h, d, pos);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public SongInfo getItem(int position) {
        return data.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton more;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_wall_item_image);
            more = (ImageButton) itemView.findViewById(R.id.image_wall_item_more);

            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = size;
            params.height = size;
            itemView.setLayoutParams(params);
        }
    }
}
