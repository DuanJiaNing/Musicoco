package com.duan.musicoco.app.interfaces;

import android.content.ComponentName;
import android.os.IBinder;

/**
 * Created by DuanJiaNing on 2017/6/30.
 */

public interface OnServiceConnect {

    void onConnected(ComponentName name, IBinder service);

    void disConnected(ComponentName name);
}
