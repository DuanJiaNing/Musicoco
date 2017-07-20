package com.duan.musicoco.shared;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by DuanJiaNing on 2017/7/4.
 */

public class ExceptionHandler {

    public interface Handle {
        void handle();
    }

    public void handleRemoteException(Context context, @Nullable String msg, @Nullable Handle handle) {
        if (msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

        if (handle != null) {
            handle.handle();
        }
    }

}
