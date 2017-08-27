package com.duan.musicoco.app.manager;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.service.PlayController;

/**
 * Created by DuanJiaNing on 2017/8/22.
 */

public class PlayNotifyManager implements
        ViewVisibilityChangeable {

    private static final String PLAY_NOTIFY = "play_notify";
    private static final String PLAY_NOTIFY_CODE = "play_notify_code";

    private static final int PLAY_STATUS_SWITCH = 0;
    private static final int PLAY_NEXT = 1;
    private static final int PLAY_PREVIOUS = 2;
    private static final int PLAY_FAVORITE_STATUS_SWITCH = 3;
    private static final int PLAY_NOTIFY_CLOSE = 4;

    private static final int PLAY_NOTIFY_ID = 0x1213;
    private final Activity activity;
    private final NotificationManagerCompat manager;
    private final IPlayControl control;
    private final DBMusicocoController dbController;
    private final PlayNotifyReceiver playNotifyReceiver;

    private SongInfo currentSong;
    private boolean play = false;
    private boolean favorite;

    public PlayNotifyManager(Activity activity, IPlayControl control, DBMusicocoController dbController) {
        this.activity = activity;
        this.control = control;
        this.dbController = dbController;
        this.manager = NotificationManagerCompat.from(activity);
        this.playNotifyReceiver = new PlayNotifyReceiver();
    }


    private Notification buildNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);

        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(startMainActivity)
                .setTicker(activity.getString(R.string.app_name_us))
                .setSmallIcon(R.drawable.logo_small_icon)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setCustomContentView(createContentView())
                .setCustomBigContentView(createContentBigView())
                .setPriority(Notification.PRIORITY_HIGH);

        return builder.build();
    }

    private RemoteViews createContentBigView() {
        final RemoteViews view = new RemoteViews(activity.getPackageName(), R.layout.play_notify_big_view);
        setCommonView(view);
        setCommonClickPending(view);

        view.setImageViewResource(R.id.play_notify_favorite,
                favorite ? R.drawable.ic_favorite :
                        R.drawable.ic_favorite_border);

        Intent pre = new Intent(PLAY_NOTIFY);
        pre.putExtra(PLAY_NOTIFY_CODE, PLAY_PREVIOUS);
        PendingIntent p3 = PendingIntent.getBroadcast(activity, PLAY_PREVIOUS, pre, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_pre, p3);

        Intent favorite = new Intent(PLAY_NOTIFY);
        favorite.putExtra(PLAY_NOTIFY_CODE, PLAY_FAVORITE_STATUS_SWITCH);
        PendingIntent p4 = PendingIntent.getBroadcast(activity, PLAY_FAVORITE_STATUS_SWITCH, favorite, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_favorite, p4);

        return view;
    }

    private RemoteViews createContentView() {
        final RemoteViews view = new RemoteViews(activity.getPackageName(), R.layout.play_notify_view);
        setCommonView(view);
        setCommonClickPending(view);
        return view;
    }

    // 图片，歌名，艺术家，播放按钮，下一曲按钮，关闭按钮
    private void setCommonView(RemoteViews view) {

        final String name = currentSong.getTitle();
        final String arts = currentSong.getArtist();
        final Bitmap cover = createCover(currentSong.getAlbum_path());

        view.setImageViewBitmap(R.id.play_notify_cover, cover);

        view.setTextViewText(R.id.play_notify_name, name);
        view.setTextViewText(R.id.play_notify_arts, arts + " - " + name);

        view.setImageViewResource(R.id.play_notify_play,
                play ? R.drawable.ic_pause
                        : R.drawable.ic_play_arrow);

    }

    // 播放或暂停，下一曲，关闭
    private void setCommonClickPending(RemoteViews view) {
        Intent playOrPause = new Intent(PLAY_NOTIFY);
        playOrPause.putExtra(PLAY_NOTIFY_CODE, PLAY_STATUS_SWITCH);
        PendingIntent p1 = PendingIntent.getBroadcast(activity, PLAY_STATUS_SWITCH, playOrPause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_play, p1);

        Intent next = new Intent(PLAY_NOTIFY);
        next.putExtra(PLAY_NOTIFY_CODE, PLAY_NEXT);
        PendingIntent p2 = PendingIntent.getBroadcast(activity, PLAY_NEXT, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_next, p2);

        Intent close = new Intent(PLAY_NOTIFY);
        close.putExtra(PLAY_NOTIFY_CODE, PLAY_NOTIFY_CLOSE);
        PendingIntent p3 = PendingIntent.getBroadcast(activity, PLAY_NOTIFY_CLOSE, close, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_close, p3);
    }

    @NonNull
    private Bitmap createCover(String path) {
        Bitmap b = BitmapFactory.decodeFile(path);
        if (b == null) {
            b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_song);
        }
        return b;
    }

    public void updateSong(SongInfo info) {
        if (info == null) {
            return;
        }

        this.currentSong = info;
        DBSongInfo dbi = dbController.getSongInfo(info.getData());
        favorite = dbi.favorite;

        try {
            play = control.status() == PlayController.STATUS_PLAYING;
        } catch (RemoteException e) {
            e.printStackTrace();
            play = false;
        }

        show();
    }

    public void updateFavorite() {
        if (currentSong == null) {
            return;
        }

        DBSongInfo dbi = dbController.getSongInfo(currentSong.getData());
        if (dbi == null) {
            return;
        }

        favorite = dbi.favorite;

        try {
            play = control.status() == PlayController.STATUS_PLAYING;
        } catch (RemoteException e) {
            e.printStackTrace();
            play = false;
        }

        show();
    }

    @Override
    public void show() {
        if (currentSong == null) {
            return;
        }

        Notification nf = buildNotification();
        manager.notify(PLAY_NOTIFY_ID, nf);
    }

    @Override
    public void hide() {
        manager.cancelAll();
    }

    @Override
    public boolean visible() {
        return manager.areNotificationsEnabled();
    }

    public void initBroadcastReceivers() {
        BroadcastManager bd = BroadcastManager.getInstance();
        bd.registerBroadReceiver(activity, playNotifyReceiver, PLAY_NOTIFY);
    }

    public void unregisterReceiver() {
        BroadcastManager.getInstance().unregisterReceiver(activity, playNotifyReceiver);
    }

    private class PlayNotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context activity, Intent intent) {
            int code = intent.getIntExtra(PLAY_NOTIFY_CODE, -1);
            if (code == -1) {
                return;
            }

            switch (code) {
                case PLAY_STATUS_SWITCH:
                    handlePlayStatusSwitch();
                    break;
                case PLAY_NEXT:
                    try {
                        control.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PLAY_PREVIOUS:
                    try {
                        control.pre();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PLAY_FAVORITE_STATUS_SWITCH:
                    dbController.updateSongFavorite(new Song(currentSong.getData()), !favorite);
                    BroadcastManager.getInstance().sendBroadcast(activity, BroadcastManager.FILTER_MAIN_SHEET_UPDATE, null);
                    break;
                case PLAY_NOTIFY_CLOSE:
                    try {
                        if (control.status() == PlayController.STATUS_PLAYING) {
                            control.pause();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    hide();
                    break;
                default:
                    break;
            }
        }

        private void handlePlayStatusSwitch() {
            try {
                if (control.status() == PlayController.STATUS_PLAYING) {
                    control.pause();
                } else {
                    control.resume();
                }
                play = !play;

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

}