package com.duan.musicoco.search;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.StringUtils;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/8/6.
 */

public class ResultsAdapter extends BaseAdapter {

    private final Activity mActivity;
    private final List<SongInfo> data;

    private int mainTC;
    private int vicTC;

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ResultsAdapter(Activity activity, List<SongInfo> data) {
        this.mActivity = activity;
        this.data = data;
    }

    public void updateColors(int mainTC, int vicTC) {
        this.mainTC = mainTC;
        this.vicTC = vicTC;
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.search_result_item, null);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.number.setTextColor(vicTC);
        holder.number.setText(String.valueOf(position + 1));

        holder.name.setTextColor(mainTC);
        holder.name.setText(info.getTitle());

        String arts = mActivity.getString(R.string.arts) + ": " + info.getArtist();
        holder.arts.setTextColor(vicTC);
        holder.arts.setText(arts);

        holder.duration.setTextColor(vicTC);
        String time = StringUtils.getGenTimeMS((int) info.getDuration());
        holder.duration.setText(time);

        String album = mActivity.getString(R.string.album) + ": " + info.getAlbum();
        holder.album.setText(album);
        holder.album.setTextColor(vicTC);

        if (itemClickListener != null) {
            final int pos = position;
            final View v = convertView;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v, pos);
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {

        TextView name;
        TextView duration;
        TextView number;
        TextView arts;
        TextView album;

        public ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.search_result_name);
            duration = (TextView) convertView.findViewById(R.id.search_result_duration);
            number = (TextView) convertView.findViewById(R.id.search_result_number);
            arts = (TextView) convertView.findViewById(R.id.search_result_arts);
            album = (TextView) convertView.findViewById(R.id.search_result_album);
        }
    }
}
