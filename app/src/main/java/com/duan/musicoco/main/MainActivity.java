package com.duan.musicoco.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;

// 全部音乐
// 最近播放
// 收藏

//歌单....

public class MainActivity extends RootActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewContract {

    private IBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(null, null);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
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
    public void setPresenter(PresenterContract presenter) {

    }

    @Override
    public void initViews(@Nullable View view, @Nullable Object obj) {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void songChanged(Song song, int index) {

    }

    @Override
    public void startPlay(Song song, int index, int status) {

    }

    @Override
    public void stopPlay(Song song, int index, int status) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void disConnected() {

    }

    @Override
    public void permissionDenied(int requestCode) {

    }

    @Override
    public void showPlayList() {

    }

    @Override
    public void hidePlayList() {

    }
}
