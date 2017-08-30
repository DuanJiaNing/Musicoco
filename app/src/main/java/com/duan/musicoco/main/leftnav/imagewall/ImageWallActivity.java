package com.duan.musicoco.main.leftnav.imagewall;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.On2CompleteListener;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.MediaUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class ImageWallActivity extends RootActivity implements
        ThemeChangeable,
        ImageAdapter.OnItemClickListener,
        ImageAdapter.OnItemMoreClickListener {

    private final List<SongInfo> data = new ArrayList<>();
    private ImageAdapter adapter;
    private OptionsDialog optionsDialog;
    private OptionsAdapter optionsAdapter;
    private MainSheetHelper helper;

    private SongInfo current;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_wall);

        adapter = new ImageAdapter(data, this);
        optionsDialog = new OptionsDialog(this);
        optionsAdapter = new OptionsAdapter(this);
        optionsDialog.setAdapter(optionsAdapter);
        helper = new MainSheetHelper(this, dbController);
        activityManager = ActivityManager.getInstance();

        initViews();
        themeChange(null, null);
        initData();

    }

    private void initData() {
        List<DBSongInfo> allSongInfo = helper.getAllSongInfo();
        List<SongInfo> list = MediaUtils.DBSongInfoToSongInfoList(this, allSongInfo, MediaManager.getInstance());
        for (SongInfo info : list) {
            String path = info.getAlbum_path();
            if (StringUtils.isReal(path)) {
                data.add(info);
            }
        }
        adapter.notifyDataSetChanged();

        optionsDialog.setTitle(getString(R.string.title_about_image));
        initAdapterOptions();
    }

    private void initAdapterOptions() {
        OptionsAdapter.Option save = new OptionsAdapter.Option(
                getString(R.string.info_save_image),
                0, null, R.drawable.ic_file_download_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        FileUtils.saveImage(ImageWallActivity.this, current.getAlbum_path(), new On2CompleteListener<Boolean, String>() {
                            @Override
                            public void onComplete(Boolean aBoolean, String s) {
                                String msg;
                                if (aBoolean) {
                                    msg = getString(R.string.success_save_image_to) + s;
                                } else {
                                    msg = getString(R.string.error_save_fail);
                                }
                                ToastUtils.showShortToast(msg, ImageWallActivity.this);
                            }
                        });
                        optionsDialog.hide();
                    }
                }
        );

        OptionsAdapter.Option jumpToSource = new OptionsAdapter.Option(
                getString(R.string.info_show_in_sheet),
                1, null, R.drawable.ic_location_searching_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        optionsDialog.hide();
                        finish();
                        int sheetID = MainSheetHelper.SHEET_ALL;
                        Song at = new Song(current.getData());
                        activityManager.startSheetDetailActivity(ImageWallActivity.this, sheetID, at);
                    }
                }
        );

        OptionsAdapter.Option detail = new OptionsAdapter.Option(
                getString(R.string.detail),
                2, null, R.drawable.ic_art_track_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        optionsDialog.hide();
                        Song song = new Song(current.getData());
                        activityManager.startSongDetailActivity(ImageWallActivity.this, song, false);
                    }
                }
        );

        optionsAdapter.addOption(save);
        optionsAdapter.addOption(jumpToSource);
        optionsAdapter.addOption(detail);
    }

    private void initViews() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.image_wall_list);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemMoreClickListener(this);
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);

        int mainBC = cs[3];
        int vicBC = cs[4];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int accentC = cs[2];

        optionsDialog.setTitleBarBgColor(vicBC);
        optionsDialog.setContentBgColor(mainBC);
        optionsDialog.setDivideColor(vicTC);
        optionsDialog.setTitleTextColor(mainTC);

        optionsAdapter.setTitleColor(mainTC);
        optionsAdapter.setIconColor(accentC);

    }

    @Override
    public void onItemClick(ImageAdapter.ViewHolder view, SongInfo d, int position) {
        current = d;
        activityManager.startImageCheckActivity(this, d.getAlbum_path());
    }

    @Override
    public void onItemMore(ImageAdapter.ViewHolder view, SongInfo d, int position) {
        current = d;
        if (optionsDialog != null) {
            if (optionsDialog.visible()) {
                optionsDialog.hide();
            } else {
                optionsDialog.show();
            }
        }
    }
}
