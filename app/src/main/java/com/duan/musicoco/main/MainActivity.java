package com.duan.musicoco.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
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
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.main.bottomnav.BottomNavigationController;
import com.duan.musicoco.main.leftnav.LeftNavigationController;
import com.duan.musicoco.play.PlayActivity;
import com.duan.musicoco.play.PlayServiceConnection;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.HeadphoneWireControlReceiver;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.setting.AutoSwitchThemeController;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

public class MainActivity extends RootActivity implements
        OnServiceConnect,
        ThemeChangeable,
        ContentUpdatable {

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private Menu menu;

    // UPDATE: 2017/8/26 修复 内存泄漏
    private static PlayServiceConnection sServiceConnection;
    private PlayServiceManager playServiceManager;
    protected MediaManager mediaManager;
    protected IPlayControl control;

    private BottomNavigationController bottomNavigationController;
    private LeftNavigationController leftNavigationController;
    private RecentMostPlayController mostPlayController;
    private MainSheetsController mainSheetsController;
    private MySheetsController mySheetsController;

    private BroadcastReceiver mySheetDataUpdateReceiver;
    private BroadcastReceiver appQuitTimeCountdownReceiver;
    private BroadcastReceiver appThemeChangeAutomaticReceiver;
    private BroadcastReceiver headsetPlugReceiver;
    private BroadcastReceiver mainSheetUpdateReceiver;
    private BroadcastManager broadcastManager;

    private boolean updateColorByCustomThemeColor = false;

    // 刚打开应用时忽略耳机是否插入的广播
    private boolean justOpenTheApplication = true;

    private ComponentName headphoneWireControlReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.transitionStatusBar(this);
        setContentView(R.layout.activity_main);

        playServiceManager = new PlayServiceManager(this);
        mediaManager = MediaManager.getInstance();

        // 单例持有的 Context 为 MainActivity 的，最早调用在此。
        broadcastManager = BroadcastManager.getInstance();
        bottomNavigationController = new BottomNavigationController(this, mediaManager);
        leftNavigationController = new LeftNavigationController(this, appPreference, auxiliaryPreference);
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

        //注册广播
        initBroadcastReceivers();

        // 应用偏好，打开应用自动播放 ?
        initPlayStatus();

    }

    private void initPlayStatus() {

        if (settingPreference.openAutoPlay()) {
            try {

                if (control.currentSong() != null && control.status() != PlayController.STATUS_PLAYING) {
                    control.resume();
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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

    // 没有数据的处理行为在各个 Controller 中完成
    @Override
    public void noData() {
    }

    private void initBroadcastReceivers() {
        mySheetDataUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mySheetsController.update(null, null);
            }
        };

        appQuitTimeCountdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(
                        BroadcastManager.Countdown.APP_QUIT_TIME_COUNTDOWN_STATUS,
                        BroadcastManager.Countdown.STOP_COUNTDOWN);
                if (status == BroadcastManager.Countdown.STOP_COUNTDOWN) {
                    leftNavigationController.stopQuitCountdown(true);
                } else {
                    leftNavigationController.startQuitCountdown();
                }
            }
        };

        appThemeChangeAutomaticReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int va = intent.getIntExtra(BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_TOKEN, BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_WHITE);
                ThemeEnum theme = va == BroadcastManager.APP_THEME_CHANGE_AUTOMATIC_WHITE ?
                        ThemeEnum.WHITE : ThemeEnum.DARK;
                appPreference.updateTheme(theme);
                switchThemeMode(theme);
            }
        };

        headsetPlugReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (justOpenTheApplication) {
                    justOpenTheApplication = false;
                    return;
                }
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) { // 耳机拔出
                        try {
                            if (control.status() == PlayController.STATUS_PLAYING) {
                                control.pause();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else if (intent.getIntExtra("state", 0) == 1) { // 耳机插入

                    }
                }
            }
        };

        mainSheetUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bottomNavigationController.updateNotifyFavorite();
                mainSheetsController.update(null, null);
            }
        };

        broadcastManager.registerBroadReceiver(this, mainSheetUpdateReceiver, BroadcastManager.FILTER_MAIN_SHEET_UPDATE);
        broadcastManager.registerBroadReceiver(this, headsetPlugReceiver, BroadcastManager.FILTER_HEADSET_PLUG);
        broadcastManager.registerBroadReceiver(this, appQuitTimeCountdownReceiver, BroadcastManager.FILTER_APP_QUIT_TIME_COUNTDOWN);
        broadcastManager.registerBroadReceiver(this, appThemeChangeAutomaticReceiver, BroadcastManager.FILTER_APP_THEME_CHANGE_AUTOMATIC);
        broadcastManager.registerBroadReceiver(this, mySheetDataUpdateReceiver, BroadcastManager.FILTER_MY_SHEET_UPDATE);

        if (settingPreference.preHeadphoneWire()) {
            headphoneWireControlReceiver = new ComponentName(getPackageName(), HeadphoneWireControlReceiver.class.getName());
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).registerMediaButtonEventReceiver(headphoneWireControlReceiver);
        } else {
            headphoneWireControlReceiver = null;
        }

    }

    /**
     * 关闭服务并退出应用
     */
    public void shutDownServiceAndApp() {
        // 关闭通知栏通知
        bottomNavigationController.hidePlayNotify();

        // 关闭 PlayActivity ，如果启动了的话，PlayActivity 也绑定了播放服务，需要解绑
        Activity activity = ActivityManager.getInstance().getActivity(PlayActivity.class.getName());
        if (activity != null) {
            activity.finish();
        }

        // 关闭自己 解绑 停止服务
        finish();

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

        // 停止服务
        broadcastManager.sendBroadcast(this, BroadcastManager.FILTER_PLAY_SERVICE_QUIT, null);

        unregisterReceiver();
        bottomNavigationController.unregisterReceiver();

        auxiliaryPreference.setTimeSleepDisable();

        AutoSwitchThemeController instance = AutoSwitchThemeController.getInstance(this);
        if (settingPreference.autoSwitchNightTheme() && instance.isSet()) {
            instance.cancelAlarm();
        }

        // PlayService 释放 MediaPlayer 时有错误，服务端始终没有彻底关闭，见服务端注释
        Process.killProcess(Process.myPid());
    }

    private void unregisterReceiver() {
        if (mySheetDataUpdateReceiver != null) {
            broadcastManager.unregisterReceiver(this, mySheetDataUpdateReceiver);
        }

        if (appQuitTimeCountdownReceiver != null) {
            broadcastManager.unregisterReceiver(this, appQuitTimeCountdownReceiver);
        }

        if (appThemeChangeAutomaticReceiver != null) {
            broadcastManager.unregisterReceiver(this, appThemeChangeAutomaticReceiver);
        }

        if (headsetPlugReceiver != null) {
            broadcastManager.unregisterReceiver(this, headsetPlugReceiver);
        }

        if (mainSheetUpdateReceiver != null) {
            broadcastManager.unregisterReceiver(this, mainSheetUpdateReceiver);
        }

        if (headphoneWireControlReceiver != null) {
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).unregisterMediaButtonEventReceiver(headphoneWireControlReceiver);
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
                ActivityManager.getInstance().startSearchActivity(this, sheetID);
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
        return sServiceConnection == null ? null : sServiceConnection.takeControl();
    }

}
