package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.ToastUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class SongOperation {

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;

    public SongOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
    }

    private Map<Integer, String[]> getSheetsData() {
        Map<Integer, String[]> res = new HashMap<>();
        String[] names;
        String[] counts;

        List<Sheet> sheets = dbMusicoco.getSheets();
        names = new String[sheets.size()];
        counts = new String[sheets.size()];
        for (int i = 0; i < sheets.size(); i++) {
            Sheet s = sheets.get(i);
            names[i] = s.name;
            counts[i] = s.count + "首";
        }

        res.put(0, names);
        res.put(1, counts);
        return res;
    }

    public void handleCollectToSheet(final SongInfo info) {
        final Map<Integer, String[]> res = getSheetsData();

        DialogProvider manager = new DialogProvider(activity);
        ListView listView = new ListView(activity);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        OptionsAdapter adapter = new OptionsAdapter(activity, null, null, res.get(0), res.get(1), null);
        adapter.setIconColor(manager.getAccentColor());
        adapter.setTitleColor(manager.getMainTextColor());
        adapter.setMsgColor(manager.getVicTextColor());

        adapter.setPaddingLeft(30);
        listView.setAdapter(adapter);
        final AlertDialog dialog = manager.createCustomInsiderDialog(
                activity.getString(R.string.song_operation_collection_sheet),
                activity.getString(R.string.song) + ": " + info.getTitle(),
                listView
        );

        addNewSheetOption(adapter, dialog);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sheetName = res.get(0)[position];
                Song song = new Song(info.getData());
                if (dbMusicoco.addSongToSheet(sheetName, song)) {
                    String msg = activity.getString(R.string.success_add_to_sheet) + " [" + sheetName + "]";
                    ToastUtils.showShortToast(msg);
                } else {
                    ToastUtils.showShortToast(activity.getString(R.string.error_song_is_already_in_sheet));
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addNewSheetOption(OptionsAdapter optionsAdapter, final AlertDialog dialog) {
        OptionsAdapter.Option newSheet = new OptionsAdapter.Option(activity.getString(R.string.new_sheet), 0);
        newSheet.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                new MySheetsOperation(activity, control, dbMusicoco).handleAddSheet();
                dialog.hide();
            }
        };
        newSheet.iconID = R.drawable.ic_action_add;

        optionsAdapter.addOption(newSheet);
        optionsAdapter.notifyDataSetChanged();
    }

    public void checkSongDetail(Song song) {
        ActivityManager.getInstance(activity).startSongDetailActivity(song);
    }

    public void handleDeleteSongForever(final Song song) {
        DialogProvider manager = new DialogProvider(activity);
        final Dialog dialog = manager.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.delete_confirm)
        );
        manager.setOnPositiveButtonListener(activity.getString(R.string.ensure), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSongFromDiskAndLibraryForever(song);
                dialog.dismiss();
            }
        });

        manager.setOnNegativeButtonListener(activity.getString(R.string.cancel), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void deleteSongFromDiskAndLibraryForever(Song song) {
        removeSongFromCurrentSheet(song);

        String msg = activity.getString(R.string.error_delete_file_fail);
        if (FileUtils.deleteFile(song.path)) {
            dbMusicoco.removeSongInfo(song);
            msg = activity.getString(R.string.success_delete_file);
        }

        ToastUtils.showShortToast(msg);
    }

    public void removeSongFromCurrentSheet(Song song) {
        try {
            //需要在服务器移除前修改数据库
            int sheetID = control.getPlayListId();
            dbMusicoco.removeSongInfoFromSheet(song, sheetID);
            control.remove(song);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void removeSongFromSheet(Song song, int sheetID) {
        try {
            //需要在服务器移除前修改数据库
            dbMusicoco.removeSongInfoFromSheet(song, sheetID);
            control.remove(song);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    //反转歌曲收藏状态
    public boolean reverseSongFavoriteStatus(Song song) {
        if (song != null) {
            DBSongInfo info = dbMusicoco.getSongInfo(song);
            if (info != null) {
                boolean isFavorite = info.favorite;
                boolean reverse = !isFavorite;
                dbMusicoco.updateSongFavorite(song, reverse);
                //广播通知 MainActivity 更新 MainSheetsController
                BroadcastManager.getInstance(activity).sendMyBroadcast(BroadcastManager.FILTER_MAIN_SHEET_CHANGED, null);

                return reverse;
            }
        }

        return false;
    }

}
