package com.duan.musicoco.shared;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/7/1.
 */

public class PeriodicTask {

    private TimerTask progressUpdateTask;
    private final Task task;
    private final int period;
    private boolean isSchedule = false;

    public interface Task {
        /**
         * 执行周期任务，注意该方法不在主线程执行，不能在其中访问 UI 控件
         */
        void execute();
    }

    /**
     * 执行周期性周期任务
     *
     * @param task   执行内容
     * @param period 周期
     */
    public PeriodicTask(Task task, int period) {
        this.task = task;
        this.period = period;
    }

    public void stop() {
        if (!isSchedule()) {
            return;
        }

        if (progressUpdateTask != null) {
            progressUpdateTask.cancel();
            isSchedule = false;
        }
    }

    public void start() {
        if (isSchedule()) {
            return;
        }

        Timer timer = new Timer();
        progressUpdateTask = new TimerTask() {
            @Override
            public void run() {
                task.execute();
            }
        };
        timer.schedule(progressUpdateTask, 0, period);
        isSchedule = true;
    }

    public boolean isSchedule() {
        return isSchedule;
    }

}
