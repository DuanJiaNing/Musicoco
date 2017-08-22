package com.duan.musicoco.app.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.duan.musicoco.service.PlayController;

/**
 * Created by DuanJiaNing on 2017/8/22.
 */

public class PlayNotifyManager implements
        ViewVisibilityChangeable {

    private static final int PLAY_NOTIFY_ID = 0x1;
    private final Context context;
    private final NotificationManagerCompat manager;
    private final IPlayControl control;
    private final DBMusicocoController dbController;
    private final MediaManager mediaManager;

    private SongInfo currentSong;
    private boolean play;
    private boolean favorite;

    public PlayNotifyManager(Context context, IPlayControl control, DBMusicocoController dbController, MediaManager mediaManager) {
        this.context = context;
        this.control = control;
        this.dbController = dbController;
        this.mediaManager = mediaManager;
        this.manager = NotificationManagerCompat.from(context);

    }

    private Notification buildNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(startMainActivity)
                .setTicker(context.getString(R.string.app_name_us))
                .setSmallIcon(R.drawable.ic_action_music_2)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setCustomContentView(createContentView())
                .setCustomBigContentView(createContentBigView());

        return builder.build();
    }

    private RemoteViews createContentBigView() {

        return null;
    }

    private RemoteViews createContentView() {
        final String name = currentSong.getTitle();
        final String arts = currentSong.getArtist();
        final Bitmap cover = createCover(currentSong.getAlbum_path());

        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.play_notify_view);
        view.setImageViewBitmap(R.id.play_notify_cover, cover);

        view.setTextViewText(R.id.play_notigy_name, name);
        view.setTextViewText(R.id.play_notigy_arts, arts);

        view.setImageViewResource(R.id.play_notify_play,
                play ? R.drawable.ic_pause_circle_outline
                        : R.drawable.ic_play_circle_outline);

        view.setImageViewResource(R.id.play_notify_next, R.drawable.ic_skip_next_black_24dp);
        view.setImageViewResource(R.id.play_notify_close, R.drawable.ic_clear_gray_24dp);


        Intent playOrPause = new Intent()

        return view;
    }

    @NonNull
    private Bitmap createCover(String path) {
        return null;
    }

    public void updateSong(Song song) {
        SongInfo info = mediaManager.getSongInfo(song);
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

    public void switchStatus(boolean play) {
        this.play = play;
        show();
    }

    public void switchFavorite(boolean favorite) {
        this.favorite = favorite;
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
        manager.cancel(PLAY_NOTIFY_ID);
    }

    @Override
    public boolean visible() {
        return manager.areNotificationsEnabled();
    }

    private class PlayNotifyReceiver extends BroadcastReceiver {

        static final String PLAY_NOTIFY = "play_notify";
        static final String PLAY_NOTIFY_CODE = "play_notify_code";
        static final int PLAY_STATUS_SWITCH = 0;
        static final int PLAY_NEXT = 1;
        static final int PLAY_PREVIOUS = 2;
        static final int PLAY_FAVORITE_STATUS_SWITCH = 3;


        @Override
        public void onReceive(Context context, Intent intent) {
            int code = intent.getIntExtra(PLAY_NOTIFY_CODE, -1);
            if (code == -1) {
                return;
            }

            switch (code) {
                case PLAY_STATUS_SWITCH:
                    switchStatus(handlePlayStatusSwitch());
                    break;
                case PLAY_NEXT:
                    try {
                        control.next();
                        updateSong(control.currentSong());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PLAY_PREVIOUS:
                    try {
                        control.pre();
                        updateSong(control.currentSong());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PLAY_FAVORITE_STATUS_SWITCH:

                    break;
                default:
                    break;
            }
        }

        private boolean handlePlayStatusSwitch() {
            try {
                if (control.status() == PlayController.STATUS_PLAYING && control.currentSong() != null) {
                    control.resume();
                    return true;
                } else {
                    control.pause();
                    return false;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
