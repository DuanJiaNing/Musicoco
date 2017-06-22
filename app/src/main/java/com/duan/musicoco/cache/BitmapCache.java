package com.duan.musicoco.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.duan.musicoco.R;
import com.duan.musicoco.image.PictureBuilder;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by DuanJiaNing on 2017/6/9.
 * 位图磁盘缓存
 * 参考博文：<a href="http://blog.csdn.net/guolin_blog/article/details/28863651">Android DiskLruCache完全解析，硬盘缓存的最佳方案</a>
 */

public class BitmapCache {

    private DiskLruCache mDiskLruCache = null;

    private Context mContext;

    private static volatile BitmapCache BITMAPCACHE;
    private final static String CACHE = "bitmap";

    public final static String DEFAULT_PIC_KEY = "default_pic_key";

    private BitmapCache(Context context) {
        this.mContext = context;
        initDiskCacheControl(context);
    }

    public static BitmapCache getInstance(Context context) {
        if (BITMAPCACHE == null) {
            synchronized (BitmapCache.class) {
                if (BITMAPCACHE == null)
                    BITMAPCACHE = new BitmapCache(context);
            }
        }
        return BITMAPCACHE;
    }

    private void initDiskCacheControl(Context context) {
        try {
            File cacheDir = FileUtils.getDiskCacheDirFile(context, CACHE);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DiskLruCache getCacheControl() {
        return mDiskLruCache;
    }

    /**
     * 从缓存中移除某张图片
     */
    public void remove(String key) {
        try {
            mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存中获得图片
     *
     * @param key key，应使用{@link com.duan.musicoco.util.StringUtil#stringToMd5(String)}方法转换为 MD5
     * @return 位图，获取失败返回 null
     */
    @Nullable
    public Bitmap get(String key) {
        Bitmap result = null;
        InputStream is = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                is = snapshot.getInputStream(0);
                result = BitmapFactory.decodeStream(is);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加图片到缓存中
     *
     * @param key    key，应使用{@link com.duan.musicoco.util.StringUtil#stringToMd5(String)}方法转换为 MD5
     * @param bitmap 位图
     * @return 添加成功返回 true
     */
    public boolean add(String key, Bitmap bitmap) {
        if (bitmap == null)
            return false;

        OutputStream os = null;
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskLruCache.edit(key);
            os = editor.newOutputStream(0);
            bitmap.compress(Bitmap.CompressFormat.PNG, 30, os);
            editor.commit();

            flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                editor.abort();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将内存中的操作记录同步到日志文件（也就是journal文件）
     * 并不是每次写入缓存都要调用一次flush()方法
     * 在Activity的onPause()方法中去调用flush()方法就可以了。
     */
    public void flush() {
        try {
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //初始化默认的专辑图片，第一次启动应用时完成
    public void initDefaultBitmap() {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int r = metrics.widthPixels * 2 / 3;

        PictureBuilder builder = new PictureBuilder(mContext);
        builder.resizeForDefault(r, r, R.mipmap.default_album);
        builder.toRoundBitmap();
        builder.addOuterCircle(0, 10, Color.parseColor("#df3b43"))
                .addOuterCircle(7, 1, Color.WHITE);
        add(StringUtil.stringToMd5(DEFAULT_PIC_KEY), builder.getBitmap());
    }

    public Bitmap getDefaultBitmap() {
        Bitmap b = get(StringUtil.stringToMd5(DEFAULT_PIC_KEY));
        if (b == null)
            initDefaultBitmap();
        return get(StringUtil.stringToMd5(DEFAULT_PIC_KEY));
    }
}
