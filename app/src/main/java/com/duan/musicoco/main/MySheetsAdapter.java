package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.DBSongInfo;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.MySheetsOperation;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.media.PlayView;

import java.util.ArrayList;
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
        OnThemeChange,
        PlayView.OnCheckedChangeListener {

    private final Activity activity;
    private final List<Sheet> sheets;
    private final DBMusicocoController dbMusicoco;
    private final MediaManager mediaManager;
    private final MySheetsOperation mySheetsOperation;
    private final IPlayControl control;

    private int mainTC;
    private int mainBC;
    private int vicTC;
    private int vicBC;
    private final OptionsDialog mDialog;
    private OptionsAdapter moreOptionsAdapter;

    private Bitmap defaultBitmap;

    private View.OnClickListener moreClickListener;

    private Sheet currentClickMoreOperationItem;

    public MySheetsAdapter(Activity activity, List<Sheet> sheets,
                           DBMusicocoController dbMusicoco, MediaManager mediaManager,
                           IPlayControl control, MySheetsOperation mySheetsOperation) {
        this.activity = activity;
        this.sheets = sheets;
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.mySheetsOperation = mySheetsOperation;
        this.mDialog = new OptionsDialog(activity);

        moreOptionsAdapter = new OptionsAdapter(activity);
        initAdapterData();
        mDialog.setAdapter(moreOptionsAdapter);

        moreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog.isShowing()) {
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
        OptionsAdapter.Option modify = new OptionsAdapter.Option(mt);
        modify.iconID = mi;
        modify.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position) {
                mySheetsOperation.handleModifySheet(currentClickMoreOperationItem);
                mDialog.hide();
            }
        };
        moreOptionsAdapter.addOption(modify);

        String dt = activity.getString(R.string.sheet_operation_delete);
        int di = R.drawable.ic_delete_forever_black_24dp;
        OptionsAdapter.Option delete = new OptionsAdapter.Option(dt);
        delete.iconID = di;
        delete.clickListener = new OptionsAdapter.OptionClickListener() {
            @Override
            public void onClick(OptionsAdapter.ViewHolder holder, int position) {
                mySheetsOperation.deleteSheet(currentClickMoreOperationItem);
                mDialog.hide();
            }
        };
        moreOptionsAdapter.addOption(delete);

    }

    private void changePlayList(Sheet sheet) throws RemoteException {
        List<DBSongInfo> songInfos = MySheetsAdapter.this.dbMusicoco.getSongInfos(sheet.id);
        if (songInfos.size() > 0) {
            List<Song> songs = new ArrayList<>();
            for (DBSongInfo s : songInfos) {
                Song song = new Song(s.path);
                songs.add(song);
            }

            control.setPlayList(songs, 0, sheet.id);
            control.playByIndex(0);
        } else {
            ToastUtils.showShortToast(activity, activity.getString(R.string.error_empty_sheet));
        }
    }

    @Override
    public int getCount() {
        return sheets.size();
    }

    @Override
    public Object getItem(int position) {
        return sheets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.my_sheet_list_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.sheets_item_image);
            holder.play = (PlayView) convertView.findViewById(R.id.sheets_item_play);
            holder.name = (TextView) convertView.findViewById(R.id.sheets_item_name);
            holder.remark = (TextView) convertView.findViewById(R.id.sheets_item_remark);
            holder.count = (TextView) convertView.findViewById(R.id.sheets_item_song_count);
            holder.playTimes = (TextView) convertView.findViewById(R.id.sheets_item_play_times);
            holder.more = (ImageButton) convertView.findViewById(R.id.sheets_item_more);

            if (defaultBitmap == null) {
                defaultBitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet, holder.image.getWidth(), holder.image.getHeight());

            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Sheet sheet = (Sheet) getItem(position);
        String name = sheet.name;
        String remark = sheet.remark;
        int count = sheet.count;
        int playTimes = sheet.playTimes;

        bindImage(holder, sheet);

        holder.more.setTag(sheet);
        holder.more.setOnClickListener(moreClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.more.getDrawable().setTint(vicTC);
        }

        holder.play.setTag(sheet);
        holder.play.setTriangleColor(mainBC);
        holder.play.setPauseLineColor(mainBC);
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
        holder.count.setText(count + "首");

        holder.playTimes.setTextColor(vicTC);
        holder.playTimes.setText(playTimes + "次");

        return convertView;
    }

    private void bindImage(final ViewHolder holder, final Sheet sheet) {
        Observable.just(sheet.id)
                .map(new Func1<Integer, Bitmap>() {
                    @Override
                    public Bitmap call(Integer integer) {
                        //Java.util.ConcurrentModificationException
                        //FIXME 多线程导致迭代时修改错误
                        List<DBSongInfo> infos = dbMusicoco.getSongInfos(integer);
//                        TreeSet<DBMusicocoController.DBSongInfo> treeSet = dbMusicoco.descSortByLastPlayTime(infos);
                        Bitmap bitmap = findBitmap(infos, holder.image);
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
                        holder.image.setImageBitmap(bitmap);

                        ColorDrawable drawable = new ColorDrawable(Color.BLACK);
                        drawable.setAlpha(100);
                        holder.play.setBackground(drawable);

                    }
                });
    }

    private Bitmap findBitmap(List<DBSongInfo> treeSet, ImageView image) {
        Bitmap bitmap = null;
        for (DBSongInfo s : treeSet) {
            SongInfo info = mediaManager.getSongInfo(s.path);
            bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), image.getWidth(), image.getHeight());
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        mainBC = colors[0];
        mainTC = colors[1];
        vicBC = colors[2];
        vicTC = colors[3];

        mDialog.setTitleBarBgColor(vicBC);
        mDialog.setContentBgColor(mainBC);
        mDialog.setDivideColor(vicTC);
        mDialog.setTitleTextColor(mainTC);

        moreOptionsAdapter.setTextColor(mainTC);
        moreOptionsAdapter.setIconColor(vicTC);

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

    private class ViewHolder {
        ImageView image;
        PlayView play;
        TextView name;
        TextView remark;
        TextView count;
        TextView playTimes;
        ImageButton more;
    }
}
