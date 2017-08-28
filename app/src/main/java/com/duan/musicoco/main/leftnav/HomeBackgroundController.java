package com.duan.musicoco.main.leftnav;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.Resource;
import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.image.BitmapProducer;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.util.MediaUtils;
import com.duan.musicoco.util.StringUtils;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/8/12.
 */

public class HomeBackgroundController {

    private final Activity activity;
    private final AppPreference appPreference;
    private NavigationView navigationView;
    private DBMusicocoController dbController;

    public HomeBackgroundController(Activity activity, AppPreference appPreference) {
        this.activity = activity;
        this.appPreference = appPreference;
    }

    public void initViews(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    public void initData(DBMusicocoController dbController) {
        this.dbController = dbController;
    }

    // 更新照片墙专用
    public void updateImage() {
        Observable.OnSubscribe<Bitmap> onSubscribe = new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                subscriber.onStart();

                Bitmap bitmap = getImageBitmap();
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
                        setImageBitmap(bitmap);
                    }
                });
    }

    @Nullable
    private String[] getImagePath() {
        int size = appPreference.getImageWallSize();
        if (size == 0) {
            return null;
        }

        MainSheetHelper h = new MainSheetHelper(activity, dbController);
        List<DBSongInfo> info = h.getAllSongInfo();
        List<SongInfo> list = MediaUtils.DBSongInfoToSongInfoList(activity, info, MediaManager.getInstance());
        if (info.size() == 0) {
            return null;
        }


        size = size > list.size() ? list.size() : size;
        String[] strs = new String[size];
        int j = 0;
        for (int i = 0; i < size; i++) {
            String path = list.get(i).getAlbum_path();
            if (StringUtils.isReal(path)) {
                strs[j++] = path;
            }
        }

        String[] des = new String[j];
        System.arraycopy(strs, 0, des, 0, j);
        return des;
    }

    private Bitmap getImageBitmap() {
        BitmapProducer producer = new BitmapProducer(activity);
        String[] res = getImagePath();
        if (res == null) {
            return null;
        }

        ImageView iv = (ImageView) navigationView.findViewById(R.id.main_left_nav_image);
        int w = iv.getWidth();
        int h = iv.getHeight();

        // 虚化程度
        // UPDATE: 2017/8/28 更新 主页左侧导航图片属性定制
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

    private void setImageBitmap(Bitmap bitmap) {
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
            iv.startAnimation(animation);
        }

    }

}
