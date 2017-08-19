package com.duan.musicoco.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * 耳机线控，仅在{@link android.os.Build.VERSION_CODES#KITKAT}以下有效，
 * 5.0以上被{@link android.support.v4.media.session.MediaSessionCompat}接管。
 * Created by hzwangchenyan on 2016/1/21.
 */
public class HeadphoneWireController extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                // 暂停
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // 下一曲
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                // 上一曲
                break;
        }
    }
}
