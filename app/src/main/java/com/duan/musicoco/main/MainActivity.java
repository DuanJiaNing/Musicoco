package com.duan.musicoco.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.InspectActivity;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.main.bottomnav.BottomNavigationController;
import com.duan.musicoco.main.leftnav.LeftNavigationController;
import com.duan.musicoco.play.PlayServiceConnection;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

public class MainActivity extends InspectActivity implements
        OnServiceConnect,
        ThemeChangeable,
        ContentUpdatable {

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private Menu menu;

    // FIXME 内存泄漏
    private static PlayServiceConnection sServiceConnection;
    private PlayServiceManager playServiceManager;
    private IPlayControl control;

    private BottomNavigationController bottomNavigationController;
    private LeftNavigationController leftNavigationController;
    private RecentMostPlayController mostPlayController;
    private MainSheetsController mainSheetsController;
    private MySheetsController mySheetsController;

    private BroadcastReceiver mySheetDataChangedReceiver;
    private BroadcastManager broadcastManager;

    private boolean updateColorByCustomThemeColor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.transitionStatusBar(this);
        setContentView(R.layout.activity_main);

        //权限检查完成后回调 permissionGranted 或 permissionDenied
        checkPermission();

    }

    @Override
    public void permissionGranted(int requestCode) {

        playServiceManager = new PlayServiceManager(this);
        broadcastManager = BroadcastManager.getInstance(this);
        bottomNavigationController = new BottomNavigationController(this, mediaManager);
        leftNavigationController = new LeftNavigationController(this, appPreference);
        mostPlayController = new RecentMostPlayController(this, mediaManager);
        mainSheetsController = new MainSheetsController(this, mediaManager);
        mySheetsController = new MySheetsController(this, dbController, mediaManager);

        initViews();
        bindService();

    }

    private void initViews() {
        mySheetsController.initView();
        initSelfViews();
        bottomNavigationController.initView();
        mostPlayController.initView();
        mainSheetsController.initView();
        leftNavigationController.initViews();
    }

    private void bindService() {
        // FIXME 耗时
        startService();
        // FIXME 耗时
        prepareData();
        // FIXME 耗时 !!
        initAppDataIfNeed();

        sServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
        // 绑定成功后回调 onConnected
        playServiceManager.bindService(sServiceConnection);

    }

    @Override
    public void onConnected(ComponentName name, IBinder service) {
        this.control = IPlayControl.Stub.asInterface(service);

        initSelfData();

        // 初始化完成后进行数据更新
        update(null, null);

        // 最后更新界面
        themeChange(null, null);

        initBroadcastReceivers();
    }

    private void initSelfData() {
        bottomNavigationController.initData(control, dbController);
        mostPlayController.initData(dbController);
        mainSheetsController.initData(dbController);
        mySheetsController.initData(control);
        leftNavigationController.initData(dbController);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged statusChanged) {

        bottomNavigationController.update(obj, statusChanged);
        mostPlayController.update(getString(R.string.rmp_history), statusChanged);
        mainSheetsController.update(obj, statusChanged);
        mySheetsController.update(obj, statusChanged);
    }

    @Override
    public void noData() {
        // 没有数据的处理行为在各个 Controller 中完成
    }

    private void initBroadcastReceivers() {
        mySheetDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mySheetsController.update(null, null);
            }
        };
        broadcastManager.registerBroadReceiver(mySheetDataChangedReceiver, BroadcastManager.FILTER_MY_SHEET_CHANGED);
    }

    private void initSelfViews() {

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        final View topContainer = findViewById(R.id.activity_main_top_container);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                // fitsSystemWindows 为 false ，这里要增加 padding 填满状态栏
                toolbar.setPadding(0, Utils.getStatusBarHeight(MainActivity.this), 0, 0);
                // CollapsingToolbarLayout 下的 LinearLayout 也需要增加 padding
                topContainer.setPadding(0, Utils.getStatusBarHeight(MainActivity.this), 0, 0);
            }
        });
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.activity_main_app_bar);
        AppBarStateChangeListener barStateChangeListener = new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case EXPANDED:
                        mySheetsController.setLineVisible(false);
                        break;
                    case COLLAPSED:
                        mySheetsController.setLineVisible(true);
                        break;
                    case IDLE:
                        mySheetsController.setLineVisible(false);
                        break;
                }
            }
        };
        appBarLayout.addOnOffsetChangedListener(barStateChangeListener);
    }

    //--------------------------------------------------------------------//--------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationController != null && bottomNavigationController.hasInitData()) {
            // 启动应用时不需要更新，在 initSelfData 中统一更新全部
            bottomNavigationController.update(null, null);
        }

        if (mySheetsController != null && mySheetsController.hasInitData()) {
            mySheetsController.update(null, null);
        }

        if (mainSheetsController != null && mainSheetsController.hasInitData()) {
            mainSheetsController.update(null, null);
        }

        if (mostPlayController != null && mostPlayController.hasInitData()) {
            mostPlayController.update(getString(R.string.rmp_history), null);
        }

        if (updateColorByCustomThemeColor) {
            themeChange(null, null);
            switchThemeMode(appPreference.getTheme());
            updateColorByCustomThemeColor = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService();
        unregisterReceiver();

        if (dbController != null) {
            dbController.close();
        }
    }

    @Override
    public void finish() {
        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_PLAY_SERVICE_QUIT, null);
        super.finish();
    }

    private void unregisterReceiver() {
        if (mySheetDataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(mySheetDataChangedReceiver);
        }
    }

    private void unbindService() {
        if (sServiceConnection != null && sServiceConnection.hasConnected) {
            sServiceConnection.unregisterListener();
            unbindService(sServiceConnection);
            sServiceConnection.hasConnected = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (leftNavigationController.onBackPressed()) {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bottomNavigationController != null) {
            bottomNavigationController.stopProgressUpdateTask();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        this.menu = menu;
        updateToolbarColors();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_main_search:
                int sheetID = MainSheetHelper.SHEET_ALL;
                ActivityManager.getInstance(this).startSearchActivity(sheetID);
                break;
            case android.R.id.home:
                if (leftNavigationController.visible()) {
                    leftNavigationController.hide();
                } else {
                    leftNavigationController.show();
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    public void disConnected(ComponentName name) {
        sServiceConnection = null;
        sServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
        // 重新绑定
        playServiceManager.bindService(sServiceConnection);
    }

    @Override
    public void themeChange(ThemeEnum t, int[] colors) {
        ThemeEnum themeEnum = appPreference.getTheme();
        bottomNavigationController.themeChange(themeEnum, null);
        mostPlayController.themeChange(themeEnum, null);
        mySheetsController.themeChange(themeEnum, null);
        updateToolbarColors();
    }

    // 文字和图标颜色
    private void updateToolbarColors() {
        if (toolbar == null || toggle == null) {
            return;
        }

        int[] colors = ColorUtils.get2ToolbarTextColors(this);

        int mainTC = colors[0];
        toolbar.setTitleTextColor(mainTC);
        toggle.getDrawerArrowDrawable().setColor(mainTC);

        if (menu != null) {
            MenuItem search = menu.findItem(R.id.action_main_search);
            if (search != null) {
                Drawable icon = search.getIcon();
                if (icon != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        icon.setTint(mainTC);
                    }
                }
            }
        }

        CollapsingToolbarLayout coll = (CollapsingToolbarLayout) findViewById(R.id.activity_main_coll_tool_bar);
        int[] cs = ColorUtils.get2ActionStatusBarColors(this);
        coll.setStatusBarScrimColor(cs[0]);
        coll.setContentScrimColor(cs[1]);

        toolbar.setBackgroundColor(cs[1]);

        // 为了使状态栏透明，不要设置颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(cs[0]);
        }
    }

    /**
     * 白天模式和夜间模式主题切换
     */
    public void switchThemeMode(final ThemeEnum theme) {
        int[] colors = ColorUtils.get10ThemeColors(this, theme);
        int to = colors[3];

        View view = getWindow().getDecorView();
        view.setBackgroundColor(to);
        leftNavigationController.themeChange(theme, null);
        themeChange(null, null);

    }

    /**
     * 设置标志需要更新主题
     * LeftNavigationController【主题色定制】时调用
     * 应用标题栏和图标颜色改变
     */
    public void updateColorByCustomThemeColor() {
        this.updateColorByCustomThemeColor = true;
    }

    public static IPlayControl getControl() {
        return sServiceConnection.takeControl();
    }
}
