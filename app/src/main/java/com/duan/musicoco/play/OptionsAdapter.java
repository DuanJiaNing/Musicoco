package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.util.StringUtils;

/**
 * Created by DuanJiaNing on 2017/7/18.
 */

public class OptionsAdapter extends BaseAdapter {

    private Context context;

    private int[] iconsID;
    private String[] texts;
    private String[] msgs;

    private int iconColor;
    private int textColor;
    private int msgColor;

    private int mPaddingLeft = 0;

    public OptionsAdapter(Context context, @Nullable int[] iconsID, @NonNull String[] texts, @Nullable String[] msgs) {
        this.context = context;
        this.iconsID = iconsID;
        this.msgs = msgs;
        this.texts = texts;
        this.iconColor = this.textColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;

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
            holder.msg = (TextView) convertView.findViewById(R.id.simple_list_item_msg);

            if (iconsID != null) {
                mPaddingLeft = mPaddingLeft == 0 ?
                        context.getResources().getDimensionPixelSize(R.dimen.activity_default_margin_s) :
                        mPaddingLeft;
                View v = holder.icon;
                holder.icon.setPadding(mPaddingLeft, v.getTop(), v.getPaddingRight(), v.getPaddingBottom());
            } else {

                View vt = holder.text;
                holder.text.setPadding(0, vt.getPaddingTop(), vt.getPaddingRight(), vt.getPaddingBottom());

                View v = holder.icon;
                holder.icon.setPadding(mPaddingLeft, v.getTop(), v.getPaddingRight(), v.getPaddingBottom());
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(texts[position]);
        holder.text.setTextColor(textColor);

        if (iconsID != null) {
            holder.icon.setImageResource(iconsID[position]);
            holder.icon.getDrawable().setTint(iconColor);
        }

        if (msgs != null) {
            holder.msg.setText(msgs[position]);
            holder.msg.setTextColor(msgColor);
        }

        return convertView;
    }

    public void setPaddingLeft(int pad) {
        if (pad > 0) {
            this.mPaddingLeft = pad;
        }
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
        notifyDataSetChanged();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        notifyDataSetChanged();
    }

    public void setMsgColor(int msgColor) {
        this.msgColor = msgColor;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView icon;
        TextView text;
        TextView msg;
    }
}
