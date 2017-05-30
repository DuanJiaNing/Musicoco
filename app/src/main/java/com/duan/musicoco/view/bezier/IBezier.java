package com.duan.musicoco.view.bezier;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public interface IBezier {

    /**
     * 计算贝塞尔曲线上点的坐标
     *
     * @param poss      控制点坐标
     * @param precision 精度（要返回坐标点个数）
     * @return 坐标
     */
    float[][] calculate(float[][] poss, int precision);

}
