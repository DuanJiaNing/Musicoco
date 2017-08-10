package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import com.bumptech.glide.load.engine.Resource;
import com.duan.musicoco.R;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.image.BitmapProducer;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.SongUtils;
import com.duan.musicoco.util.Utils;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by DuanJiaNing on 2017/8/10.
 */

public class LeftNavigationController implements
        ViewVisibilityChangeable,
        NavigationView.OnNavigationItemSelectedListener {

    private final Activity activity;
    protected DBMusicocoController dbController;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public LeftNavigationController(Activity activity) {
        this.activity = activity;
    }

    public void initViews() {
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void initData(DBMusicocoController dbController) {
        this.dbController = dbController;
        initBackground();

    }

    private void initBackground() {
        // FIXME 异步执行
        BitmapProducer producer = new BitmapProducer(activity);
        String[] res = getImagePath();
//        DisplayMetrics metrics = Utils.getMetrics(activity);
        int w = navigationView.getWidth();
        int h = navigationView.getHeight();

        final Bitmap kaleidoscope = producer.getKaleidoscope(res, w, h, R.drawable.default_album);
        final BlurTransformation btf = new BlurTransformation(activity, 1, 5);
        Resource<Bitmap> resource = btf.transform(new Resource<Bitmap>() {
            @Override
            public Bitmap get() {
                return kaleidoscope;
            }

            @Override
            public int getSize() {
                Bitmap b = get();
                int size;
                if (b != null) {
                    size = b.getRowBytes() * b.getHeight() / 1024;
                } else {
                    size = 0;
                }
                return size;
            }

            @Override
            public void recycle() {
                Bitmap b = get();
                if (b != null) {
                    if (!b.isRecycled()) {
                        b.recycle();
                        b = null;
                    }
                }
            }
        }, w, w);

        BitmapDrawable bd = new BitmapDrawable(resource.get());
        navigationView.setBackground(bd);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorDrawable cd = new ColorDrawable(Color.BLACK);
            cd.setAlpha(100);
            navigationView.setForeground(cd);
        }
    }

    public boolean onBackPressed() {
        if (visible()) {
            hide();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void show() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void hide() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean visible() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private String[] getImagePath() {
        MainSheetHelper h = new MainSheetHelper(activity, dbController);
        List<DBSongInfo> info = h.getAllSongInfo();
        List<SongInfo> list = SongUtils.DBSongInfoToSongInfoList(info, MediaManager.getInstance(activity));

        int count = activity.getResources().getInteger(R.integer.main_nav_image_count);
        count = list.size() > count ? count : list.size();

        String[] strs = new String[count];
        for (int i = 0; i < count; i++) {
            strs[i] = list.get(i).getAlbum_path();
        }
        return strs;
    }
}
