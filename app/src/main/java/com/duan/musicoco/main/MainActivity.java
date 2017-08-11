package com.duan.musicoco.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.InspectActivity;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.play.PlayServiceConnection;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.SheetsOperation;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.SongUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

import java.util.List;

public class MainActivity extends InspectActivity implements
        OnServiceConnect,
        ThemeChangeable {

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private Menu menu;

    // FIXME 内存泄漏
    private static PlayServiceConnection sServiceConnection;

    private BottomNavigationController bottomNavigationController;
    private LeftNavigationController leftNavigationController;
    private RecentMostPlayController mostPlayController;
    private MainSheetsController mainSheetsController;
    private MySheetsController mySheetsController;

    private BroadcastReceiver mySheetDataChangedReceiver;
    private BroadcastReceiver mainSheetDataChangedReceiver;
    private BroadcastReceiver dataChangedReceiver;
    private BroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.transitionStatusBar(this);

        broadcastManager = BroadcastManager.getInstance(this);
        bottomNavigationController = new BottomNavigationController(this, mediaManager, appPreference, playPreference);
        leftNavigationController = new LeftNavigationController(this, appPreference);
        mostPlayController = new RecentMostPlayController(this, mediaManager);
        mainSheetsController = new MainSheetsController(this, mediaManager);
        mySheetsController = new MySheetsController(this, dbController, mediaManager);

        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationController.hasInitData()) {
            bottomNavigationController.update(null, null);
        }

        if (mySheetsController.hasInitData()) {
            mySheetsController.update(null, null);
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

    private void unregisterReceiver() {

        if (mySheetDataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(mySheetDataChangedReceiver);
        }
        if (mainSheetDataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(mainSheetDataChangedReceiver);
        }
        if (dataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(dataChangedReceiver);
        }
    }

    private void unbindService() {
        if (sServiceConnection != null && sServiceConnection.hasConnected) {
            sServiceConnection.unregisterListener();
            unbindService(sServiceConnection);
        }
    }

    @Override
    protected void initViews() {
        bottomNavigationController.initView();
        mostPlayController.initView();
        mainSheetsController.initView();
        mySheetsController.initView();
        leftNavigationController.initViews();

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

    @Override
    public void onBackPressed() {
        if (leftNavigationController.onBackPressed()) {
            moveTaskToBack(true);
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
    public void permissionGranted(int requestCode) {
        super.permissionGranted(requestCode);

        sServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
        playServiceManager.bindService(sServiceConnection);

    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    public void onConnected(ComponentName name, IBinder service) {

        initSelfData();
        initBroadcastReceivers();

    }

    private void initBroadcastReceivers() {
        mySheetDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: mySheetDataChangedReceiver");
                mySheetsController.update(null, null);
                isNeedDeletePlayList(intent);
            }
        };

        mainSheetDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: mainSheetDataChangedReceiver");
                mainSheetsController.update(null, null);
            }
        };

        dataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
            }
        };

        broadcastManager.registerBroadReceiver(dataChangedReceiver, BroadcastManager.FILTER_MAIN_DATA_UPDATE);
        broadcastManager.registerBroadReceiver(mySheetDataChangedReceiver, BroadcastManager.FILTER_MY_SHEET_CHANGED);
        broadcastManager.registerBroadReceiver(mainSheetDataChangedReceiver, BroadcastManager.FILTER_MAIN_SHEET_CHANGED);
    }

    private void isNeedDeletePlayList(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int sheetID = extras.getInt(SheetsOperation.DELETE_SHEET_ID, Integer.MAX_VALUE);
            if (sheetID == Integer.MAX_VALUE) {
                return;
            }

            try {
                IPlayControl control = sServiceConnection.takeControl();
                int cursid = control.getPlayListId();
                if (sheetID == cursid) {

                    //当前播放歌单属于被删除歌单时需将播放列表置为【全部歌单】
                    MainSheetHelper helper = new MainSheetHelper(this, dbController);
                    List<DBSongInfo> list = helper.getAllSongInfo();
                    List<Song> songs = SongUtils.DBSongInfoListToSongList(list);
                    control.setPlayList(songs, 0, MainSheetHelper.SHEET_ALL);
                    control.pause();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    private void initSelfData() {

        bottomNavigationController.initData(sServiceConnection.takeControl(), dbController);
        mostPlayController.initData(dbController, getString(R.string.rmp_history));
        mainSheetsController.initData(dbController);
        mySheetsController.initData(sServiceConnection.takeControl());
        leftNavigationController.initData(dbController);

        update();
        themeChange(null, null);

    }

    @Override
    public void disConnected(ComponentName name) {
        sServiceConnection = null;
        sServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
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

        MenuItem search = menu.findItem(R.id.action_main_search);
        Drawable icon = search.getIcon();
        if (icon != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon.setTint(mainTC);
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

    private void update() {
        Log.d("updateCurrentPlay", "MainActivity updateCurrentPlay");
        bottomNavigationController.update(null, null);
        mostPlayController.update(getString(R.string.rmp_history), null);
        mainSheetsController.update(null, null);
        mySheetsController.update(null, null);
    }

    public static IPlayControl getControl() {
        return sServiceConnection.takeControl();
    }
}
