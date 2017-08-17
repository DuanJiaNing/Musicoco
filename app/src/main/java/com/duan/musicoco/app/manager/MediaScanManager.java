package com.duan.musicoco.app.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

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
