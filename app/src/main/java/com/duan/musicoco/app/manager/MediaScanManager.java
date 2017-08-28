package com.duan.musicoco.app.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by DuanJiaNing on 2017/8/16.
 * 歌曲文件扫描
 */

public class MediaScanManager {

    private Context context;
    private ScanReceiver scanSdReceiver;

    public MediaScanManager(Context context) {
        this.context = context;
        this.scanSdReceiver = new ScanReceiver();
    }

    private MediaScanListener listener;

    public void setOnMediaScanListener(MediaScanListener listener) {
        this.listener = listener;
    }

    public interface MediaScanListener {
        void scanStarted();

        void scanFinished();
    }

    /**
     * 刷新媒体库
     */
    private void updataMedia(String path) {
        //版本号的判断  4.4一下可以发送广播，以上非系统应用不允许发送 ACTION_MEDIA_MOUNTED(系统广播) 广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    context.sendBroadcast(mediaScanIntent);
                }
            });
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(new File(path))));
        }

    }

    public void startScan() {
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addDataScheme("file");
        context.registerReceiver(scanSdReceiver, intentfilter);

        String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
        String path = externalStoragePath + "/";

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + path)));

    }

    private class ScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {

                if (listener != null) {
                    listener.scanStarted();
                }

            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {

                if (listener != null) {
                    listener.scanFinished();
                }
                context.unregisterReceiver(scanSdReceiver);
            }
        }
    }
}
