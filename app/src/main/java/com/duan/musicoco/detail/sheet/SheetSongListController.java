package com.duan.musicoco.detail.sheet;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.OnCompleteListener;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.MediaUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/25.
 */

public class SheetSongListController implements
        View.OnClickListener,
        ThemeChangeable {

    private static final String TAG = "SheetSongListController";
    private ImageView random;
    private TextView playAllRandom;
    private View line;
    private View randomContainer;
    private RecyclerView songList;

    private View checkContainer;
    private TextView checkCount;
    private CheckBox checkAll;

    private final Activity activity;
    private SongAdapter songAdapter;
    private IPlayControl control;

    private int sheetID;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private int currentIndex = 0;
    private int songCount;
    private boolean needLocation = false;
    private Song locationAt;

    private final List<SongAdapter.DataHolder> data;

    private SongOperation songOperation;
    private OptionsDialog optionsDialog;
    private OptionsAdapter optionsAdapter;
    private SongAdapter.DataHolder currentSongData;

    private static final int OPTIONS_ID_ADD_TO_SHEET = 1;
    private static final int OPTIONS_SONG_DETAIL = 2;
    private static final int OPTIONS_DELETE_FOREVER = 3;
    private static final int OPTIONS_FAVORITE = 4;
    private static final int OPTIONS_REMOVE_FROM_SHEET = 5;

    private boolean isCurrentSheetPlaying = false;

    public SheetSongListController(Activity activity) {
        this.activity = activity;
        this.data = new ArrayList<>();
    }

    public void initViews() {
        random = (ImageView) activity.findViewById(R.id.sheet_detail_songs_icon);
        playAllRandom = (TextView) activity.findViewById(R.id.sheet_detail_songs_play_random);
        line = activity.findViewById(R.id.sheet_detail_songs_line);
        songList = (RecyclerView) activity.findViewById(R.id.sheet_detail_songs_list);
        randomContainer = activity.findViewById(R.id.sheet_detail_random_container);

        checkContainer = activity.findViewById(R.id.sheet_detail_check_container);
        checkAll = (CheckBox) activity.findViewById(R.id.sheet_detail_check_all);
        checkCount = (TextView) activity.findViewById(R.id.sheet_detail_check_count);
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = checkAll.isChecked();
                if (isChecked) {
                    songAdapter.checkAll();
                } else {
                    songAdapter.clearAllCheck();
                }
            }
        });

        randomContainer.setOnClickListener(this);
        FloatingActionButton fabPlayAll = (FloatingActionButton) activity.findViewById(R.id.sheet_detail_play_all);
        fabPlayAll.setOnClickListener(this);

        songList.post(new Runnable() {
            @Override
            public void run() {
                calculateRecycleViewHeight();
            }
        });
    }

    //计算 RecycleView 高度，否则无法复用 item
    private void calculateRecycleViewHeight() {
        ActionBar bar = ((AppCompatActivity) activity).getSupportActionBar();
        if (bar != null) {
            int actionH = bar.getHeight();
            int randomCH = randomContainer.getHeight();
            int statusH = Utils.getStatusBarHeight(activity);
            int screenHeight = Utils.getMetrics(activity).heightPixels;
            int height = screenHeight - actionH - statusH - randomCH;

            ViewGroup.LayoutParams params = songList.getLayoutParams();
            params.height = height;
            songList.setLayoutParams(params);
        }
    }

    public void initData(int sheetID, IPlayControl control, DBMusicocoController dbController, MediaManager mediaManager) {
        this.sheetID = sheetID;
        this.dbController = dbController;
        this.mediaManager = mediaManager;

        this.control = control;

        initSongList();
        initOptionsDialog();

    }

    private void initOptionsDialog() {

        optionsDialog = new OptionsDialog(activity);
        optionsAdapter = new OptionsAdapter(activity, getIconsID(), getIds(), getTexts(), null, null);
        songOperation = new SongOperation(activity, control, dbController);
        optionsDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = new Song(currentSongData.info.getData());
                SongInfo info = currentSongData.info;
                switch (position) {
                    case 0: //收藏到歌单
                        songOperation.handleAddSongToSheet(info);
                        break;
                    case 1: //详细信息
                        ActivityManager.getInstance().startSongDetailActivity(activity, song, false);
                        break;
                    case 2: //彻底删除
                        songOperation.handleDeleteSongForever(song, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Void aVoid) {
                                update();
                            }
                        });
                        break;
                    case 3: //收藏
                        songOperation.reverseSongFavoriteStatus(song);
                        update();
                        break;
                    case 4: //从歌单中移除(非主歌单才有该选项)
                        if (isCurrentSheetPlaying) {
                            songOperation.removeSongFromCurrentPlayingSheet(null, song);
                        } else {
                            songOperation.removeSongFromSheetNotPlaying(null, sheetID, song);
                        }
                        update();

                        break;
                }
                optionsDialog.hide();
            }
        });
        optionsDialog.setAdapter(optionsAdapter);

    }

    private void initSongList() {

        songAdapter = new SongAdapter(activity, data, sheetID);
        LinearLayoutManager lm = new LinearLayoutManager(activity);
        songList.setLayoutManager(lm);
        songList.setAdapter(songAdapter);

        songList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (!activity.isDestroyed()) {
                            Glide.with(activity).resumeRequests();
                        }
                    }
                } else {
                    Glide.with(activity).pauseRequests();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    songAdapter.setUseAnim(false);
                } else {
                    songAdapter.setUseAnim(true);
                }
            }
        });
        setSongAdapterListeners();
        //在 calculateRecycleViewHeight 之后再更新
        songList.post(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });

    }

    private void setSongAdapterListeners() {
        songAdapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SongAdapter.ViewHolder view, SongAdapter.DataHolder data, int position) {
                playSong(position);
            }
        });

        songAdapter.setOnMoreClickListener(new SongAdapter.OnMoreClickListener() {
            @Override
            public void onMore(SongAdapter.ViewHolder view, SongAdapter.DataHolder data, int position) {
                currentSongData = data;
                if (optionsDialog.visible()) {
                    optionsDialog.hide();
                } else {
                    updateSongFavoriteOptions();
                    String title = activity.getString(R.string.song) + ": " + data.info.getTitle();
                    optionsDialog.setTitle(title);
                    optionsDialog.show();
                }
            }
        });

        songAdapter.setOnItemLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                songAdapter.setMultiselectionModeEnable(true);
                switchMultiselectionMode(true);
                return true;
            }
        });

        songAdapter.setOnCheckStatusChangedListener(new SongAdapter.OnItemCheckStatusChangedListener() {
            @Override
            public void itemCheckChanged(int position, boolean check) {
                List<?> checked = songAdapter.getCheckItemsIndex();
                String count = activity.getString(R.string.select_song);
                boolean isAllChecked = false;
                if (checked != null) {
                    int itemCount = songAdapter.getItemCount();
                    count = checked.size() + "/" + itemCount;
                    if (itemCount == checked.size()) {
                        isAllChecked = true;
                    }
                }

                checkCount.setText(count);
                checkAll.setChecked(isAllChecked);

            }
        });

    }

    private void playSong(int position) {
        try {
            int id = control.getPlayListId();
            int index = control.currentSongIndex();

            if (id == sheetID) { // 当前歌单
                if (position == index) {
                    // UPDATE: 2017/8/26 更新 在另外的歌单删除当前歌单中正在播放的歌曲
                    if (control.status() != PlayController.STATUS_PLAYING) {
                        control.resume();
                    }
                } else {
                    control.playByIndex(position);
                    isCurrentSheetPlaying = true;
                    songAdapter.update(isCurrentSheetPlaying, position);

                }
            } else {
                control.setPlaySheet(sheetID, position);
                control.resume();
                isCurrentSheetPlaying = true;
                songAdapter.update(isCurrentSheetPlaying, position);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateSongFavoriteOptions() {

        String addFavorite = activity.getString(R.string.song_operation_add_to_favorite);
        String removeFavorite = activity.getString(R.string.song_operation_remove_from_favorite);
        OptionsAdapter.Option op = optionsAdapter.getOption(OPTIONS_FAVORITE);
        if (op != null) {

            if (currentSongData.isFavorite) {
                op.title = removeFavorite;
            } else {
                op.title = addFavorite;
            }
        }
        optionsAdapter.notifyDataSetChanged();

    }

    private int[] getIconsID() {
        int[] res = {
                R.drawable.ic_create_new_folder_black_24dp,
                R.drawable.ic_art_track_black_24dp,
                R.drawable.ic_delete_forever_black_24dp,
                R.drawable.ic_favorite_border,
                R.drawable.ic_clear_black_24dp
        };

        int[] ids;
        if (sheetID < 0) {
            ids = Arrays.copyOf(res, 4);
        } else {
            ids = res;
        }
        return ids;
    }

    private int[] getIds() {
        int[] res = {
                OPTIONS_ID_ADD_TO_SHEET,
                OPTIONS_SONG_DETAIL,
                OPTIONS_DELETE_FOREVER,
                OPTIONS_FAVORITE,
                OPTIONS_REMOVE_FROM_SHEET
        };

        int[] ids;
        if (sheetID < 0) {
            ids = Arrays.copyOf(res, 4);
        } else {
            ids = res;
        }
        return ids;

    }

    private String[] getTexts() {
        String[] res = {
                activity.getString(R.string.title_add_to_sheet),
                activity.getString(R.string.song_operation_detail),
                activity.getString(R.string.song_operation_delete),
                activity.getString(R.string.song_operation_add_to_favorite),
                activity.getString(R.string.song_operation_remove_from_sheet)
        };

        String[] sts;
        if (sheetID < 0) {
            sts = Arrays.copyOf(res, 4);
        } else {
            sts = res;
        }
        return sts;
    }

    public void update() {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                List<DBSongInfo> ds;
                if (sheetID < 0) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbController);
                    ds = helper.getMainSheetSongInfo(sheetID);
                } else {
                    ds = dbController.getSongInfos(sheetID);
                }
                List<SongInfo> da = MediaUtils.DBSongInfoToSongInfoList(activity, ds, mediaManager);

                data.clear();
                for (int i = 0; i < da.size(); i++) {
                    SongInfo info = da.get(i);
                    boolean fa = ds.get(i).favorite;
                    SongAdapter.DataHolder dh = new SongAdapter.DataHolder(info, fa);
                    data.add(dh);
                }

                songCount = data.size();

                try {
                    int curID = control.getPlayListId();
                    if (curID == sheetID) {
                        isCurrentSheetPlaying = true;
                        currentIndex = control.currentSongIndex();
                    } else {
                        isCurrentSheetPlaying = false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(isCurrentSheetPlaying);
            }
        };

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean playing) {
                        songAdapter.update(playing, currentIndex);

                        if (needLocation) {
                            location();
                            needLocation = false;
                        }

                        String str = activity.getString(R.string.replace_play_all_song_random);
                        String res = str.replace("*", String.valueOf(songCount));
                        playAllRandom.setText(res);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sheet_detail_random_container:
                playAll(true);
                break;
            case R.id.sheet_detail_play_all:
                if (songAdapter.getMultiselectionModeEnable()) {
                    songAdapter.setMultiselectionModeEnable(false);
                    switchMultiselectionMode(false);
                } else {
                    playAll(false);
                }
                break;
        }
    }

    private void playAll(boolean isRandom) {

        if (songAdapter.getItemCount() <= 0) {
            return;
        }

        try {
            Song song = null;
            if (isRandom) {
                control.setPlayMode(PlayController.MODE_RANDOM);
                Random random = new Random();
                List<DBSongInfo> infos;
                if (sheetID < 0) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbController);
                    infos = helper.getMainSheetSongInfo(sheetID);
                } else {
                    infos = dbController.getSongInfos(sheetID);
                }
                song = control.setPlaySheet(sheetID, random.nextInt(infos.size()));
            } else {
                song = control.setPlaySheet(sheetID, 0);
            }
            control.resume();

            if (song != null) {
                activity.finish();
                ActivityManager.getInstance().startPlayActivity(activity);
            } else {
                ToastUtils.showShortToast(activity.getString(R.string.error_non_song_to_play), activity);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

        songAdapter.themeChange(themeEnum, new int[]{mainTC, vicTC, accentC});

        playAllRandom.setTextColor(mainTC);
        line.setBackgroundColor(vicTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            random.getDrawable().setTint(vicTC);
        }

        optionsDialog.setTitleBarBgColor(vicBC);
        optionsDialog.setContentBgColor(mainBC);
        optionsDialog.setDivideColor(vicTC);
        optionsDialog.setTitleTextColor(mainTC);

        optionsAdapter.setTitleColor(mainTC);
        optionsAdapter.setIconColor(accentC);
    }

    public boolean onBackPressed() {
        if (songAdapter.getMultiselectionModeEnable()) {
            songAdapter.setMultiselectionModeEnable(false);
            switchMultiselectionMode(false);
            return false;
        }
        return true;
    }

    private void switchMultiselectionMode(boolean mulit) {
        if (mulit) {
            showCheckAllView();
            ((SheetDetailActivity) activity).setMultiModeMenuVisible(true);
        } else {
            hideCheckAllView();
            ((SheetDetailActivity) activity).setMultiModeMenuVisible(false);
        }
    }

    private void hideCheckAllView() {
        checkContainer.setVisibility(View.GONE);

        randomContainer.setClickable(true);
        randomContainer.setOnClickListener(this);
        random.setVisibility(View.VISIBLE);
        playAllRandom.setVisibility(View.VISIBLE);
    }

    private void showCheckAllView() {
        checkContainer.setVisibility(View.VISIBLE);
        TextView all = (TextView) activity.findViewById(R.id.sheet_detail_check_all_);
        int vicTC = ((ColorDrawable) line.getBackground()).getColor();
        int mainTC = playAllRandom.getCurrentTextColor();
        all.setTextColor(vicTC);
        checkCount.setTextColor(mainTC);
        checkCount.setText(R.string.select_song);
        checkAll.setChecked(false);

        randomContainer.setClickable(false);
        random.setVisibility(View.GONE);
        playAllRandom.setVisibility(View.GONE);
    }

    public List<Integer> getSelectSongIndex() {
        return songAdapter.getCheckItemsIndex();
    }

    // 没有歌曲选中返回 true
    public boolean checkSelectedEmpty() {
        List<?> list = getSelectSongIndex();
        return list == null || list.size() == 0;
    }

    public List<Song> getCheckItemsIndex() {
        List<Integer> list = songAdapter.getCheckItemsIndex();
        List<Song> songs = new ArrayList<>();
        for (int index : list) {
            SongAdapter.DataHolder item = songAdapter.getItem(index);
            Song song = new Song(item.info.getData());
            songs.add(song);
        }
        return songs;
    }

    public List<SongAdapter.DataHolder> getCheckItemsDataHolder() {
        List<Integer> list = songAdapter.getCheckItemsIndex();
        List<SongAdapter.DataHolder> dataHolders = new ArrayList<>();
        for (int index : list) {
            SongAdapter.DataHolder item = songAdapter.getItem(index);
            dataHolders.add(item);
        }
        return dataHolders;
    }

    public boolean isCurrentSheetPlaying() {
        return isCurrentSheetPlaying;
    }

    public void locationAt(Song locationAt, boolean delay) {
        if (locationAt == null || TextUtils.isEmpty(locationAt.path)) {
            return;
        }
        this.locationAt = locationAt;

        if (delay) {
            needLocation = true;
        } else {
            location();
        }
    }

    private void location() {
        int index;
        for (index = 0; index < data.size(); index++) {
            String path = data.get(index).info.getData();
            if (path.equals(locationAt.path)) {
                break;
            }
        }
        songList.smoothScrollToPosition(index);
    }

    public void setUseAnim(boolean anim) {
        songAdapter.setUseAnim(anim);
    }
}
