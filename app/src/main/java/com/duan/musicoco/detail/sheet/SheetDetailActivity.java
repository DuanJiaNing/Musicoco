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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.SheetsOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

public class SheetDetailActivity extends AppCompatActivity implements OnThemeChange {

    private static final String TAG = "SheetDetailActivity";

    private SheetInfoController infoController;
    private SheetSongListController songListController;
    private Toolbar toolbar;
    private Menu menu;

    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private FloatingActionButton toTop;
    private RecyclerView songList;

    private AppBarStateChangeListener barStateChangeListener;
    private AppPreference appPreference;
    private SheetsOperation sheetsOperation;
    private IPlayControl control;
    private BroadcastReceiver songsChangeReceiver;
    private BroadcastManager broadcastManager;

    private Sheet sheet;
    private int sheetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreference = new AppPreference(this);
        checkTheme();
        setContentView(R.layout.activity_sheet_detail);

        Utils.transitionStatusBar(this);

        boolean darkTheme = appPreference.getTheme() == ThemeEnum.DARK;
        infoController = new SheetInfoController(this, darkTheme);
        songListController = new SheetSongListController(this);
        dbController = new DBMusicocoController(this, true);
        mediaManager = MediaManager.getInstance(this);
        control = MainActivity.getControl();
        sheetsOperation = new SheetsOperation(this, control, dbController);
        broadcastManager = BroadcastManager.getInstance(this);

        getSheet();
        initViews();
        initData();
        initBroadcast();

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
                Log.d(TAG, "onReceive: songsChangeReceiver");
                songListController.update();
            }
        };
        broadcastManager.registerBroadReceiver(songsChangeReceiver, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_CHANGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(songsChangeReceiver);
    }

    private void checkTheme() {
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
                    ToastUtils.showShortToast(getString(R.string.error_load_sheet_fail));
                    finish();
                } else {
                    sheetID = si;
                }
            }
        }
    }

    private void initData() {

        infoController.initData(sheetID, sheet, dbController, mediaManager);
        songListController.initData(sheetID, control, dbController, mediaManager);
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
            menu.removeItem(R.id.sheet_detail_action_modify);
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
        s.setVisible(!visible);

        maf.setVisible(visible);
        mas.setVisible(visible);
        mr.setVisible(visible);
        md.setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sheet_detail_search:
                //TODO
                ToastUtils.showShortToast("sheet_detail_search");
                break;
            case R.id.sheet_detail_action_collection:
                if (songList.getChildCount() == 0) {
                    String msg = getString(R.string.error_empty_sheet);
                    ToastUtils.showShortToast(msg);
                } else {
                    sheetsOperation.handleAddAllSongToFavorite(sheetID);
                }
                break;
            case R.id.sheet_detail_action_modify:
                sheetsOperation.handleModifySheet(sheet);
                break;
            case android.R.id.home:
                if (songListController.onBackPressed()) {
                    finish();
                }
                break;
            case R.id.sheet_detail_multi_add_to_sheet: // 收藏多首歌曲到歌单
                if (!songListController.checkSelectedEmpty()) {

                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg);
                }
                break;
            case R.id.sheet_detail_multi_delete_songs: // 彻底删除多首歌曲
                if (!songListController.checkSelectedEmpty()) {

                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg);
                }
                break;
            case R.id.sheet_detail_multi_add_favorite: // 添加多首歌曲到[我的收藏]
                if (!songListController.checkSelectedEmpty()) {

                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg);
                }
                break;
            case R.id.sheet_detail_multi_remove: // 从当前歌单移除
                if (!songListController.checkSelectedEmpty()) {

                } else {
                    String msg = getString(R.string.error_non_song_select);
                    ToastUtils.showShortToast(msg);
                }
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
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
        //FIXME view.post 和 addOnGlobalLayoutListener 都有获取为 0 的情况
        toTop.post(new Runnable() {
            @Override
            public void run() {
                toTopY = toTop.getY();
            }
        });
    }

    public void smoothScrollToTop(View view) {
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
        int[] cs;
        switch (th) {
            case DARK:
                cs = ColorUtils.get10DarkThemeColors(this);
                break;
            case WHITE:
            default:
                cs = ColorUtils.get10WhiteThemeColors(this);
                break;
        }

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

        songListController.themeChange(th, cs);

        collapsingToolbarLayout.setContentScrimColor(toolbarC);
        collapsingToolbarLayout.setStatusBarScrimColor(statusC);

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
