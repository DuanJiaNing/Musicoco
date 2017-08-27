package com.duan.musicoco.search;

import android.app.Activity;
import android.os.RemoteException;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.MediaUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/8/6.
 */

public class SearchController implements ThemeChangeable, ResultsAdapter.OnItemClickListener {

    private final List<DBSongInfo> mInfos;
    private TextView mResult;
    private TextView mSheet;
    private View mTextContaiiner;
    private ListView mList;

    private final int mSheetId;
    private final Activity mActivity;
    private final DBMusicocoController mDbController;

    private final List<SongInfo> data;
    private final ResultsAdapter adapter;

    private final OptionsDialog optionsDialog;
    private OptionsAdapter optionsAdapter;
    private SongOperation songOperation;
    private IPlayControl control;

    private SongInfo currentClickItem;
    private ActivityManager activityManager;

    public SearchController(Activity activity, DBMusicocoController dbController, int sheetId, List<DBSongInfo> infos) {
        this.mSheetId = sheetId;
        this.mActivity = activity;
        this.mDbController = dbController;
        this.mInfos = infos;
        this.data = new ArrayList<>();
        this.adapter = new ResultsAdapter(activity, data);
        this.optionsDialog = new OptionsDialog(activity);
        this.control = MainActivity.getControl();
        this.activityManager = ActivityManager.getInstance();
    }

    private void updatePrompt() {
        String str = mActivity.getString(R.string.replace_search_result_prompt);
        str = str.replace("*", String.valueOf(data.size()));
        mResult.setText(str);
    }

    public void initViews() {
        mResult = (TextView) mActivity.findViewById(R.id.search_result);
        mSheet = (TextView) mActivity.findViewById(R.id.search_sheet);
        mTextContaiiner = mActivity.findViewById(R.id.search_result_text_container);
        mList = (ListView) mActivity.findViewById(R.id.search_list);

        mList.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    public void initData() {
        String str = mActivity.getString(R.string.replace_search_from_songs);
        str = str.replace("*", String.valueOf(mInfos.size()));
        mResult.setText(str);

        String sheet = "";
        if (mSheetId < 0) {
            sheet = MainSheetHelper.getMainSheetName(mActivity, mSheetId);
        } else {
            Sheet s = mDbController.getSheet(mSheetId);
            if (s != null) {
                sheet = mActivity.getString(R.string.sheet) + ": " + s.name;
            }
        }
        mSheet.setText(sheet);

        initOptionsDialog();
    }

    private void initOptionsDialog() {
        songOperation = new SongOperation(mActivity, control, mDbController);

        optionsAdapter = new OptionsAdapter(mActivity);
        initDialogOptions();
        optionsDialog.setAdapter(optionsAdapter);
    }

    private void initDialogOptions() {

        // 播放
        optionsAdapter.addOption(
                mActivity.getString(R.string.play),
                null,
                0,
                R.drawable.ic_play_arrow_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        playSong();
                        optionsDialog.hide();
                    }
                });

        // 在歌单中显示
        optionsAdapter.addOption(
                mActivity.getString(R.string.info_show_in_sheet),
                null,
                1,
                R.drawable.ic_location_searching_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        optionsDialog.hide();
                        mActivity.finish();
                        locationSongInSheet();
                    }
                });

        // 歌曲详情
        optionsAdapter.addOption(
                mActivity.getString(R.string.song_operation_detail),
                null,
                2,
                R.drawable.ic_art_track_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        Song song = new Song(currentClickItem.getData());
                        optionsDialog.hide();
                        activityManager.startSongDetailActivity(mActivity, song, false);
                    }
                });

    }

    private void locationSongInSheet() {
        Song song = new Song(currentClickItem.getData());
        activityManager.startSheetDetailActivity(mActivity, mSheetId, song);
    }

    private void playSong() {
        try {

            int sid = control.getPlayListId();
            Song song = new Song(currentClickItem.getData());
            if (sid != mSheetId) {
                control.setPlaySheet(mSheetId, 0);
                control.play(song);
            } else {
                control.play(song);
            }
            mActivity.finish();
            activityManager.startPlayActivity(mActivity);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void update(String keyWord) {

        if (keyWord == null || keyWord.length() == 0) {
            data.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        boolean b = true;
        for (char c : keyWord.toCharArray()) {
            if (c != ' ') {
                b = false;
                break;
            }
        }

        if (b) {
            data.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        MediaManager manager = MediaManager.getInstance();
        List<SongInfo> infos = MediaUtils.DBSongInfoToSongInfoList(mActivity, mInfos, manager);

        data.clear();
        for (SongInfo i : infos) {
            String name = i.getTitle();
            String arts = i.getArtist();
            String album = i.getAlbum();

            if (name.contains(keyWord) || arts.contains(keyWord) || album.contains(keyWord)) {
                data.add(i);
            }
        }

        updatePrompt();
        adapter.notifyDataSetChanged();

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int statusC = colors[0];
        int toolbarC = colors[1];
        int accentC = colors[2];
        int mainBC = colors[3];
        int vicBC = colors[4];
        int mainTC = colors[5];
        int vicTC = colors[6];
        int navC = colors[7];
        int toolbarMainTC = colors[8];
        int toolbarVicTC = colors[9];

        adapter.updateColors(mainTC, vicTC);

        optionsDialog.setTitleBarBgColor(vicBC);
        optionsDialog.setContentBgColor(mainBC);
        optionsDialog.setDivideColor(vicTC);
        optionsDialog.setTitleTextColor(mainTC);

        optionsAdapter.setTitleColor(mainTC);
        optionsAdapter.setIconColor(accentC);

        mTextContaiiner.setBackgroundColor(accentC);
        mResult.setTextColor(toolbarMainTC);
        mSheet.setTextColor(toolbarVicTC);

    }

    @Override
    public void onItemClick(View v, int position) {
        currentClickItem = adapter.getItem(position);
        if (optionsDialog.visible()) {
            optionsDialog.hide();
        } else {
            String title = mActivity.getString(R.string.song) + ": " + currentClickItem.getTitle();
            optionsDialog.setTitle(title);
            optionsDialog.show();
        }
    }
}
