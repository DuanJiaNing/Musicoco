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
import com.duan.musicoco.app.interfaces.OnThemeChange;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.play_list_item, null);
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
            setSelectItem(holder);
        } else {
            setNormalItem(holder);
        }

        holder.name.setTextColor(colorMain);
        holder.arts.setTextColor(colorVic);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.remove.getDrawable().setTint(colorVic);
        }

        return convertView;
    }

    private void setSelectItem(ViewHolder holder) {

        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(R.drawable.ic_volume_up_black_24dp);
            if (drawable != null) {
                drawable.setTint(colorMain);
            }
        } else {
            drawable = context.getResources().getDrawable(R.drawable.ic_volume_up_black_24dp);
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            holder.name.setCompoundDrawables(drawable, null, null, null);
        }

    }

    private void setNormalItem(ViewHolder holder) {
        holder.name.setCompoundDrawables(null, null, null, null);
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        int[] cs = new int[2];
        if (colors == null) {
            switch (theme) {
                case DARK:
                    cs = ColorUtils.get2DarkThemeTextColor(context);
                    break;
                case WHITE:
                default:
                    cs = ColorUtils.get2WhiteThemeTextColor(context);
                    break;
            }
        } else if (colors.length >= 2) {
            cs = colors;
        }

        colorMain = cs[0];
        colorVic = cs[1];

        notifyDataSetChanged();
    }

    private final class ViewHolder {
        TextView name;
        TextView arts;
        ImageButton remove;
    }

    public int getColorMain() {
        return colorMain;
    }

    public int getColorVic() {
        return colorVic;
    }

    public void setOnRemoveClickListener(View.OnClickListener removeClickListener) {
        this.removeClickListener = removeClickListener;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
