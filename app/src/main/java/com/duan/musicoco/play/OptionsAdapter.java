package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/7/18.
 */

public class OptionsAdapter extends BaseAdapter {

    private Context context;

    private int[] iconsID;
    private String[] texts;

    private int iconColor;
    private int textColor;

    public OptionsAdapter(Context context, int[] iconsID, String[] texts) {
        this.context = context;
        this.iconsID = iconsID;
        this.texts = texts;
        this.iconColor = this.textColor = Color.DKGRAY;
    }

    @Override
    public int getCount() {
        return texts.length;
    }

    @Override
    public Object getItem(int position) {
        return texts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item_image_text, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.simple_list_item_image);
            holder.text = (TextView) convertView.findViewById(R.id.simple_list_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(texts[position]);
        holder.text.setTextColor(textColor);

        holder.icon.setImageResource(iconsID[position]);
        holder.icon.getDrawable().setTint(iconColor);

        return convertView;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView icon;
        TextView text;
    }
}
