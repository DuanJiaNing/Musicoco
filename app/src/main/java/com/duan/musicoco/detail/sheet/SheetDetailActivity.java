package com.duan.musicoco.detail.sheet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.OnCompleteListener;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.SheetOperation;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

import java.util.ArrayList;
import java.util.List;

public class SheetDetailActivity extends RootActivity implements ThemeChangeable {

    private static final String TAG = "SheetDetailActivity";

    private SheetInfoController infoController;
    private SheetSongListController songListController;
    private Toolbar toolbar;
    private Menu menu;

    private MediaManager mediaManager;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private FloatingActionButton toTop;
    private RecyclerView songList;

    private AppBarStateChangeListener barStateChangeListener;
    private SheetOperation sheetOperation;
    private SongOperation songOperation;
    private IPlayControl control;
    private BroadcastReceiver songsChangeReceiver;
    private BroadcastManager broadcastManager;

    private Sheet sheet;
    private int sheetID;
    private Song locationAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_detail);

        Utils.transitionStatusBar(this);

        boolean darkTheme = appPreference.getTheme() == ThemeEnum.DARK;
        infoController = new SheetInfoController(this, darkTheme);
        songListController = new SheetSongListController(this);
        mediaManager = MediaManager.getInstance();
        control = MainActivity.getControl();
        sheetOperation = new SheetOperation(this, control, dbController);
        songOperation = new SongOperation(this, control, dbController);
        broadcastManager = BroadcastManager.getInstance();

        getSheet();
        initViews();
        initData();
        initBroadcast();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        locationAt = intent.getParcelableExtra(ActivityManager.SHEET_DETAIL_LOCATION_AT);
        handleListLocation(false);
    }

    // delay 是否延迟滚动
    private void handleListLocation(boolean delay) {
        if (locationAt != null) {
            songListController.locationAt(locationAt, delay);
        }
    }

    @Override
    public void onBackPressed() {
        if (songListController != null) {
            if (songListController.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void initBroadcast() {
        songsChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songListController.update();
            }
        };
        broadcastManager.registerBroadReceiver(this, songsChangeReceiver, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(this, songsChangeReceiver);
    }

    @Override
    protected void checkTheme() {
        ThemeEnum themeEnum = appPreference.getTheme();
        if (themeEnum == ThemeEnum.DARK) {
            this.setTheme(R.style.Theme_Sheet_Detail_DARK);
        } else if (themeEnum == ThemeEnum.WHITE) {
            this.setTheme(R.style.Theme_Sheet_Detail_WHITE);
        }
    }

    private void getSheet() {
        Intent intent = getIntent();

        int si = intent.getIntExtra(ActivityManager.SHEET_DETAIL_ID, Integer.MAX_VALUE);
        if (Integer.MAX_VALUE != si) {
            if (si < 0) {
                sheetID = si;
                sheet = null;
            } else {
                sheet = dbController.getSheet(si);
                if (sheet == null) {
                    ToastUtils.showShortToast(getString(R.string.error_load_sheet_fail), this);
                    finish();
                } else {
                    sheetID = si;
                }
            }
        }

        locationAt = intent.getParcelableExtra(ActivityManager.SHEET_DETAIL_LOCATION_AT);
    }

    private void initData() {

        infoController.initData(sheetID, sheet, dbController, mediaManager);
        songListController.initData(sheetID, control, dbController, mediaManager);
        handleListLocation(true);

        themeChange(null, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sheet_detail, menu);
        this.menu = menu;
        filterMenu();
        updateToolbarColor();
        return true;
    }

    private void filterMenu() {
        if (sheetID < 0) {

            // 主歌单歌单信息不允许修改
            menu.removeItem(R.id.sheet_detail_action_modify);

            // 主歌单没有【移除多首歌曲】选项，我的收藏的由【多项取消收藏】选项
            menu.removeItem(R.id.sheet_detail_multi_remove);

            // 我的收藏 歌单不需要【收藏所有】
            if (sheetID == MainSheetHelper.SHEET_FAVORITE) {
                menu.removeItem(R.id.sheet_detail_action_collection);
            }
        }
        setMultiModeMenuVisible(false);
    }

    public void setMultiModeMenuVisible(boolean visible) {
        MenuItem c = menu.findItem(R.id.sheet_detail_action_collection);
        MenuItem m = menu.findItem(R.id.sheet_detail_action_modify);
        MenuItem s = menu.findItem(R.id.sheet_detail_search);
        MenuItem maf = menu.findItem(R.id.sheet_detail_multi_add_favorite);
        MenuItem mas = menu.findItem(R.id.sheet_detail_multi_add_to_sheet);
        MenuItem mr = menu.findItem(R.id.sheet_detail_multi_remove);
        MenuItem md = menu.findItem(R.id.sheet_detail_multi_delete_songs);

        if (c != null) {
            c.setVisible(!visible);
        }
        if (m != null) {
            m.setVisible(!visible);
        }
        if (s != null) {
            s.setVisible(!visible);
        }
        if (maf != null) {
            maf.setVisible(visible);
        }
        if (mas != null) {
            mas.setVisible(visible);
        }
        if (mr != null) {
            mr.setVisible(visible);
        }
        if (md != null) {
            md.setVisible(visible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        OnCompleteListener<Void> complete = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Void aVoid) {
                songListController.onBackPressed();
            }
        };

        switch (id) {
            case R.id.sheet_detail_search:
                if (songList.getChildCount() == 0) {
                    String msg = getString(R.string.error_empty_sheet);
                    ToastUtils.showShortToast(msg, this);
                } else {
                    ActivityManager.getInstance().startSearchActivity(this, sheetID);
                }
                break;
            case R.id.sheet_detail_action_collection:
                if (songList.getChildCount() == 0) {
                    String msg = getString(R.string.error_empty_sheet);
                    ToastUtils.showShortToast(msg, this);
                } else {
                    songOperation.handleAddAllSongToFavorite(sheetID);
                }
                break;
            case R.id.sheet_detail_action_modify:
                sheetOperation.modifySheet(sheet);
                break;
            case android.R.id.home:
                if (songListController.onBackPressed()) {
                    finish();
                }
                break;
            case R.id.sheet_detail_multi_add_to_sheet: // 添加多首歌曲到歌单
                if (!songListController.checkSelectedEmpty()) {
                    List<Song> songs = songListController.getCheckItemsIndex();
                    songOperation.handleAddSongToSheet(songs, complete);
                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg, this);
                }
                break;
            case R.id.sheet_detail_multi_delete_songs: // 彻底删除多首歌曲
                if (!songListController.checkSelectedEmpty()) {
                    List<Song> songs = songListController.getCheckItemsIndex();
                    songOperation.handleDeleteSongForever(complete, sheetID, songs);
                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg, this);
                }
                break;
            case R.id.sheet_detail_multi_add_favorite: // 添加多首歌曲到[我的收藏]
                if (!songListController.checkSelectedEmpty()) {
                    handleAddSelectSongToFavorite(complete);
                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg, this);
                }
                break;
            case R.id.sheet_detail_multi_remove: // 从当前歌单移除多首歌曲
                if (!songListController.checkSelectedEmpty()) {

                    List<Song> songs = songListController.getCheckItemsIndex();
                    if (songListController.isCurrentSheetPlaying()) {
                        songOperation.handleRemoveSongFromCurrentPlayingSheet(complete, songs);
                    } else {
                        songOperation.handleRemoveSongFromSheetNotPlaying(complete, sheetID, songs);
                    }

                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg, this);
                }
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void handleAddSelectSongToFavorite(OnCompleteListener<Void> complete) {
        List<SongAdapter.DataHolder> holder = songListController.getCheckItemsDataHolder();

        int favoriteCount = 0;
        List<Song> songs = new ArrayList<>();
        for (SongAdapter.DataHolder h : holder) {
            songs.add(new Song(h.info.getData()));
            if (h.isFavorite) {
                favoriteCount++;
            }
        }

        if (sheetID == MainSheetHelper.SHEET_FAVORITE || favoriteCount == holder.size()) { // 选中全为收藏歌曲
            songOperation.handleSelectSongCancelFavorite(songs, complete);
        } else if (favoriteCount == 0) { // 选中歌曲全为非收藏歌曲
            songOperation.handleSelectSongAddToFavorite(songs, complete);
        } else { // 都有
            songOperation.handleSelectSongFavorite(songs, complete);
        }

    }

    private void initViews() {

        initSelfView();
        infoController.initView();
        songListController.initViews();

    }

    private void initSelfView() {
        toolbar = (Toolbar) findViewById(R.id.sheet_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toTop = (FloatingActionButton) findViewById(R.id.sheet_detail_top);
        initToTopPos();

        songList = (RecyclerView) findViewById(R.id.sheet_detail_songs_list);
        appBarLayout = (AppBarLayout) findViewById(R.id.sheet_detail_app_bar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.sheet_detail_toolbar_layout);
        barStateChangeListener = new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case EXPANDED:
                        collapsingToolbarLayout.setTitle(" ");
                        songList.setNestedScrollingEnabled(false);
                        transToTopBt(true);
                        break;
                    case COLLAPSED:
                        collapsingToolbarLayout.setTitle(infoController.getTitle());
                        songList.setNestedScrollingEnabled(true);
                        toTop.setVisibility(View.VISIBLE);
                        transToTopBt(false);
                        break;
                    case IDLE:
                        collapsingToolbarLayout.setTitle(" ");
                        break;
                }
            }
        };
        appBarLayout.addOnOffsetChangedListener(barStateChangeListener);

    }

    private void initToTopPos() {
        toTop.post(new Runnable() {
            @Override
            public void run() {
                toTopY = toTop.getY();
            }
        });
    }

    // 滚动到顶部 按钮的点击事件
    public void smoothScrollToTop(View view) {
        songListController.setUseAnim(false);
        songList.smoothScrollToPosition(0);
    }

    private float toTopY;
    private boolean isToTopShowing = false;

    private void transToTopBt(boolean hide) {
        if ((hide && !isToTopShowing) || (!hide && isToTopShowing)) {
            return;
        }

        isToTopShowing = !hide;
        float from = toTopY;
        float to = Utils.getMetrics(this).heightPixels;
        if (!hide) {
            from = to;
            to = toTopY;
        }
        int dur = getResources().getInteger(R.integer.anim_default_duration);
        AnimationUtils.startTranslateYAnim(from, to, dur, toTop, null);
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        ThemeEnum th = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, th);

        int statusC = cs[0];
        int toolbarC = cs[1];
        int accentC = cs[2];
        int mainBC = cs[3];
        int vicBC = cs[4];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int navC = cs[7];
        int toolbarMainTC = cs[8];
        int toolbarVicTC = cs[9];


        int[] sa = ColorUtils.get2ActionStatusBarColors(this);
        collapsingToolbarLayout.setStatusBarScrimColor(sa[0]);
        collapsingToolbarLayout.setContentScrimColor(sa[1]);

        songListController.themeChange(th, cs);

        updateFloatingBtColor(new int[]{accentC, toolbarMainTC, toolbarVicTC});
        infoController.themeChange(th, new int[]{toolbarMainTC, toolbarVicTC});

    }

    private void updateToolbarColor() {

        if (toolbar == null || collapsingToolbarLayout == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            int[] colors = ColorUtils.get2ToolbarTextColors(this);

            int mainTC = colors[0];
            collapsingToolbarLayout.setCollapsedTitleTextColor(mainTC);
            Drawable navD = toolbar.getNavigationIcon();
            if (navD != null) {
                navD.setTint(mainTC);
            }

            int menuCount = menu.size();
            int mainC = colors[0];
            for (int i = 0; i < menuCount; i++) {
                MenuItem item = menu.getItem(i);
                if (item != null) {
                    Drawable icon = item.getIcon();
                    if (icon != null) {
                        icon.setTint(mainC);
                    }
                }
            }
        }
    }

    private void updateFloatingBtColor(int[] colors) {

        int bgC = colors[0];
        int tintC = colors[1];
        int rippleC = colors[2];

        infoController.updateFloatingBtColor(new int[]{tintC, rippleC, bgC});

        toTop.setRippleColor(rippleC);
        toTop.setBackgroundTintList(ColorStateList.valueOf(bgC));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toTop.getDrawable().setTint(tintC);
        }

    }
}
