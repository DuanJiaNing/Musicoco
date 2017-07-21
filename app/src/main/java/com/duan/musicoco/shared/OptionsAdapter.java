package com.duan.musicoco.shared;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/18.
 */

public class OptionsAdapter extends BaseAdapter {

    private Context context;

    private List<Option> options;

    private int iconColor;
    private int textColor;
    private int msgColor;

    private int mPaddingLeft = 0;

    public OptionsAdapter(Context context) {
        this.context = context;
        this.iconColor = this.textColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;
        this.options = new ArrayList<>();
    }

    public OptionsAdapter(Context context, List<Option> options) {
        this.context = context;
        this.iconColor = this.textColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;
        this.options = options;
    }

    public OptionsAdapter(Context context, @Nullable int[] iconsID, @NonNull String[] texts, @Nullable String[] msgs, @Nullable OptionClickListener[] listener) {
        this.context = context;
        this.iconColor = this.textColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;

        this.options = new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            Option option = new Option(
                    texts[i],
                    msgs == null ? null : msgs[i],
                    iconsID == null ? -1 : iconsID[i],
                    listener == null ? null : listener[i]
            );
            options.add(option);
        }
    }

    public void addOption(Option option) {
        options.add(option);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return options.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Option option = (Option) getItem(position);

        final String text = option.text;
        final String msg = option.msg == null ? "" : option.msg;
        final int iconID = option.iconID;
        final OptionClickListener listener = option.clickListener;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item_image_text, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.simple_list_item_image);
            holder.text = (TextView) convertView.findViewById(R.id.simple_list_item_text);
            holder.msg = (TextView) convertView.findViewById(R.id.simple_list_item_msg);

            if (iconID != -1) {
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

        holder.text.setText(text);
        holder.text.setTextColor(textColor);

        if (iconID != -1) {
            holder.icon.setImageResource(iconID);
            holder.icon.getDrawable().setTint(iconColor);
        } else if (holder.icon.getDrawable() != null) {
            //复用时防止图标错乱
            holder.icon.setImageDrawable(null);
        }

        holder.msg.setText(msg);
        holder.msg.setTextColor(msgColor);

        if (listener != null) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(holder, position);
                }
            });
        } else {
            convertView.setClickable(false);
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

    public class ViewHolder {
        public ImageView icon;
        public TextView text;
        public TextView msg;
    }

    /**
     * 可以选择以 OptionClickListener 的方式为每一个选项指定点击事件，这样代码更清晰，也可选择为 ListView etc 添加
     * ItemClickListener 处理选项点击,同时设置时 OptionClickListener 优先
     */
    public interface OptionClickListener {
        void onClick(ViewHolder holder, int position);
    }

    public static class Option {
        public String text;
        public String msg;
        public int iconID = -1;

        public OptionClickListener clickListener;

        public Option(String text) {
            this.text = text;
        }

        /**
         * @param iconID 没有时为 -1
         */
        public Option(@NonNull String text, @Nullable String msg, int iconID, @Nullable OptionClickListener clickListener) {
            this.text = text;
            this.msg = msg == null ? "" : msg;
            this.iconID = iconID;
            this.clickListener = clickListener;
        }
    }
}
