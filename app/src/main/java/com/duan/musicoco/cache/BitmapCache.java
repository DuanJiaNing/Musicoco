package com.duan.musicoco.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.StringUtils;

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

    private final static String TAG = "BitmapCache";

    public final static String DEFAULT_PIC_KEY = "default_key";

    public static final String CACHE_ALBUM_VISUALIZER_IMAGE = "album_visualizer";

    private final String name;

    public BitmapCache(Context context, String name) {
        this.mContext = context;
        this.name = name;
        initDiskCacheControl(context, name);
    }

    public String getName() {
        return name;
    }

    private void initDiskCacheControl(Context context, String name) {
        try {
            File cacheDir = FileUtils.getDiskCacheDirFile(context, name);
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
     * @param key key，应使用{@link StringUtils#stringToMd5(String)}方法转换为 MD5
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
     * @param key    key，应使用{@link StringUtils#stringToMd5(String)}方法转换为 MD5
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
    public void initDefaultBitmap(Bitmap bitmap) {
        add(StringUtils.stringToMd5(DEFAULT_PIC_KEY), bitmap);
    }

    public Bitmap getDefaultBitmap() throws Exception {
        Bitmap b = get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
        if (b == null) {
            throw new Exception("you need call com.duan.musicoco.cache.BitmapCache#initDefaultBitmap(Bitmap b) first");
        }
        return get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
    }
}
