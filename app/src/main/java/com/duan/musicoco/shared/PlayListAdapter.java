package com.duan.musicoco.shared;

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
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/6/22.
 */

public class PlayListAdapter extends BaseAdapter implements
        OnThemeChange,
        OnContentUpdate {

    private static final String TAG = "PlayListAdapter";

    private final List<SongInfo> songs;

    private final IPlayControl control;
    private final Context context;
    private DBMusicocoController dbController;

    private View.OnClickListener removeClickListener;
    private View.OnClickListener itemClickListener;

    private final MediaManager mediaManager;
    private SongOperation songOperation;

    private int colorMain;
    private int colorVic;

    public PlayListAdapter(final Context context, final IPlayControl control,
                           final DBMusicocoController dbMusicocoController,
                           final SongOperation songOperation) {
        this.context = context;
        this.control = control;
        this.dbController = dbMusicocoController;
        this.songOperation = songOperation;
        this.mediaManager = MediaManager.getInstance(context);

        this.removeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song s = new Song((String) v.getTag(R.id.play_list_item_remove_path));
                PlayListAdapter.this.songOperation.removeSongFromCurrentSheet(s);
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
        update(null, null);
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
            holder.number = (TextView) convertView.findViewById(R.id.play_list_item_number);
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
        holder.number.setText(String.valueOf(getItemId(position) + 1));

        try {
            int sheetID = control.getPlayListId();
            if (sheetID < 0) {
                holder.remove.setEnabled(false);
                holder.remove.setVisibility(View.INVISIBLE);
            } else {
                holder.remove.setEnabled(true);
                holder.remove.setVisibility(View.VISIBLE);

                holder.remove.setTag(R.id.play_list_item_remove_position, position);
                holder.remove.setTag(R.id.play_list_item_remove_path, info.getData());
                holder.remove.setOnClickListener(removeClickListener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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
            holder.number.setCompoundDrawables(drawable, null, null, null);
            holder.number.setText("");
        }

    }

    private void setNormalItem(ViewHolder holder) {
        holder.number.setCompoundDrawables(null, null, null, null);
    }

    public void updateColors(Theme theme, int[] colors) {
        Log.d("update", "PlayListAdapter updateColors");

        colorMain = colors[0];
        colorVic = colors[1];

        notifyDataSetChanged();
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        updateColors(theme, colors);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged statusChanged) {
        Log.d("update", "PlayListAdapter update");
        try {

            List<Song> ss = control.getPlayList();
            songs.clear();
            for (Song s : ss) {
                songs.add(mediaManager.getSongInfo(s));
            }
            notifyDataSetChanged();

        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(context,
                    context.getString(R.string.exception_remote), null
            );
        }
    }

    private final class ViewHolder {
        TextView name;
        TextView arts;
        TextView number;
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
