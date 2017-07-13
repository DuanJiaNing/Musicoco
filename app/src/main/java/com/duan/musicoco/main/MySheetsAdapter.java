package com.duan.musicoco.main;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.view.media.PlayView;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsAdapter extends BaseAdapter implements
        OnThemeChange {

    private Context context;
    private List<DBMusicocoController.Sheet> sheets;

    private int colorMain;
    private int colorVic;

    private View.OnClickListener moreClickListener;
    private PlayView.OnCheckedChangeListener playCheckChangeListener;

    public MySheetsAdapter(final Context context, List<DBMusicocoController.Sheet> sheets) {
        this.context = context;
        this.sheets = sheets;

        moreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) v.getTag();
                Toast.makeText(context, "click sheet more " + sheet.name, Toast.LENGTH_SHORT).show();
            }
        };

        playCheckChangeListener = new PlayView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(PlayView view, boolean checked) {
                DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) view.getTag();
                Toast.makeText(context, "check status change " + sheet.name + " checked=" + checked, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public int getCount() {
        return sheets.size();
    }

    @Override
    public Object getItem(int position) {
        return sheets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.my_sheet_list_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.sheets_item_image);
            holder.play = (PlayView) convertView.findViewById(R.id.sheets_item_play);
            holder.name = (TextView) convertView.findViewById(R.id.sheets_item_name);
            holder.remark = (TextView) convertView.findViewById(R.id.sheets_item_remark);
            holder.count = (TextView) convertView.findViewById(R.id.sheets_item_song_count);
            holder.playTimes = (TextView) convertView.findViewById(R.id.sheets_item_play_times);
            holder.more = (ImageButton) convertView.findViewById(R.id.sheets_item_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) getItem(position);
        String name = sheet.name;
        String remark = sheet.remark;
        int count = sheet.count;
        int playTimes = sheet.playTimes;

        holder.more.setTag(sheet);
        holder.more.setOnClickListener(moreClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.more.getDrawable().setTint(colorVic);
        }

        holder.play.setTag(sheet);
        holder.play.setTriangleColor(colorMain);
        holder.play.setPauseLineColor(colorMain);
        holder.play.setOnCheckedChangeListener(playCheckChangeListener);

        holder.name.setTextColor(colorMain);
        holder.name.setText(name);

        holder.remark.setTextColor(colorVic);
        holder.remark.setText(remark);

        holder.count.setTextColor(colorVic);
        holder.count.setText(count + "首");

        holder.playTimes.setTextColor(colorVic);
        holder.playTimes.setText(playTimes + "次");

        return convertView;
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        colorMain = colors[0];
        colorVic = colors[1];

        notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView image;
        PlayView play;
        TextView name;
        TextView remark;
        TextView count;
        TextView playTimes;
        ImageButton more;
    }
}
