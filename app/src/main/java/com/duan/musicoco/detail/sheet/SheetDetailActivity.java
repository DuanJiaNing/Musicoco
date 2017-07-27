package com.duan.musicoco.detail.sheet;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

public class SheetDetailActivity extends AppCompatActivity implements OnThemeChange {

    private SheetInfoController infoController;
    private SheetSongListController songListController;
    private Toolbar toolbar;

    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private FloatingActionButton toTop;
    private RecyclerView songList;
    private View container;

    private AppBarStateChangeListener barStateChangeListener;
    private AppPreference appPreference;

    private Sheet sheet;
    private int sheetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_detail);

        Utils.transitionStatusBar(this);

        appPreference = new AppPreference(this);

        boolean darkTheme = appPreference.getTheme() == Theme.DARK;
        infoController = new SheetInfoController(this, darkTheme);

        songListController = new SheetSongListController(this);

        dbController = new DBMusicocoController(this, true);
        mediaManager = MediaManager.getInstance(this);

        getSheet();

        initViews();

        initData();

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
        songListController.initData(sheetID, dbController, mediaManager);
        themeChange(null, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sheet_detail, menu);
        initOptionsMenuColors(menu);
        return true;
    }

    private void initOptionsMenuColors(Menu menu) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Theme th = appPreference.getTheme();
            int color;
            switch (th) {
                case DARK:
                    color = ColorUtils.get2DarkThemeTextColor()[0];
                    break;
                case WHITE:
                default:
                    color = Color.WHITE;
                    break;
            }

            MenuItem heart = menu.findItem(R.id.sheet_detail_action_collection);
            MenuItem search = menu.findItem(R.id.sheet_detail_search);
            MenuItem edit = menu.findItem(R.id.sheet_detail_action_modify);

            heart.getIcon().setTint(color);
            search.getIcon().setTint(color);
            edit.getIcon().setTint(color);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sheet_detail_search:
                ToastUtils.showShortToast("search");
                break;
            case R.id.sheet_detail_action_collection:

                break;
            case R.id.sheet_detail_action_modify:

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

        container = findViewById(R.id.sheet_detail_song_list_container);
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
    public void themeChange(Theme theme, int[] colors) {

        Theme th = appPreference.getTheme();
        updateFloatingBtColor(th);
        int[] cs = new int[4];
        //标题栏和状态栏颜色
        int topC;
        switch (th) {
            case DARK:
                cs = ColorUtils.get4DarkThemeColors();
                topC = cs[2];
                break;
            case WHITE:
            default:
                cs = ColorUtils.get4WhiteThemeColors();
                topC = getResources().getColor(R.color.colorPrimary);
                break;
        }

        int mainBC = cs[0];
        int mainTC = cs[1];
        int vicBC = cs[2];
        int vicTC = cs[3];

        songListController.themeChange(th, cs);

        int[] cos = {
                th == Theme.DARK ? mainTC : mainBC, // 主字体色
                th == Theme.DARK ? vicTC : vicBC, // 辅字体色
        };
        infoController.themeChange(th, cos);

        collapsingToolbarLayout.setContentScrimColor(topC);
        collapsingToolbarLayout.setStatusBarScrimColor(topC);

        container.setBackgroundColor(mainBC);
        updateToolbarColor(th == Theme.DARK ? mainTC : mainBC);

    }

    private void updateToolbarColor(int textC) {
        collapsingToolbarLayout.setCollapsedTitleTextColor(textC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable navD = toolbar.getNavigationIcon();
            if (navD != null) {
                navD.setTint(textC);
            }
        }
    }

    private void updateFloatingBtColor(Theme theme) {
        int[] colors = new int[4];
        switch (theme) {
            case DARK:
                colors = ColorUtils.get4DarkDialogThemeColors();
                break;
            case WHITE:
            default:
                colors = ColorUtils.get4WhiteDialogThemeColors();
                break;
        }

        int tintC = colors[0];
        int rippleC = colors[2];
        int bgC = colors[3];

        infoController.updateFloatingBtColor(new int[]{tintC, rippleC, bgC});

        toTop.setRippleColor(rippleC);
        toTop.setBackgroundTintList(ColorStateList.valueOf(bgC));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toTop.getDrawable().setTint(tintC);
        }

    }
}
