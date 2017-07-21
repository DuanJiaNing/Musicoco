package com.duan.musicoco.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
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
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.OnServiceConnect;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.play.PlayServiceConnection;
import com.duan.musicoco.preference.Theme;

public class MainActivity extends RootActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnServiceConnect,
        OnThemeChange,
        SwipeRefreshLayout.OnRefreshListener {

    private PlayServiceConnection mServiceConnection;
    private SwipeRefreshLayout refreshLayout;

    private BottomNavigationController bottomNavigationController;
    private RecentMostPlayController mostPlayController;
    private MainSheetsController mainSheetsController;
    private MySheetsController mySheetsController;

    private BroadcastReceiver mySheetDataChangedReceiver;
    private BroadcastReceiver mainSheetDataChangedReceiver;

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
        bottomNavigationController = new BottomNavigationController(this, mediaManager, appPreference, playPreference);
        mostPlayController = new RecentMostPlayController(this, mediaManager);
        mainSheetsController = new MainSheetsController(this, mediaManager);
        mySheetsController = new MySheetsController(this, dbMusicoco, mediaManager);

        //FIXME test
        appPreference.modifyTheme(Theme.WHITE);

        Theme theme = appPreference.getTheme();
        if (theme == Theme.DARK) {
            this.setTheme(R.style.Theme_DARK);
        } else if (theme == Theme.WHITE) {
            this.setTheme(R.style.Theme_WHITE);
        }

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
            BroadcastManager.unregisterReceiver(this, mySheetDataChangedReceiver);
        }
        if (mainSheetDataChangedReceiver != null) {
            BroadcastManager.unregisterReceiver(this, mainSheetDataChangedReceiver);
        }
    }

    private void unbindService() {
        if (mServiceConnection != null && mServiceConnection.hasConnected) {
            mServiceConnection.unregisterListener();
            unbindService(mServiceConnection);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
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

        mServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);

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
            }
        };
        mainSheetDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: mainSheetDataChangedReceiver");
                mainSheetsController.update(null, null);
            }
        };

        BroadcastManager.registerBroadReceiver(this, mySheetDataChangedReceiver, BroadcastManager.FILTER_MY_SHEET_CHANGED);
        BroadcastManager.registerBroadReceiver(this, mainSheetDataChangedReceiver, BroadcastManager.FILTER_MAIN_SHEET_CHANGED);
    }

    private void initSelfData() {

        bottomNavigationController.initData(mServiceConnection.takeControl(), dbMusicoco);
        mostPlayController.initData(dbMusicoco, "历史最多播放");
        mainSheetsController.initData(dbMusicoco);
        mySheetsController.initData(mServiceConnection.takeControl());

        update();

        Theme theme = appPreference.getTheme();
        themeChange(theme, null);

    }


    @Override
    public void disConnected(ComponentName name) {
        mServiceConnection = null;
        mServiceConnection = new PlayServiceConnection(bottomNavigationController, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        bottomNavigationController.themeChange(theme, null);
        mostPlayController.themeChange(theme, null);
        mySheetsController.themeChange(theme, null);
    }

    @Override
    public void onRefresh() {
        update();
    }

    private void update() {
        bottomNavigationController.update(null, statusChanged);
        mostPlayController.update("历史最多播放", statusChanged);
        mainSheetsController.update(null, statusChanged);
        mySheetsController.update(null, statusChanged);
    }

}
