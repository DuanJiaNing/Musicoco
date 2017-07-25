package com.duan.musicoco.detail.sheet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/25.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {


    private boolean isCurrentSheetPlaying = false;
    private int playIndex;
    private Context context;
    //FXME
    private final int sheetID = 1;

    private List<DBSongInfo> data;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;


    public void update() {
        List<DBSongInfo> ds;
        if (sheetID < 0) {
            MainSheetHelper helper = new MainSheetHelper(context, dbController);
            ds = helper.getMainSheetSongInfo(sheetID);
        } else {
            ds = dbController.getSongInfos(sheetID);
        }

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        ImageView favorite;
        TextView duration;
        TextView number;
        TextView arts;
        ImageButton more;

        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.sheet_song_item_name);
            image = (ImageView) itemView.findViewById(R.id.sheet_song_item_image);
            favorite = (ImageView) itemView.findViewById(R.id.sheet_song_item_favorite);
            duration = (TextView) itemView.findViewById(R.id.sheet_song_item_duration);
            number = (TextView) itemView.findViewById(R.id.sheet_song_item_number);
            arts = (TextView) itemView.findViewById(R.id.sheet_song_item_arts);
            more = (ImageButton) itemView.findViewById(R.id.sheet_song_item_more);
        }
    }
}
