package com.duan.musicoco.main.leftnav;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.AuxiliaryPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.PeriodicTask;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;

/**
 * Created by DuanJiaNing on 2017/8/10.
 */

public class LeftNavigationController implements
        ViewVisibilityChangeable,
        NavigationView.OnNavigationItemSelectedListener,
        ThemeChangeable {

    private final Activity activity;
    private final AppPreference appPreference;
    protected DBMusicocoController dbController;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private HomeBackgroundController homeBackgroundController;
    private ActivityManager activityManager;
    private AuxiliaryPreference auxiliaryPreference;

    private DrawerListener drawerListener;

    public LeftNavigationController(Activity activity, AppPreference appPreference, AuxiliaryPreference auxiliaryPreference) {
        this.activity = activity;
        this.appPreference = appPreference;
        this.auxiliaryPreference = auxiliaryPreference;
        this.homeBackgroundController = new HomeBackgroundController(activity, appPreference);
        this.activityManager = ActivityManager.getInstance();
        this.drawerListener = new DrawerListener();
    }

    public void initViews() {
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        homeBackgroundController.initViews(navigationView);
        drawerLayout.addDrawerListener(drawerListener);
        updateSwitchMenuIconAndText();
    }


    public void initData(DBMusicocoController dbController) {
        this.dbController = dbController;

        homeBackgroundController.initData(dbController);
        initNavImage();

    }

    private void initNavImage() {

        navigationView.post(new Runnable() {
            @Override
            public void run() {
                ImageView iv = (ImageView) navigationView.findViewById(R.id.main_left_nav_image);
                if (iv != null) {
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            homeBackgroundController.updateImage();
                        }
                    });
                }

            }
        });
    }


    public boolean onBackPressed() {
        if (visible()) {
            hide();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void show() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void hide() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean visible() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.setting_night_mode) {
            handleModeSwitch();
        } else {
            drawerListener.id = id;
            drawerListener.actionAfterClose = true;
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return false;
    }

    private void handleModeSwitch() {
        ThemeEnum theme = ThemeEnum.reversal(appPreference.getTheme());

        appPreference.updateTheme(theme);
        ((MainActivity) activity).switchThemeMode(theme);

        // 播放界面更新主题（底部弹出的选项框）
        BroadcastManager manager = BroadcastManager.getInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(BroadcastManager.Play.PLAY_THEME_CHANGE_TOKEN, BroadcastManager.Play.PLAY_APP_THEME_CHANGE);
        manager.sendBroadcast(activity, BroadcastManager.FILTER_PLAY_UI_MODE_CHANGE, bundle);

    }

    private void updateSwitchMenuIconAndText() {
        MenuItem item;
        Menu menu = navigationView.getMenu();
        if (menu != null) {
            item = menu.findItem(R.id.setting_night_mode);
            if (item == null) {
                return;
            }
        } else {
            return;
        }

        ThemeEnum theme = appPreference.getTheme();

        Drawable icon;
        String title;

        if (theme == ThemeEnum.WHITE || theme == ThemeEnum.VARYING) { // 白天模式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = activity.getDrawable(R.drawable.ic_night);
            } else {
                icon = activity.getResources().getDrawable(R.drawable.ic_night);
            }
            title = activity.getString(R.string.setting_night_mode);
        } else { // 切换到 白天模式

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                icon = activity.getDrawable(R.drawable.ic_daytime);
            } else { // 切换到 白天模式
                icon = activity.getResources().getDrawable(R.drawable.ic_daytime);
            }
            title = activity.getString(R.string.setting_daytime_mode);
        }

        item.setIcon(icon);
        item.setTitle(title);

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        ThemeEnum th = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(activity, th);
        int mainBC = cs[3];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int accentC = cs[2];

        navigationView.setItemTextColor(ColorStateList.valueOf(mainTC));
        navigationView.setBackgroundColor(mainBC);

        updateSwitchMenuIconAndText();

        Menu menu = navigationView.getMenu();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(0);
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon.setTint(accentC);
                }
            }
        }
    }

    /**
     * 用于定时停止播放功能，执行期间不应该调用 stop 方法，只应该在 CountDown 结束时才调用
     */
    private PeriodicTask quitCountDown = null;
    private CountDownTask task = null;

    public void startQuitCountdown() {
        if (quitCountDown == null) {
            task = new CountDownTask();
            quitCountDown = new PeriodicTask(task, 1000);
        } else {
            task.reset();
        }
        quitCountDown.start();
    }

    public void stopQuitCountdown(boolean resetText) {
        if (quitCountDown != null && quitCountDown.isSchedule()) {
            quitCountDown.stop();
        }
        auxiliaryPreference.setTimeSleepDisable();

        if (resetText) {
            resetText();
        }
    }

    private void resetText() {
        Menu menu = navigationView.getMenu();
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.setting_sleep);
            item.setTitle(R.string.setting_time_sleep);
        }
    }

    private class CountDownTask implements PeriodicTask.Task {
        final MenuItem item;
        int dur;
        long startTime;
        final String title;

        // dur 的单位为 分
        public CountDownTask() {
            reset();

            Menu menu = navigationView.getMenu();
            if (menu != null) {
                item = menu.findItem(R.id.setting_sleep);
                title = item.getTitle().toString();
            } else {
                item = null;
                title = "";
            }

        }

        int sec = 59;

        @Override
        public void execute() {
            if (dur >= 0 && item != null) {
                if (dur == 0) {
                    if (sec == 0) {
                        countDownFinish(false);
                    } else if (sec == 23) { // 倒数 23 秒提醒用户 20s 将退出
                        navigationView.post(new Runnable() {
                            @Override
                            public void run() {
                                String msg = activity.getString(R.string.info_app_wile_quit_after_20s);
                                ToastUtils.showShortToast(msg, activity);
                            }
                        });
                    }
                }

                if (sec == 0) {
                    sec = 59;
                    dur--;
                } else {
                    sec--;
                }
                countDown();
            } else {
                countDownFinish(false);
            }
        }

        private void countDownFinish(boolean resetText) {
            stopQuitCountdown(resetText);

            //定时自然停止时应用也应关闭
            ((MainActivity) activity).shutDownServiceAndApp();
        }

        private void countDown() {
            navigationView.post(new Runnable() {
                @Override
                public void run() {
                    String newTitle = title + "    " + getString();
                    item.setTitle(newTitle);
                }
            });
        }

        private String getString() {
            String sq = " : ";
            String time;
            if (dur >= 60) {
                int h = dur / 60;
                int m = dur % 60;
                time = get2Str(h) + sq + get2Str(m) + sq + get2Str(sec);
            } else {
                int m = dur % 60;
                time = get2Str(m) + sq + get2Str(sec);
            }
            return time;
        }

        private String get2Str(int m) {
            return m < 10 ? ("0" + m) : (m + "");
        }

        private void reset() {
            dur = auxiliaryPreference.getTimeSleepDuration();
            startTime = auxiliaryPreference.getTimeSleepStartTime();
            // dur = 2 ,开始倒数时从 1:59 开始
            dur--;
        }
    }

    private class DrawerListener implements DrawerLayout.DrawerListener {

        int id;
        boolean actionAfterClose = false;

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

            if (actionAfterClose) {
                switch (id) {
                    case R.id.setting_sleep: // 睡眠定时
                        activityManager.startTimeSleepActivity(activity);
                        break;
                    case R.id.setting_image_wall: // 照片墙
                        activityManager.startImageWallActivity(activity);
                        break;
                    case R.id.setting_play_ui: // 播放界面风格
                        activityManager.startPlayThemeCustomActivity(activity);
                        break;
                    case R.id.setting_theme_color_custom: // 主题色
                        activityManager.startThemeColorCustomActivity(activity);
                        ((MainActivity) activity).updateColorByCustomThemeColor();
                        break;
                    case R.id.setting_set: // 设置
                        activityManager.startSettingActivity(activity);
                        break;
                    case R.id.setting_quit: // 退出
                        ((MainActivity) activity).shutDownServiceAndApp();
                        break;
                    case R.id.setting_user_guide: // 用户指南
                        ActivityManager.getInstance().startWebActivity(activity, activity.getString(R.string.guide_url));
                        break;
                    default:
                        break;
                }
                actionAfterClose = false;
            }

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }
}
