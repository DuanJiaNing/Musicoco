package com.duan.musicoco.shared;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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

    private List<Option> options = new ArrayList<>();

    private int iconColor;
    private int titleColor;
    private int msgColor;

    private int mPaddingLeft = 0;

    public OptionsAdapter(Context context) {
        this.context = context;
        this.iconColor = this.titleColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;
        this.options = new ArrayList<>();
    }

    public OptionsAdapter(Context context, List<Option> options) {
        this.context = context;
        this.iconColor = this.titleColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;
        this.options = options;
    }

    public OptionsAdapter(Context context, @Nullable int[] iconsID, @Nullable int[] ids, @NonNull String[] texts, @Nullable String[] msgs, @Nullable OptionClickListener[] listener) {
        this.context = context;
        this.iconColor = this.titleColor = Color.DKGRAY;
        this.msgColor = Color.GRAY;

        for (int i = 0; i < texts.length; i++) {
            Option option = new Option(
                    texts[i],
                    ids == null ? 0 : (ids.length == texts.length ? ids[i] : 0),
                    msgs == null ? null : msgs[i],
                    iconsID == null ? -1 : iconsID[i],
                    listener == null ? null : listener[i]
            );
            options.add(option);
        }
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

        final String text = option.title;
        final String msg = option.msg == null ? "" : option.msg;
        final int iconID = option.iconID;
        final OptionClickListener listener = option.clickListener;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item_image_text, null);
            holder = new ViewHolder(convertView, iconID != -1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(text);
        holder.text.setTextColor(titleColor);

        if (iconID != -1) {
            holder.icon.setImageResource(iconID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.icon.getDrawable().setTint(iconColor);
            }
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
                    listener.onClick(holder, position, option);
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

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        notifyDataSetChanged();
    }

    public void setMsgColor(int msgColor) {
        this.msgColor = msgColor;
        notifyDataSetChanged();
    }

    @Nullable
    public Option getOption(int id) {
        for (Option o : options) {
            if (o.id == id) {
                return o;
            }
        }
        return null;
    }

    public void removeOption(int id) {
        Option op = getOption(id);
        if (op != null) {
            int index = options.indexOf(op);
            options.remove(index);
            notifyDataSetChanged();
        }
    }

    public void addOption(Option option) {
        options.add(option);
        notifyDataSetChanged();
    }

    public void addOption(@NonNull String title, @Nullable String msg, int id, int iconID, @Nullable OptionClickListener listener) {
        Option opion = new Option(title, id, msg, iconID, listener);
        options.add(opion);
        notifyDataSetChanged();
    }

    public void addOption(@Nullable int[] iconsID, @Nullable int[] ids, @NonNull String[] texts, @Nullable String[] msgs, @Nullable OptionClickListener[] listener) {
        for (int i = 0; i < texts.length; i++) {
            Option option = new Option(
                    texts[i],
                    ids == null ? 0 : (ids.length == texts.length ? ids[i] : 0),
                    msgs == null ? null : msgs[i],
                    iconsID == null ? -1 : iconsID[i],
                    listener == null ? null : listener[i]
            );
            options.add(option);
        }
        notifyDataSetChanged();
    }

    public void clearOptions() {
        options.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public ImageView icon;
        public TextView text;
        public TextView msg;

        public ViewHolder(View convertView, boolean hasIcon) {
            icon = (ImageView) convertView.findViewById(R.id.simple_list_item_image);
            text = (TextView) convertView.findViewById(R.id.simple_list_item_text);
            msg = (TextView) convertView.findViewById(R.id.simple_list_item_msg);

            if (hasIcon) {
                mPaddingLeft = mPaddingLeft == 0 ?
                        context.getResources().getDimensionPixelSize(R.dimen.activity_default_margin_s) :
                        mPaddingLeft;
                View v = icon;
                icon.setPadding(mPaddingLeft, v.getTop(), v.getPaddingRight(), v.getPaddingBottom());
            } else {

                View vt = text;
                text.setPadding(0, vt.getPaddingTop(), vt.getPaddingRight(), vt.getPaddingBottom());

                View v = icon;
                icon.setPadding(mPaddingLeft, v.getTop(), v.getPaddingRight(), v.getPaddingBottom());
            }

        }
    }

    /**
     * 可以选择以 OptionClickListener 的方式为每一个选项指定点击事件，这样代码更清晰，也可选择为 ListView etc 添加
     * ItemClickListener 处理选项点击,同时设置时 OptionClickListener 优先
     */
    public interface OptionClickListener {
        void onClick(ViewHolder holder, int position, Option option);
    }

    public static class Option {
        public String title;
        public String msg;
        public int id;
        public int iconID = -1;

        public OptionClickListener clickListener;

        public Option(int id) {
            this("", id);
        }

        public Option(@NonNull String title, int id) {
            this(title, id, null, -1, null);
        }

        /**
         * @param iconID 没有时为 -1
         */
        public Option(@NonNull String title, int id, @Nullable String msg, int iconID, @Nullable OptionClickListener clickListener) {
            this.title = title;
            this.msg = msg == null ? "" : msg;
            this.iconID = iconID;
            this.id = id;
            this.clickListener = clickListener;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setIconID(int iconID) {
            this.iconID = iconID;
        }

        public void setClickListener(OptionClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Option option = (Option) o;

            return id == option.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
