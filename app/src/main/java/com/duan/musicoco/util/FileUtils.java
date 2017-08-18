package com.duan.musicoco.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duan.musicoco.R;
import com.duan.musicoco.app.App;
import com.duan.musicoco.app.interfaces.OnCompleteListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by DuanJiaNing on 2017/6/9.
 */

public class FileUtils {

    public static File getDiskCacheDirFile(String uniqueName) {
        Context context = App.getContext();
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

        if (d) {
            Log.d("musicoco", "success deleteFile: " + path);
        } else {
            Log.d("musicoco", "fial deleteFile: " + path);
        }

        return d;
    }

    public static void saveImage(final Context context, final String imagePath, @Nullable final OnCompleteListener<Boolean> completeListener) {

        if (!StringUtils.isReal(imagePath)) {
            if (completeListener != null) {
                completeListener.onComplete(false);
            }
            return;
        }

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                StringBuilder builder = new StringBuilder();
                builder.append(file.getAbsoluteFile())
                        .append(File.separator)
                        .append(context.getString(R.string.app_name_us))
                        .append(File.separator)
                        .append(context.getString(R.string.album_save_path));
                File imageF = new File(builder.toString());
                if (!imageF.exists()) {
                    imageF.mkdirs();
                }
                File im = new File(imagePath);
                String to = imageF.getAbsolutePath() + File.separator + im.getName() + ".jpeg";
                return copy(imagePath, to);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    if (completeListener != null) {
                        completeListener.onComplete(true);
                    }
                } else {
                    if (completeListener != null) {
                        completeListener.onComplete(false);
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
