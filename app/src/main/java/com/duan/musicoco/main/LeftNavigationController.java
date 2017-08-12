package com.duan.musicoco.main;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.Resource;
import com.duan.musicoco.R;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.image.BitmapProducer;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.SongUtils;
import com.duan.musicoco.view.discreteseekbar.internal.drawable.TrackOvalDrawable;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/8/10.
 */

public class LeftNavigationController implements
        ViewVisibilityChangeable,
        NavigationView.OnNavigationItemSelectedListener,
        ContentUpdatable,
        ThemeChangeable {

    private final Activity activity;
    private final AppPreference appPreference;
    protected DBMusicocoController dbController;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public LeftNavigationController(Activity activity, AppPreference appPreference) {
        this.activity = activity;
        this.appPreference = appPreference;
    }

    public void initViews() {
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initDaytimeOrNightMode();
    }

    private void initDaytimeOrNightMode() {
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.setting_night_mode);

        // 0 日间时应该显示的
        // 1 夜间时应该显示的
        Drawable[] ds = new Drawable[2];
        String[] ts = new String[2];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ds[0] = activity.getDrawable(R.drawable.ic_night);
        } else {
            ds[0] = activity.getResources().getDrawable(R.drawable.ic_night);
        }
        ts[0] = activity.getString(R.string.setting_night_mode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ds[1] = activity.getDrawable(R.drawable.ic_daytime);
        } else {
            ds[1] = activity.getResources().getDrawable(R.drawable.ic_daytime);
        }
        ts[1] = activity.getString(R.string.setting_daytime_mode);

        Drawable icon;
        String title;
        ThemeEnum theme = appPreference.getTheme();
        if (theme == ThemeEnum.WHITE || theme == ThemeEnum.VARYING) {
            icon = ds[0];
            title = ts[0];
        } else {
            icon = ds[1];
            title = ts[1];
        }

        item.setIcon(icon);
        item.setTitle(title);
    }

    public void initData(DBMusicocoController dbController) {
        this.dbController = dbController;

        // FIXME null
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                ImageView iv = (ImageView) navigationView.findViewById(R.id.main_left_nav_image);
                iv.post(new Runnable() {
                    @Override
                    public void run() {
                        update(null, null);
                    }
                });
            }
        });

    }

    private Bitmap getImageWallBitmap() {
        BitmapProducer producer = new BitmapProducer(activity);
        String[] res = getImagePath();
        if (res == null) {
            return null;
        }

        ImageView iv = (ImageView) navigationView.findViewById(R.id.main_left_nav_image);
        int w = iv.getWidth();
        int h = iv.getHeight();

        int blur = appPreference.getImageWallBlur();
        int defaultSam = activity.getResources().getInteger(R.integer.image_wall_default_sampling);
        int sam = blur == 1 ? 1 : defaultSam;

        final Bitmap kaleidoscope = producer.getKaleidoscope(res, w, h, R.drawable.default_album);
        final BlurTransformation btf = new BlurTransformation(activity, blur, sam);
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

        return resource.get();
    }

    private void setImageWallBitmap(Bitmap bitmap) {
        if (bitmap != null) {

            // menu 中有图标时要通过 getHeaderView 查找子 view
            View headerView = navigationView.getHeaderView(0);
            ImageView iv = (ImageView) headerView.findViewById(R.id.main_left_nav_image);
            iv.setImageBitmap(bitmap);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ColorDrawable cd = new ColorDrawable(Color.BLACK);
                int alpha = appPreference.getImageWallAlpha();
                cd.setAlpha(alpha);
                iv.setForeground(cd);
            }

            Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
            navigationView.startAnimation(animation);
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
        int id = item.getItemId();
        switch (id) {
            case R.id.setting_scan: // 文件扫描

                break;
            case R.id.setting_sleep: // 睡眠定时

                break;
            case R.id.setting_image_wall: // 照片墙

                break;
            case R.id.setting_play_ui: // 播放界面设置

                break;
            case R.id.setting_theme_color_custom: // 主题色

                break;
            case R.id.setting_night_mode: // 夜间模式
                handleModeSwitch(item);
                break;
            case R.id.setting_set: // 设置

                break;
            case R.id.setting_quit: // 退出

                break;
            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void handleModeSwitch(MenuItem item) {
        ThemeEnum theme = appPreference.getTheme();

        Drawable icon;
        String title;

        if (theme == ThemeEnum.WHITE || theme == ThemeEnum.VARYING) { // 切换到 夜间模式
            theme = ThemeEnum.DARK;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = activity.getDrawable(R.drawable.ic_daytime);
            } else {
                icon = activity.getResources().getDrawable(R.drawable.ic_daytime);
            }
            title = activity.getString(R.string.setting_daytime_mode);
        } else { // 切换到 白天模式
            theme = ThemeEnum.WHITE;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = activity.getDrawable(R.drawable.ic_night);
            } else { // 切换到 白天模式
                icon = activity.getResources().getDrawable(R.drawable.ic_night);
            }
            title = activity.getString(R.string.setting_night_mode);
        }

        item.setIcon(icon);
        item.setTitle(title);

        appPreference.updateTheme(theme);
        ((MainActivity) activity).switchThemeMode(theme);

    }

    @Nullable
    private String[] getImagePath() {
        int size = appPreference.getImageWallSize();
        if (size == 0) {
            return null;
        }

        MainSheetHelper h = new MainSheetHelper(activity, dbController);
        List<DBSongInfo> info = h.getAllSongInfo();
        List<SongInfo> list = SongUtils.DBSongInfoToSongInfoList(info, MediaManager.getInstance(activity));
        if (info.size() == 0) {
            return null;
        }

        size = size > list.size() ? list.size() : size;
        String[] strs = new String[size];
        for (int i = 0; i < size; i++) {
            strs[i] = list.get(i).getAlbum_path();
        }
        return strs;
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged statusChanged) {
        Observable.OnSubscribe<Bitmap> onSubscribe = new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                subscriber.onStart();

                Bitmap bitmap = getImageWallBitmap();
                subscriber.onNext(bitmap);

                subscriber.onCompleted();
            }
        };

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        setImageWallBitmap(bitmap);
                    }
                });
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        ThemeEnum th = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(activity, th);
        int mainBC = cs[3];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int accentC = cs[2];

        navigationView.setItemTextColor(ColorStateList.valueOf(mainTC));
        navigationView.setBackgroundColor(mainBC);

        Menu menu = navigationView.getMenu();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(0);
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon.setTint(accentC);
                }
            }
        }
    }
}
