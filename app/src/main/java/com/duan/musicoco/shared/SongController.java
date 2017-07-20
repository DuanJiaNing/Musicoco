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
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.util.DialogUtils;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.ToastUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class SongController {

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;

    public SongController(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
    }

    private Map<Integer, String[]> getSheetsData() {
        Map<Integer, String[]> res = new HashMap<>();
        String[] names;
        String[] counts;

        List<Sheet> sheets = dbMusicoco.getSheets();
        names = new String[sheets.size() + 1];
        counts = new String[sheets.size() + 1];
        for (int i = 0; i < sheets.size(); i++) {
            Sheet s = sheets.get(i);
            names[i] = s.name;
            counts[i] = s.count + "首";
        }
        names[names.length - 1] = activity.getString(R.string.new_sheet) + " + ";
        counts[names.length - 1] = "";

        res.put(0, names);
        res.put(1, counts);
        return res;
    }

    public void handleCollectToSheet(final SongInfo info) {
        final Map<Integer, String[]> res = getSheetsData();

        DialogProvider manager = new DialogProvider(activity);
        ListView listView = new ListView(activity);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        OptionsAdapter adapter = new OptionsAdapter(activity, null, res.get(0), res.get(1));
        adapter.setPaddingLeft(30);
        listView.setAdapter(adapter);
        final AlertDialog dialog = manager.createCustomInsiderDialog(
                activity.getString(R.string.song_collection_sheet),
                "歌曲：" + info.getTitle(),
                listView
        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == (res.get(0).length - 1)) {
                    DialogUtils.showAddSheetDialog(activity, dbMusicoco);
                } else {
                    String sheetName = res.get(0)[position];
                    Song song = new Song(info.getData());
                    if (dbMusicoco.addSongToSheet(sheetName, song)) {
                        String msg = activity.getString(R.string.success_add_to_sheet) + "[" + sheetName + "]";
                        ToastUtils.showShortToast(activity, msg);
                    } else {
                        ToastUtils.showShortToast(activity, activity.getString(R.string.error_song_is_already_in_sheet));
                    }
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void checkSongDetail(SongInfo info) {
        new ActivityManager(activity).startSongDetailActivity(new Song(info.getData()));
    }

    public void deleteSongFromDisk(final Song song) {
        DialogProvider manager = new DialogProvider(activity);
        final Dialog dialog = manager.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.delete_confirm)
        );
        manager.setOnPositiveButtonListener("确认", new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSongFromDiskAndLibraryForever(song);
                dialog.dismiss();
            }
        });

        manager.setOnNegativeButtonListener("取消", new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void deleteSongFromDiskAndLibraryForever(Song song) {
        removeSongFromSheet(song);

        String msg = activity.getString(R.string.error_delete_file_fail);
        if (FileUtils.deleteFile(song.path)) {
            dbMusicoco.removeSongInfoFromBothTable(song);
            msg = activity.getString(R.string.success_delete_file);
        }

        ToastUtils.showShortToast(activity, msg);
    }

    public void removeSongFromSheet(Song song) {
        try {
            //需要在服务器移除前修改数据库
            int sheetID = control.getPlayListId();
            dbMusicoco.removeSongFromSheet(song, sheetID);

            control.remove(song);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
