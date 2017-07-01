package com.duan.musicoco.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/7/1.
 */

public class PeriodicTask {

    private TimerTask progressUpdateTask;
    private final Task task;
    private final int delay;

    public interface Task {
        void execute();
    }

    public PeriodicTask(Task task, int delay) {
        this.task = task;
        this.delay = delay;
    }

    public void stop() {
        if (progressUpdateTask != null)
            progressUpdateTask.cancel();
    }

    public void start() {
        Timer timer = new Timer();
        progressUpdateTask = new TimerTask() {
            @Override
            public void run() {
                task.execute();
            }
        };
        timer.schedule(progressUpdateTask, 0, delay);
    }

}
