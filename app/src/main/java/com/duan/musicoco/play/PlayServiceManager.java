package com.duan.musicoco.play;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.telecom.Connection;

import com.duan.musicoco.service.PlayService;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class PlayServiceManager {

    private Context mContext;

    private ServiceConnection mConnection;

    private Contract.Presenter mPresenter;

    public PlayServiceManager(Context context, ServiceConnection connection, Contract.Presenter presenter){
        this.mContext = context;
        this.mConnection = connection;
        this.mPresenter = presenter;
    }

    //启动服务，需要关闭记得一定要使用 stopService 关闭，即使没有组件绑定到服务服务也会一直运行，因为此时他是以启动的方式启动的，而不是绑定。
    public static void startPlayService(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        context.startService(intent);
    }

    //绑定服务
    public static void bindService(Context context, ServiceConnection connection){
        Intent intent = new Intent(context, PlayService.class);
        context.bindService(intent,connection, Service.BIND_AUTO_CREATE);
    }



}
