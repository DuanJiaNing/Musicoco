package com.duan.musicoco.app.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.duan.musicoco.service.PlayService;

/**
 * Created by DuanJiaNing on 2017/5/25.
 * 不能设置成单例，负责不同的 Activity 进行绑定调用时，始终是同一个 Activity 在进行绑定
 */

public class PlayServiceManager {

    private Context context;

    public PlayServiceManager(Context context) {
        this.context = context;
    }

    //启动服务，需要关闭记得一定要使用 stopService 关闭，即使没有组件绑定到服务服务也会一直运行，因为此时他是以启动的方式启动的，而不是绑定。
    public static void startPlayService(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        context.startService(intent);
    }

    //绑定服务
    public void bindService(ServiceConnection connection) {
        Intent intent = new Intent(context, PlayService.class);
        context.bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

}
