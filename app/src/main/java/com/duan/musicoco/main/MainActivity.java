package com.duan.musicoco.main;

import android.content.ComponentName;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.duan.musicoco.R;
import com.duan.musicoco.app.PlayServiceManager;
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
        bottomNavigationController = new BottomNavigationController(this, mediaManager, appPreference);
        mostPlayController = new RecentMostPlayController(this, mediaManager);
        mainSheetsController = new MainSheetsController(this, mediaManager);
        mySheetsController = new MySheetsController(this, dbMusicoco);

        //FIXME test
        appPreference.modifyTheme(Theme.WHITE);
        Theme theme = appPreference.getTheme();
        if (theme == Theme.DARK) {
            this.setTheme(R.style.Theme_DARK);
        } else if (theme == Theme.WHITE) {
            this.setTheme(R.style.Theme_WHITE);
        }

        setContentView(R.layout.activity_main);

        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt(BottomNavigationController.CURRENT_POSITION, bottomNavigationController.getCurrentPosition());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int listPos = savedInstanceState.getInt(BottomNavigationController.CURRENT_POSITION);
        bottomNavigationController.setCurrentPosition(listPos);
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

    }

    private void initSelfData() {

        bottomNavigationController.initData(mServiceConnection.takeControl());
        mostPlayController.initData(dbMusicoco, "历史最多播放");
        mainSheetsController.initData(dbMusicoco);
        mySheetsController.initData();

        update();
        bottomNavigationController.update(null, null);

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
        mostPlayController.update("历史最多播放", null);
        mainSheetsController.update(null, statusChanged);
        mySheetsController.update(null, statusChanged);
    }

}
