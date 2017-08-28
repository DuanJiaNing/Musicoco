package com.duan.musicoco.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.OnCompleteListener;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.MediaUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.util.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/20.
 */

public class SongOperation {

    private Activity activity;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private BroadcastManager broadcastManager;

    private static final int ADD_TO_SHEET_NAMES = 0x0;
    private static final int ADD_TO_SHEET_COUNT = 0x1;

    public SongOperation(Activity activity, IPlayControl control, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.broadcastManager = BroadcastManager.getInstance();
    }

    private Map<Integer, String[]> getSheetsData() {
        Map<Integer, String[]> res = new HashMap<>();
        String[] names;
        String[] counts;

        List<Sheet> sheets = dbMusicoco.getSheets();
        names = new String[sheets.size()];
        counts = new String[sheets.size()];
        for (int i = 0; i < sheets.size(); i++) {
            Sheet s = sheets.get(i);
            names[i] = s.name;
            counts[i] = s.count + " " + activity.getString(R.string.head);
        }

        res.put(ADD_TO_SHEET_NAMES, names);
        res.put(ADD_TO_SHEET_COUNT, counts);
        return res;
    }

    private void appendNewSheetOption(OptionsAdapter optionsAdapter, final AlertDialog dialog) {
        OptionsAdapter.Option newSheet = new OptionsAdapter.Option(activity.getString(R.string.new_sheet), 0);
        newSheet.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                new SheetOperation(activity, control, dbMusicoco).addSheet();
                dialog.hide();
            }
        };
        newSheet.iconID = R.drawable.ic_action_add;

        optionsAdapter.addOption(newSheet);
        optionsAdapter.notifyDataSetChanged();
    }

    //反转歌曲收藏状态
    public boolean reverseSongFavoriteStatus(Song song) {
        if (song != null) {
            DBSongInfo info = dbMusicoco.getSongInfo(song);
            if (info != null) {
                boolean isFavorite = info.favorite;
                boolean reverse = !isFavorite;
                dbMusicoco.updateSongFavorite(song, reverse);
                BroadcastManager.getInstance().sendBroadcast(activity, BroadcastManager.FILTER_MAIN_SHEET_UPDATE, null);
                return reverse;
            }
        }

        return false;
    }

    public void handleAddAllSongToFavorite(final int sheetID) {
        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(activity.getString(R.string.tip),
                activity.getString(R.string.info_add_all_songs_to_favorite),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addAllSongToFavorite(sheetID);
                    }
                },
                null,
                true);
        promptDialog.show();

    }

    public void handleSelectSongAddToFavorite(final List<Song> songs, final OnCompleteListener<Void> onCompleteListener) {
        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(
                activity.getString(R.string.favorite),
                activity.getString(R.string.info_add_select_songs_to_favorite),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modifySelectSongFavorite(true, songs, onCompleteListener);
                    }
                },
                null,
                true);
        promptDialog.show();
    }

    public void modifySelectSongFavorite(final boolean favorite, final List<Song> songs, final OnCompleteListener<Void> onCompleteListener) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                for (Song song : songs) {
                    DBSongInfo info = dbMusicoco.getSongInfo(song);
                    if (info != null && info.favorite != favorite) {
                        dbMusicoco.updateSongFavorite(song, favorite);
                    }
                }

                Utils.pretendToRun(500);
                subscriber.onCompleted();
            }
        };

        String title;
        if (favorite) {
            title = activity.getString(R.string.in_progress_add_songs_to_favorite);
        } else {
            title = activity.getString(R.string.in_progress_remove_songs_from_favorite);
        }
        final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(title);
        progressDialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();

                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete(null);
                        }

                        String msg;
                        if (favorite) {
                            msg = activity.getString(R.string.success_add_select_song_to_favorite);
                        } else {
                            msg = activity.getString(R.string.success_remove_select_song_from_favorite);
                        }
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    private void sendBroadcast() {
                        broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();

                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete(null);
                        }

                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    @Override
                    public void onNext(Boolean s) {
                    }
                });
    }

    public void addAllSongToFavorite(final int sheetID) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                List<Song> songs = new ArrayList<>();
                if (sheetID < 0 && sheetID != MainSheetHelper.SHEET_FAVORITE) {
                    MainSheetHelper helper = new MainSheetHelper(activity, dbMusicoco);
                    List<DBSongInfo> info = helper.getMainSheetSongInfo(sheetID);
                    songs = MediaUtils.DBSongInfoListToSongList(info);
                } else {
                    List<DBSongInfo> infos = dbMusicoco.getSongInfos(sheetID);
                    songs = MediaUtils.DBSongInfoListToSongList(infos);
                }

                for (Song song : songs) {
                    DBSongInfo info = dbMusicoco.getSongInfo(song);
                    if (info != null && !info.favorite) {
                        dbMusicoco.updateSongFavorite(song, true);
                    }
                }

                Utils.pretendToRun(500);
                subscriber.onCompleted();
            }
        };

        final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(activity.getString(R.string.in_progress_add_songs_to_favorite));
        progressDialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.success_add_all_song_to_favorite);
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    private void sendBroadcast() {
                        broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    @Override
                    public void onNext(Boolean s) {
                    }
                });
    }

    public void handleAddSongToSheet(final SongInfo info) {
        final Map<Integer, String[]> res = getSheetsData();

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sheetName = res.get(ADD_TO_SHEET_NAMES)[which];
                Song song = new Song(info.getData());
                if (dbMusicoco.addSongToSheet(sheetName, song)) {
                    String msg = activity.getString(R.string.success_add_to_sheet) + " [" + sheetName + "]";
                    ToastUtils.showShortToast(msg, activity);
                } else {
                    ToastUtils.showShortToast(activity.getString(R.string.error_song_is_already_in_sheet), activity);
                }
                dialog.dismiss();
            }
        };

        String msg = activity.getString(R.string.song) + ": " + info.getTitle();
        AlertDialog dialog = createAddSongToSheetDialog(res, msg, listener, true);

        dialog.show();
    }

    public AlertDialog createAddSongToSheetDialog(@NonNull Map<Integer, String[]> res, @Nullable String msg, @Nullable final DialogInterface.OnClickListener listener, boolean newSheetOption) {

        DialogProvider manager = new DialogProvider(activity);
        ListView listView = new ListView(activity);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        OptionsAdapter adapter = new OptionsAdapter(
                activity,
                null,
                null,
                res.get(ADD_TO_SHEET_NAMES),
                res.get(ADD_TO_SHEET_COUNT),
                null);
        adapter.setIconColor(manager.getAccentColor());
        adapter.setTitleColor(manager.getMainTextColor());
        adapter.setMsgColor(manager.getVicTextColor());

        adapter.setPaddingLeft(30);
        listView.setAdapter(adapter);

        int height = Utils.getListViewHeight(listView);
        int screenHeight = Utils.getMetrics(activity).heightPixels;
        if (height >= screenHeight / 2) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenHeight / 2);
            listView.setLayoutParams(params);
        }

        final AlertDialog dialog = manager.createCustomInsiderDialog(
                activity.getString(R.string.song_operation_collection_sheet),
                TextUtils.isEmpty(msg) ? "" : msg,
                listView
        );

        if (newSheetOption) {
            appendNewSheetOption(adapter, dialog);
        }

        if (listener != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.onClick(dialog, position);
                }
            });
        }

        return dialog;
    }

    public void handleAddSongToSheet(final List<Song> songs, final OnCompleteListener<Void> complete) {
        final Map<Integer, String[]> res = getSheetsData();

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sheetName = res.get(ADD_TO_SHEET_NAMES)[which];
                addSongToSheet(songs, complete, sheetName);
                dialog.dismiss();
            }
        };

        String str = activity.getString(R.string.replace_add_songs_to);
        String replace = str.replace("*", songs.size() + "");

        AlertDialog dialog = createAddSongToSheetDialog(res, replace, listener, true);

        dialog.show();
    }

    public void addSongToSheet(final List<Song> songs, final OnCompleteListener<Void> complete, final String sheetName) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                for (Song s : songs) {
                    dbMusicoco.addSongToSheet(sheetName, s);
                }

                Utils.pretendToRun(500);
                subscriber.onCompleted();
            }
        };

        final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(activity.getString(R.string.in_progress_add_songs_to_favorite));
        progressDialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.success_add_to_sheet) + " [" + sheetName + "]";
                        ToastUtils.showShortToast(msg, activity);
                        callBack();
                    }

                    private void callBack() {
                        if (complete != null) {
                            complete.onComplete(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg, activity);
                        callBack();
                    }

                    @Override
                    public void onNext(Boolean s) {
                    }
                });
    }

    public void handleSelectSongCancelFavorite(final List<Song> songs, final OnCompleteListener<Void> complete) {
        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(
                activity.getString(R.string.cancel_favorite),
                activity.getString(R.string.info_remove_select_songs_from_favorite),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modifySelectSongFavorite(false, songs, complete);
                    }
                },
                null,
                true);
        promptDialog.show();

    }

    public void handleSelectSongFavorite(final List<Song> songs, final OnCompleteListener<Void> complete) {

        DialogProvider provider = new DialogProvider(activity);
        int[] colors = new int[5];
        Arrays.fill(colors, provider.getMainTextColor());
        colors[4] = provider.getVicTextColor();

        View view = ViewUtils.getSelectFavoriteOptionsView(activity, null, null, colors);
        String title = activity.getString(R.string.select_operation);
        final AlertDialog dialog = provider.createCustomInsiderDialog(title, null, view);

        View fit = view.findViewById(R.id.select_favorite_true);
        View fif = view.findViewById(R.id.select_favorite_false);
        fit.setOnClickListener(new View.OnClickListener() { // 收藏
            @Override
            public void onClick(View v) {
                modifySelectSongFavorite(true, songs, complete);
                dialog.dismiss();
            }
        });
        fif.setOnClickListener(new View.OnClickListener() { // 收藏
            @Override
            public void onClick(View v) {
                modifySelectSongFavorite(false, songs, complete);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void handleRemoveSongFromCurrentPlayingSheet(final @Nullable OnCompleteListener<Void> complete, final List<Song> songs) {

        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(
                activity.getString(R.string.remove),
                activity.getString(R.string.info_remove_select_songs_from_sheet),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Song[] s = songs.toArray(new Song[songs.size()]);
                        removeSongFromCurrentPlayingSheet(complete, s);
                    }
                },
                null,
                true);
        promptDialog.show();
    }

    //从当前正在播放的歌单中移除
    public void removeSongFromCurrentPlayingSheet(final @Nullable OnCompleteListener<Void> complete, final Song... songs) {
        if (songs.length == 1) {
            try {
                //需要在服务器移除前修改数据库
                int sheetID = control.getPlayListId();
                dbMusicoco.removeSongInfoFromSheet(songs[0], sheetID);
                control.remove(songs[0]);

                String msg = activity.getString(R.string.success_remove_song_from_sheet);
                ToastUtils.showShortToast(msg, activity);

                broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                if (complete != null) {
                    complete.onComplete(null);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {

            Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    subscriber.onStart();

                    try {
                        //需要在服务器移除前修改数据库
                        // UPDATE: 2017/8/24 更新 移除歌曲里包括正在播放的歌曲时出错
                        int sheetID = control.getPlayListId();
                        for (Song song : songs) {
                            dbMusicoco.removeSongInfoFromSheet(song, sheetID);
                            control.remove(song);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    Utils.pretendToRun(300);
                    subscriber.onCompleted();
                }
            };

            String title = activity.getString(R.string.in_progress_remove_songs_from_sheet);
            final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(title);
            progressDialog.setCancelable(false);

            Observable.create(onSubscribe)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {

                        @Override
                        public void onStart() {
                            progressDialog.show();
                        }

                        @Override
                        public void onCompleted() {
                            progressDialog.dismiss();

                            String msg = activity.getString(R.string.success_remove_song_from_sheet);
                            ToastUtils.showShortToast(msg, activity);
                            sendBroadcast();
                        }

                        private void sendBroadcast() {
                            broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                            if (complete != null) {
                                complete.onComplete(null);
                            }
                        }


                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                            String msg = activity.getString(R.string.unknown);
                            ToastUtils.showShortToast(msg, activity);
                            sendBroadcast();
                        }

                        @Override
                        public void onNext(Boolean s) {
                        }
                    });
        }
    }

    public void handleRemoveSongFromSheetNotPlaying(final @Nullable OnCompleteListener<Void> complete, final int sheetID, final List<Song> songs) {
        final Dialog promptDialog = new DialogProvider(activity).createPromptDialog(
                activity.getString(R.string.remove),
                activity.getString(R.string.info_remove_select_songs_from_sheet),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Song[] s = songs.toArray(new Song[songs.size()]);
                        removeSongFromSheetNotPlaying(complete, sheetID, s);
                    }
                },
                null,
                true);
        promptDialog.show();
    }

    //从歌单中移除
    public void removeSongFromSheetNotPlaying(final OnCompleteListener<Void> complete, final int sheetID, final Song... songs) {

        if (songs.length == 1) {

            dbMusicoco.removeSongInfoFromSheet(songs[0], sheetID);

            String msg = activity.getString(R.string.success_remove_song_from_sheet);
            ToastUtils.showShortToast(msg, activity);

            broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
            if (complete != null) {
                complete.onComplete(null);
            }
        } else {

            Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    subscriber.onStart();

                    for (Song song : songs) {
                        dbMusicoco.removeSongInfoFromSheet(song, sheetID);
                    }

                    Utils.pretendToRun(300);
                    subscriber.onCompleted();
                }
            };

            String title = activity.getString(R.string.in_progress_remove_songs_from_sheet);
            final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(title);
            progressDialog.setCancelable(false);

            Observable.create(onSubscribe)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {

                        @Override
                        public void onStart() {
                            progressDialog.show();
                        }

                        @Override
                        public void onCompleted() {
                            progressDialog.dismiss();

                            String msg = activity.getString(R.string.success_remove_song_from_sheet);
                            ToastUtils.showShortToast(msg, activity);
                            sendBroadcast();
                        }

                        private void sendBroadcast() {
                            broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                            if (complete != null) {
                                complete.onComplete(null);
                            }
                        }


                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            progressDialog.dismiss();

                            String msg = activity.getString(R.string.unknown);
                            ToastUtils.showShortToast(msg, activity);
                            sendBroadcast();
                        }

                        @Override
                        public void onNext(Boolean s) {
                        }
                    });
        }
    }

    public void handleDeleteSongForever(final Song song, final OnCompleteListener<Void> complete) {
        DialogProvider manager = new DialogProvider(activity);
        final Dialog dialog = manager.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.info_delete_confirm),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSongFromDiskAndLibraryForever(song);
                        checkIsPlaying(song);
                        if (complete != null) {
                            complete.onComplete(null);
                        }
                    }
                },
                null,
                true
        );
        dialog.show();
    }

    // 检查当前歌曲是否正在播放，若正在播放则将其从服务器播放列表中移除
    private void checkIsPlaying(Song s) {
        try {
            Song song = control.currentSong();
            if (s.equals(song)) {
                control.remove(s);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void deleteSongFromDiskAndLibraryForever(Song song) {

        String msg = activity.getString(R.string.error_delete_file_fail);
        if (FileUtils.deleteFile(song.path)) {
            dbMusicoco.removeSongInfo(song);
            msg = activity.getString(R.string.success_delete_file);
        }

        ToastUtils.showShortToast(msg, activity);
    }

    public void handleDeleteSongForever(final OnCompleteListener<Void> complete, final int sheetID, final List<Song> songs) {
        DialogProvider manager = new DialogProvider(activity);
        final Dialog dialog = manager.createPromptDialog(
                activity.getString(R.string.warning),
                activity.getString(R.string.info_delete_select_confirm),
                new DialogProvider.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSongFromDiskAndLibraryForever(complete, sheetID, songs);
                    }
                },
                null,
                true
        );
        dialog.show();
    }

    public void deleteSongFromDiskAndLibraryForever(final OnCompleteListener<Void> complete, final int sheetID, final List<Song> songs) {

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onStart();

                try {
                    // UPDATE: 2017/8/26 更新 删除多首歌曲时，删除歌曲里包含正在播放的歌曲时，服务器的播放列表和歌单显示的列表不一样
                    Song song = control.currentSong();

                    for (Song s : songs) {
                        dbMusicoco.removeSongInfo(s);
                        if (s.equals(song)) {
                            control.remove(song);
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                Utils.pretendToRun(300);
                subscriber.onCompleted();
            }
        };

        String title = activity.getString(R.string.in_progress_delete);
        final Dialog progressDialog = new DialogProvider(activity).createProgressDialog(title);
        progressDialog.setCancelable(false);

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.success_delete_file);
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    private void sendBroadcast() {
                        if (complete != null) {
                            complete.onComplete(null);
                        }
                        broadcastManager.sendBroadcast(activity, BroadcastManager.FILTER_SHEET_DETAIL_SONGS_UPDATE, null);
                    }


                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.dismiss();

                        String msg = activity.getString(R.string.unknown);
                        ToastUtils.showShortToast(msg, activity);
                        sendBroadcast();
                    }

                    @Override
                    public void onNext(Boolean s) {
                    }
                });
    }

    public void playSongAtSheetAll(Song song) {
        try {

            int sid = control.getPlayListId();
            if (sid != MainSheetHelper.SHEET_ALL) {
                control.pause();
                control.setPlaySheet(MainSheetHelper.SHEET_ALL, 0);
                control.play(song);
            } else {
                control.play(song);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
