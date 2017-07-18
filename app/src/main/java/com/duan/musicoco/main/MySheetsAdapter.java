package com.duan.musicoco.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.view.media.PlayView;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsAdapter extends BaseAdapter implements
        OnThemeChange {

    private Context context;
    private List<DBMusicocoController.Sheet> sheets;
    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;

    private int colorMain;
    private int colorVic;
    private int colorMainB;

    private Bitmap defaultBitmap;

    private View.OnClickListener moreClickListener;
    private PlayView.OnCheckedChangeListener playCheckChangeListener;

    public MySheetsAdapter(final Context context, List<DBMusicocoController.Sheet> sheets, DBMusicocoController dbMusicoco, MediaManager mediaManager) {
        this.context = context;
        this.sheets = sheets;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;

        moreClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) v.getTag();
                Toast.makeText(context, "OnClickListener sheet more " + sheet.name, Toast.LENGTH_SHORT).show();
            }
        };

        playCheckChangeListener = new PlayView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(PlayView view, boolean checked) {
                DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) view.getTag();
                Toast.makeText(context, "check status change " + sheet.name + " checked=" + checked, Toast.LENGTH_SHORT).show();
            }
        };
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
            convertView = LayoutInflater.from(context).inflate(R.layout.my_sheet_list_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.sheets_item_image);
            holder.play = (PlayView) convertView.findViewById(R.id.sheets_item_play);
            holder.name = (TextView) convertView.findViewById(R.id.sheets_item_name);
            holder.remark = (TextView) convertView.findViewById(R.id.sheets_item_remark);
            holder.count = (TextView) convertView.findViewById(R.id.sheets_item_song_count);
            holder.playTimes = (TextView) convertView.findViewById(R.id.sheets_item_play_times);
            holder.more = (ImageButton) convertView.findViewById(R.id.sheets_item_more);

            if (defaultBitmap == null) {
                defaultBitmap = BitmapUtils.bitmapResizeFromResource(context.getResources(), R.drawable.default_sheet, holder.image.getWidth(), holder.image.getHeight());

            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DBMusicocoController.Sheet sheet = (DBMusicocoController.Sheet) getItem(position);
        String name = sheet.name;
        String remark = sheet.remark;
        int count = sheet.count;
        int playTimes = sheet.playTimes;

        bindImage(holder, sheet);

        holder.more.setTag(sheet);
        holder.more.setOnClickListener(moreClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.more.getDrawable().setTint(colorVic);
        }

        holder.play.setTag(sheet);
        holder.play.setTriangleColor(colorMainB);
        holder.play.setPauseLineColor(colorMainB);
        holder.play.setOnCheckedChangeListener(playCheckChangeListener);

        holder.name.setTextColor(colorMain);
        holder.name.setText(name);

        holder.remark.setTextColor(colorVic);
        holder.remark.setText(remark);

        holder.count.setTextColor(colorVic);
        holder.count.setText(count + "首");

        holder.playTimes.setTextColor(colorVic);
        holder.playTimes.setText(playTimes + "次");

        return convertView;
    }

    private void bindImage(final ViewHolder holder, final DBMusicocoController.Sheet sheet) {
        Observable.just(sheet.id)
                .map(new Func1<Integer, Bitmap>() {
                    @Override
                    public Bitmap call(Integer integer) {
                        List<DBMusicocoController.SongInfo> infos = dbMusicoco.getSongInfos(integer);
                        TreeSet<DBMusicocoController.SongInfo> treeSet = dbMusicoco.descSortByLastPlayTime(infos);
                        Bitmap bitmap = findBitmap(treeSet, holder.image);
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

                        AnimationUtils.startAlphaAnim(holder.image, 400, null, 0.5f, 1.0f);
                    }
                });
    }

    private Bitmap findBitmap(TreeSet<DBMusicocoController.SongInfo> treeSet, ImageView image) {
        Bitmap bitmap = null;
        for (DBMusicocoController.SongInfo s : treeSet) {
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

        colorMain = colors[0];
        colorVic = colors[1];
        colorMainB = colors[2];

        notifyDataSetChanged();
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
