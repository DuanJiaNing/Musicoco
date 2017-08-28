package com.duan.musicoco.rmp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.Utils;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/8/8.
 */

public class RMPAdapter extends RecyclerView.Adapter<RMPAdapter.ViewHolder> implements ThemeChangeable {

    private Context context;
    private final List<DataHolder> data;

    private int accentBC;
    private int accentTC;

    private final int imageWidth;
    private final int imageHeight;

    public RMPAdapter(Context context, List<DataHolder> data) {
        this.context = context;
        this.data = data;
        this.imageWidth = Utils.getMetrics((Activity) context).widthPixels;
        this.imageHeight = context.getResources().getDimensionPixelSize(R.dimen.rmp_song_item_height);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DataHolder data, ViewHolder view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rmp_song_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataHolder dataHolder = getItem(position);

        holder.name.setText(dataHolder.title);
        holder.arts.setText(dataHolder.arts);
        String time = dataHolder.times + " " + context.getString(R.string.count);
        holder.time.setText(time);
        holder.time.setBackgroundColor(accentBC);
        holder.time.setTextColor(accentTC);

        final ImageView image = holder.image;
        final View gradientV = holder.gradient;
        Glide.with(context)
                .load(dataHolder.album)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_song)
                .into(new SimpleTarget<Bitmap>(imageWidth, imageHeight) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            image.setImageBitmap(resource);
                            int dur = context.getResources().getInteger(R.integer.anim_default_duration) * 2;
                            AnimationUtils.startAlphaAnim(image, dur, null, 0.3f, 1.0f);

                            bindImageAndForeground(gradientV, resource);
                        }
                    }
                });

        if (listener != null) {
            final int pos = position;
            final DataHolder da = dataHolder;
            final ViewHolder view = holder;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(da, view, pos);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bindImageAndForeground(View image, Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = BitmapUtils.bitmapResizeFromResource(context.getResources(), R.drawable.default_song, imageWidth, imageHeight);
        }

        int defaultTC = Color.WHITE;
        int defaultBC = Color.GRAY;
        int[] colors = new int[4];
        ColorUtils.get4LightColorWithTextFormBitmap(bitmap, defaultBC, defaultTC, colors);

        int color = colors[0];
        color = color == defaultBC ? colors[2] : color;
        color = android.support.v4.graphics.ColorUtils.setAlphaComponent(color, 255);
        GradientDrawable dra = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, Color.TRANSPARENT});
        dra.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        image.setBackground(dra);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public DataHolder getItem(int position) {
        return data.get(position);
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int statusC = colors[0];
        int toolbarC = colors[1];
        int accentC = colors[2];
        int mainBC = colors[3];
        int vicBC = colors[4];
        int mainTC = colors[5];
        int vicTC = colors[6];
        int navC = colors[7];
        int toolbarMainTC = colors[8];
        int toolbarVicTC = colors[9];

        this.accentBC = accentC;
        this.accentTC = toolbarMainTC;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView arts;
        TextView time;

        // 通过为 image 设置前景的效果太差，所以单独用一个 View
        View gradient;

        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = (ImageView) itemView.findViewById(R.id.rmp_item_image);
            name = (TextView) itemView.findViewById(R.id.rmp_item_name);
            arts = (TextView) itemView.findViewById(R.id.rmp_item_arts);
            time = (TextView) itemView.findViewById(R.id.rmp_item_time);
            gradient = itemView.findViewById(R.id.rmp_item_gradient);

        }
    }

    static class DataHolder {
        String title;
        String arts;
        String album;
        int times;
        String path;
    }
}
