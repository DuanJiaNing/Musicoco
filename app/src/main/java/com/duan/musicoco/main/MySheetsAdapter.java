package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.OnCompleteListener;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.SheetOperation;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.MediaUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.media.PlayView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsAdapter extends BaseAdapter implements
        ThemeChangeable,
        PlayView.OnCheckedChangeListener {

    private final Activity activity;
    private final List<Sheet> sheets;
    private final DBMusicocoController dbMusicoco;
    private final MediaManager mediaManager;
    private final SheetOperation sheetOperation;
    private final IPlayControl control;

    private int mainTC;
    private int mainBC;
    private int vicTC;
    private int vicBC;
    private int accentC;
    private int itemBGC;

    private final OptionsDialog mDialog;
    private OptionsAdapter moreOptionsAdapter;

    private Bitmap defaultBitmap;

    private View.OnClickListener moreClickListener;

    private Sheet currentClickMoreOperationItem;

    public MySheetsAdapter(Activity activity, List<Sheet> sheets,
                           DBMusicocoController dbMusicoco, MediaManager mediaManager,
                           IPlayControl control, SheetOperation sheetOperation) {
        this.activity = activity;
        this.sheets = sheets;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.sheetOperation = sheetOperation;
        this.mDialog = new OptionsDialog(activity);

        moreOptionsAdapter = new OptionsAdapter(activity);
        initAdapterData();
        mDialog.setAdapter(moreOptionsAdapter);

        moreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog.visible()) {
                    mDialog.hide();
                } else {
                    Sheet sheet = (Sheet) v.getTag();
                    currentClickMoreOperationItem = sheet;
                    String title = MySheetsAdapter.this.activity.getString(R.string.sheet) + ": " + sheet.name;
                    mDialog.setTitle(title);
                    mDialog.show();
                }
            }
        };

    }

    private void initAdapterData() {
        String mt = activity.getString(R.string.sheet_operation_modify);
        int mi = R.drawable.ic_poll;
        OptionsAdapter.Option modify = new OptionsAdapter.Option(mt, 0);
        modify.iconID = mi;
        modify.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                sheetOperation.modifySheet(currentClickMoreOperationItem);
                mDialog.hide();
            }
        };
        moreOptionsAdapter.addOption(modify);

        String dt = activity.getString(R.string.sheet_operation_delete);
        int di = R.drawable.ic_delete_forever_black_24dp;
        OptionsAdapter.Option delete = new OptionsAdapter.Option(dt, 1);
        delete.iconID = di;
        delete.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                sheetOperation.handleDeleteSheet(currentClickMoreOperationItem, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(Boolean aBoolean) {
                        if (aBoolean) {
                            String msg = activity.getString(R.string.success_delete_sheet) + " [" + currentClickMoreOperationItem.name + "]";
                            ToastUtils.showShortToast(msg, activity);

                            isCurrentPlayingSheetBeDelete(currentClickMoreOperationItem.id);

                        } else {
                            String msg = activity.getString(R.string.error_delete_sheet_fail);
                            ToastUtils.showShortToast(msg, activity);
                        }
                    }
                });
                mDialog.hide();
            }
        };
        moreOptionsAdapter.addOption(delete);

    }

    /**
     * 当前播放歌单属于被删除歌单时需将播放列表置为【全部歌单】
     */
    public void isCurrentPlayingSheetBeDelete(int sheetID) {

        try {
            int cursid = control.getPlayListId();
            if (sheetID == cursid) {

                MainSheetHelper helper = new MainSheetHelper(activity, dbMusicoco);
                List<DBSongInfo> list = helper.getAllSongInfo();
                List<Song> songs = MediaUtils.DBSongInfoListToSongList(list);
                control.setPlayList(songs, 0, MainSheetHelper.SHEET_ALL);
                control.pause();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getCount() {
        return sheets.size();
    }

    @Override
    public Sheet getItem(int position) {
        return sheets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Sheet sheet = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.my_sheet_list_item, null);
            holder = new ViewHolder(convertView);

            if (defaultBitmap == null) {
                defaultBitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet, holder.image.getWidth(), holder.image.getHeight());

            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String name = sheet.name;
        String remark = sheet.remark;
        int count = sheet.count;
        int playTimes = sheet.playTimes;

        holder.image.setTag(sheet.name);
        bindImage(holder.image, sheet);

        holder.more.setTag(sheet);
        holder.more.setOnClickListener(moreClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.more.getDrawable().setTint(vicTC);
        }

        holder.play.setTag(sheet);
        holder.play.setTriangleColor(mainBC);
        holder.play.setPauseLineColor(accentC);
        holder.play.setOnCheckedChangeListener(this);

        try {
            if (control.getPlayListId() == sheet.id &&
                    control.status() == PlayController.STATUS_PLAYING) {
                holder.play.setChecked(true);
            } else {
                holder.play.setChecked(false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        holder.name.setTextColor(mainTC);
        holder.name.setText(name);

        holder.remark.setTextColor(vicTC);
        holder.remark.setText(remark);

        holder.count.setTextColor(vicTC);
        holder.count.setText(count + activity.getString(R.string.head));

        holder.playTimes.setTextColor(vicTC);
        if (playTimes > 0) {
            holder.playTimes.setText(playTimes + activity.getString(R.string.count));
        }

        convertView.setBackgroundColor(itemBGC);

        return convertView;
    }

    private void bindImage(final ImageView image, final Sheet sheet) {
        Observable.just(sheet.id)
                .map(new Func1<Integer, Bitmap>() {
                    @Override
                    public Bitmap call(Integer integer) {
                        //Java.util.ConcurrentModificationException
                        // UPDATE: 2017/8/26 修复 多线程导致迭代时修改错误
                        List<DBSongInfo> infos = dbMusicoco.getSongInfos(integer);
//                        TreeSet<DBMusicocoController.DBSongInfo> treeSet = dbController.descSortByLastPlayTime(infos);
                        Bitmap bitmap = findBitmap(infos, image);
                        if (bitmap == null) {
                            bitmap = defaultBitmap;
                        }
                        return bitmap;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (((String) image.getTag()).equals(sheet.name)) {
                            image.setImageBitmap(bitmap);
                        } else {
                            image.setImageBitmap(defaultBitmap);
                        }
                    }
                });
    }

    private Bitmap findBitmap(List<DBSongInfo> treeSet, ImageView image) {
        Bitmap bitmap = null;
        for (DBSongInfo s : treeSet) {
            SongInfo info = mediaManager.getSongInfo(activity, s.path);
            bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), image.getWidth(), image.getHeight());
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        mainBC = colors[3];
        mainTC = colors[5];
        vicBC = colors[4];
        vicTC = colors[6];
        accentC = colors[2];

        itemBGC = themeEnum == ThemeEnum.WHITE ? Color.WHITE : vicBC;

        mDialog.setTitleBarBgColor(vicBC);
        mDialog.setContentBgColor(mainBC);
        mDialog.setDivideColor(vicTC);
        mDialog.setTitleTextColor(mainTC);

        moreOptionsAdapter.setTitleColor(mainTC);
        moreOptionsAdapter.setIconColor(accentC);

        notifyDataSetChanged();
    }

    @Override
    public void onCheckedChanged(PlayView view, boolean checked) {
        Sheet sheet = (Sheet) view.getTag();

        try {

            int sheetID = control.getPlayListId();
            if (checked && sheet.id != sheetID) { // 播放状态且当前播放歌单不是目标歌单
                changePlayList(sheet);
            } else if (checked && sheet.id == sheetID) { //播放状态且当前歌单是目标歌单
                control.resume();
            } else if (!checked) { // 停止播放
                control.pause();
            }
            notifyDataSetChanged();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void changePlayList(Sheet sheet) throws RemoteException {
        List<DBSongInfo> songInfos = dbMusicoco.getSongInfos(sheet.id);
        if (songInfos.size() > 0) {
            List<Song> songs = MediaUtils.DBSongInfoListToSongList(songInfos);

            //BottomNavigationController#onPlayListChange 被回调，在那里更新 PlayListAdapter
            control.setPlayList(songs, 0, sheet.id);
            control.resume();
        } else {
            ToastUtils.showShortToast(activity.getString(R.string.error_empty_sheet), activity);
        }
    }

    private class ViewHolder {
        ImageView image;
        PlayView play;
        TextView name;
        TextView remark;
        TextView count;
        TextView playTimes;
        ImageButton more;

        public ViewHolder(View convertView) {
            image = (ImageView) convertView.findViewById(R.id.sheets_item_image);
            play = (PlayView) convertView.findViewById(R.id.sheets_item_play);
            name = (TextView) convertView.findViewById(R.id.sheets_item_name);
            remark = (TextView) convertView.findViewById(R.id.sheets_item_remark);
            count = (TextView) convertView.findViewById(R.id.sheets_item_song_count);
            playTimes = (TextView) convertView.findViewById(R.id.sheets_item_play_times);
            more = (ImageButton) convertView.findViewById(R.id.sheets_item_more);

        }
    }
}
