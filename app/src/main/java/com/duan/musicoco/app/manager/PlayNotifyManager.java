package com.duan.musicoco.app.manager;

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
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.main.MainActivity;

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
    private final Context context;
    private final NotificationManagerCompat manager;
    private final IPlayControl control;
    private final DBMusicocoController dbController;
    private final PlayNotifyReceiver playNotifyReceiver;

    private SongInfo currentSong;
    private boolean play = false;
    private boolean favorite;

    public PlayNotifyManager(Context context, IPlayControl control, DBMusicocoController dbController) {
        this.context = context;
        this.control = control;
        this.dbController = dbController;
        this.manager = NotificationManagerCompat.from(context);
        this.playNotifyReceiver = new PlayNotifyReceiver();
    }


    private Notification buildNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(startMainActivity)
                .setTicker(context.getString(R.string.app_name_us))
                .setSmallIcon(R.drawable.ic_musicoco)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setCustomContentView(createContentView())
                .setCustomBigContentView(createContentBigView())
                .setPriority(Notification.PRIORITY_MAX);

        return builder.build();
    }

    private RemoteViews createContentBigView() {
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.play_notify_big_view);
        setCommonView(view);
        setCommonClickPending(view);

        view.setImageViewResource(R.id.play_notify_favorite,
                favorite ? R.drawable.ic_favorite :
                        R.drawable.ic_favorite_border);

        Intent pre = new Intent(PLAY_NOTIFY);
        pre.putExtra(PLAY_NOTIFY_CODE, PLAY_PREVIOUS);
        PendingIntent p3 = PendingIntent.getBroadcast(context, PLAY_PREVIOUS, pre, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_pre, p3);

        Intent favorite = new Intent(PLAY_NOTIFY);
        favorite.putExtra(PLAY_NOTIFY_CODE, PLAY_FAVORITE_STATUS_SWITCH);
        PendingIntent p4 = PendingIntent.getBroadcast(context, PLAY_FAVORITE_STATUS_SWITCH, favorite, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_favorite, p4);

        return view;
    }

    private RemoteViews createContentView() {
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.play_notify_view);
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
        view.setTextViewText(R.id.play_notify_arts, arts);

        view.setImageViewResource(R.id.play_notify_play,
                play ? R.drawable.ic_pause
                        : R.drawable.ic_play_arrow);

    }

    // 播放或暂停，下一曲，关闭
    private void setCommonClickPending(RemoteViews view) {
        Intent playOrPause = new Intent(PLAY_NOTIFY);
        playOrPause.putExtra(PLAY_NOTIFY_CODE, PLAY_STATUS_SWITCH);
        PendingIntent p1 = PendingIntent.getBroadcast(context, PLAY_STATUS_SWITCH, playOrPause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_play, p1);

        Intent next = new Intent(PLAY_NOTIFY);
        next.putExtra(PLAY_NOTIFY_CODE, PLAY_NEXT);
        PendingIntent p2 = PendingIntent.getBroadcast(context, PLAY_NEXT, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_next, p2);

        Intent close = new Intent(PLAY_NOTIFY);
        close.putExtra(PLAY_NOTIFY_CODE, PLAY_NOTIFY_CLOSE);
        PendingIntent p3 = PendingIntent.getBroadcast(context, PLAY_NOTIFY_CLOSE, close, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.play_notify_close, p3);
    }

    @NonNull
    private Bitmap createCover(String path) {
        Bitmap b = BitmapFactory.decodeFile(path);
        if (b == null) {
            b = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_song);
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
        BroadcastManager bd = BroadcastManager.getInstance(context);
        bd.registerBroadReceiver(playNotifyReceiver, PLAY_NOTIFY);
    }

    public void unregisterReceiver() {
        BroadcastManager.getInstance(context).unregisterReceiver(playNotifyReceiver);
    }

    public void init(boolean b) {
        play = b;
    }

    private class PlayNotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
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
                    updateFavorite();
                    break;
                case PLAY_NOTIFY_CLOSE:
                    hide();
                    break;
                default:
                    break;
            }
        }

        private void handlePlayStatusSwitch() {
            try {
                if (play) {
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