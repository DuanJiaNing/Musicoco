package com.duan.musicoco.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.play.PlayServiceConnection;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.shared.SheetsOperation;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.SongUtils;

import java.util.List;

public class MainActivity extends RootActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnServiceConnect,
        OnThemeChange,
        SwipeRefreshLayout.OnRefreshListener {

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private Menu menu;

    private static PlayServiceConnection sServiceConnection;
    private SwipeRefreshLayout refreshLayout;

    private BottomNavigationController bottomNavigationController;
    private RecentMostPlayController mostPlayController;
    private MainSheetsController mainSheetsController;
    private MySheetsController mySheetsController;

    private BroadcastReceiver mySheetDataChangedReceiver;
    private BroadcastReceiver mainSheetDataChangedReceiver;
    private BroadcastManager broadcastManager;

    private OnUpdateStatusChanged statusChanged = new OnUpdateStatusChanged() {
        @Override
        public void onCompleted() {
            refreshLayout.setRefreshing(false);
        }

        @Override
        public void onStart() {
            refreshLayout.setRefreshing(true);
        }

        @Override
        public void onError(Throwable e) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        broadcastManager = BroadcastManager.getInstance(this);
        bottomNavigationController = new BottomNavigationController(this, mediaManager, appPreference, playPreference);
        mostPlayController = new RecentMostPlayController(this, mediaManager);
        mainSheetsController = new MainSheetsController(this, mediaManager);
        mySheetsController = new MySheetsController(this, dbMusicoco, mediaManager);

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

    }

    private void unregisterReceiver() {

        if (mySheetDataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(mySheetDataChangedReceiver);
        }
        if (mainSheetDataChangedReceiver != null) {
            broadcastManager.unregisterReceiver(mainSheetDataChangedReceiver);
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

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_swipe_refresh);
        refreshLayout.setOnRefreshListener(this);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        updateToolbarColors();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_main_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                    MainSheetHelper helper = new MainSheetHelper(this, dbMusicoco);
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

        bottomNavigationController.initData(sServiceConnection.takeControl(), dbMusicoco);
        mostPlayController.initData(dbMusicoco, getString(R.string.rmp_history));
        mainSheetsController.initData(dbMusicoco);
        mySheetsController.initData(sServiceConnection.takeControl());

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
    public void themeChange(Theme t, int[] colors) {
        Theme theme = appPreference.getTheme();
        bottomNavigationController.themeChange(theme, null);
        mostPlayController.themeChange(theme, null);
        mySheetsController.themeChange(theme, null);
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
    }

    @Override
    public void onRefresh() {
        update();
    }

    private void update() {
        Log.d("update", "MainActivity update");
        bottomNavigationController.update(null, statusChanged);
        mostPlayController.update(getString(R.string.rmp_history), statusChanged);
        mainSheetsController.update(null, statusChanged);
        mySheetsController.update(null, statusChanged);
    }

    public static IPlayControl getControl() {
        return sServiceConnection.takeControl();
    }
}
