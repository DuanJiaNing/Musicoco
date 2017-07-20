package com.duan.musicoco.app.interfaces;

import android.support.annotation.Nullable;

import rx.Subscriber;

/**
 * Created by DuanJiaNing on 2017/7/15.
 */

public abstract class SubscriberAbstract<T> extends Subscriber<T> {

    private OnUpdateStatusChanged statusChanged = null;

    public SubscriberAbstract(@Nullable OnUpdateStatusChanged statusChanged) {
        this.statusChanged = statusChanged;
    }

    @Override
    public void onStart() {
        if (statusChanged != null) {
            statusChanged.onStart();
        }
    }

    @Override
    public void onCompleted() {
        if (statusChanged != null) {
            statusChanged.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (statusChanged != null) {
            statusChanged.onError(e);
        }
    }

    @Override
    public abstract void onNext(T t);
}
