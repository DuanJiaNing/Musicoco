package com.duan.musicoco.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.On2CompleteListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by DuanJiaNing on 2017/6/9.
 */

public class FileUtils {

    public static File getDiskCacheDirFile(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        boolean d = false;
        if (file.exists() && file.isFile()) {
            d = file.delete();
        }

        return d;
    }

    public static void saveImage(final Context context, final String imagePath, @Nullable final On2CompleteListener<Boolean, String> completeListener) {

        if (!StringUtils.isReal(imagePath)) {
            if (completeListener != null) {
                completeListener.onComplete(false, null);
            }
            return;
        }

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String path = String.valueOf(file.getAbsoluteFile()) +
                File.separator +
                context.getString(R.string.app_name_us) +
                File.separator +
                context.getString(R.string.album_save_path);

        File imageF = new File(path);
        if (!imageF.exists()) {
            imageF.mkdirs();
        }
        File im = new File(imagePath);
        final String to = imageF.getAbsolutePath() + File.separator + im.getName() + ".jpeg";

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                return copy(imagePath, to);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    if (completeListener != null) {
                        completeListener.onComplete(true, to);
                    }
                } else {
                    if (completeListener != null) {
                        completeListener.onComplete(false, null);
                    }
                }
            }
        }.execute(imagePath);

    }

    public static boolean copy(String from, String to) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            File file = new File(from);

            File toF = new File(to);
            if (!toF.exists()) {
                toF.createNewFile();
            }
            toF.setReadable(true);
            toF.setWritable(true);

            if (file.exists()) {
                is = new FileInputStream(from);
                os = new FileOutputStream(toF);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
