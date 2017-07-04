package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.ExceptionHandler;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.OnThemeChange;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/6/22.
 */

public class PlayListAdapter extends BaseAdapter implements OnThemeChange {

    private static final String TAG = "PlayListAdapter";

    private final List<SongInfo> songs;

    private final IPlayControl control;
    private final Context context;

    private View.OnClickListener removeClickListener;
    private View.OnClickListener itemClickListener;

    private final MediaManager mediaManager;

    private int colorMain;
    private int colorVic;

    public PlayListAdapter(final Context context, final IPlayControl control) {
        this.context = context;
        this.control = control;
        this.mediaManager = MediaManager.getInstance(context);

        this.removeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song s = new Song((String) v.getTag(R.id.play_list_item_remove_path));

                try {
                    //如果移除当前正在播放曲目服务端会自动跳到下一首
                    control.remove(s);
                    updateData();
                    notifyDataSetChanged();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(context,
                            context.getString(R.string.exception_remote), null
                    );
                }
            }
        };

        this.itemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    int pos = (int) v.getTag(R.id.play_list_item_position);

                    int index = control.currentSongIndex();
                    if (pos == index) {
                        Log.d(TAG, "onClick: the song is playing");
                        if (control.status() != PlayController.STATUS_PLAYING)
                            control.resume();
                        return;
                    }

                    SongInfo in = (SongInfo) getItem(pos);
                    control.play(new Song(in.getData()));
                    notifyDataSetChanged();

                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(context,
                            context.getString(R.string.exception_remote), null
                    );
                }
            }
        };

        this.songs = new ArrayList<>();
        updateData();
    }

    private void updateData() {
        try {

            List<Song> ss = control.getPlayList();
            songs.clear();
            for (Song s : ss) {
                songs.add(mediaManager.getSongInfo(s));
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(context,
                    context.getString(R.string.exception_remote), null
            );
        }
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_play_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.play_list_item_name);
            holder.arts = (TextView) convertView.findViewById(R.id.play_list_item_arts);
            holder.remove = (ImageButton) convertView.findViewById(R.id.play_list_item_remove);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(R.id.play_list_item_position, position);
        convertView.setOnClickListener(itemClickListener);

        SongInfo info = (SongInfo) getItem(position);
        holder.name.setText(info.getTitle());
        holder.arts.setText(info.getArtist());

        holder.remove.setTag(R.id.play_list_item_remove_position, position);
        holder.remove.setTag(R.id.play_list_item_remove_path, info.getData());
        holder.remove.setOnClickListener(removeClickListener);

        Drawable drawable = null;

        int index = 0;
        try {

            index = control.currentSongIndex();

        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(context,
                    context.getString(R.string.exception_remote), null
            );
        }

        if (position == index) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = context.getDrawable(R.drawable.ic_volume_up_black_24dp);
            } else
                drawable = context.getResources().getDrawable(R.drawable.ic_volume_up_black_24dp);
        }

        if (drawable != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable.setTint(colorMain);
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        holder.name.setCompoundDrawables(drawable, null, null, null);

        holder.name.setTextColor(colorMain);
        holder.arts.setTextColor(colorVic);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.remove.getDrawable().setTint(colorVic);
        }

        return convertView;
    }


    @Override
    public void themeChange(Theme theme) {
        int[] colors;
        switch (theme) {
            case DARK:
                colors = ColorUtils.getThemeDarkColors(context);
                break;
            case WHITE:
            default:
                colors = ColorUtils.getThemeWhiteColors(context);
                break;
        }

        colorMain = colors[1];
        colorVic = colors[3];

        notifyDataSetChanged();
    }

    private final class ViewHolder {
        TextView name;
        TextView arts;
        ImageButton remove;
    }


    public void setOnRemoveClickListener(View.OnClickListener removeClickListener) {
        this.removeClickListener = removeClickListener;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
