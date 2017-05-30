package com.duan.musicoco.view.bezier;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class BezierImpl implements IBezier {

    /**
     * 1.
     * 设 P 为要计算的坐标，N 为控制点个数，P0,P1,P2..Pn 为贝塞尔曲线控制点的坐标，当 N 值不同时有如下计算公式: <br>
     * 如 N 为 3 表示贝塞尔曲线的控制点有 3 个点，这时 n 为 2 ，这三个点分别用 P0,P1,P2 表示。 <br>
     * N = 3: P = (1-t)^2*P0 + 2*(1-t)*t*P1 + t^2*P2 <br>
     * N = 4: P = (1-t)^3*P0 + 3*(1-t)^2*t*P1 + 3(1-t)*t^2*P2 + t^3*P3 <br>
     * N = 5: P = (1-t)^4*P0 + 4*(1-t)^3*t*P1 + 6(1-t)^2*t^2*P2 + 4*(1-t)*t^3*P3 + t^4*P4 <br>
     * <p>
     * <p>
     * 2.
     * 从上面三个公式可以找出如下共同点： <br>
     * N=3 时公式里有 2 个加号，连接三个表达式求和；N=4 时有 3 个加号，四个表达式；N=5 时有 4 个加号，五个表达式， <br>
     * 设有常数 a,b 和 c，则这些表达式可统一表示为如下形式： <br>
     * a * (1 - t)^b * t^c * Pn; <br>
     * <p>
     * <p>
     * 3.
     * 分析当 N 分别为 3,4,5 时对应 a,b,c 的值： <br>
     * 如 N = 3 时，公式有三个表达式，第一个表达式为 (1-t)^2*P0，其对应 a,b,c 值分别为：1,2,0 <br>
     * N = 3: 1,2,0  2,1,1  1,0,2 <br>
     * a: 1 2 1 <br>
     * b: 2 1 0 <br>
     * c: 0 1 2 <br>
     * <p>
     * N = 4: 1,3,0  3,2,1  3,1,2  1,0,3 <br>
     * a: 1 3 3 1 <br>
     * b: 3 2 1 0 <br>
     * c: 0 1 2 3 <br>
     * <p>
     * N = 5: 1,4,0  4,3,1  6,2,2  4,1,3  1,0,4 <br>
     * a: 1 4 6 4 1 <br>
     * b: 4 3 2 1 0 <br>
     * c: 0 1 2 3 4 <br>
     * <p>
     * <p>
     * 4.
     * 根据第 3 的分析就可以总结出 a,b,c 对应的取值规则： <br>
     * b: (N - 1) 递减到 0     (b 为 1-t 的幂) <br>
     * c: 0 递增到 (N - 1)     (c 为 t 的幂) <br>
     * a: 在 N 分别为 1,2，3,4,5 时将其值用如下形式表示： <br>
     * N=1:---------1 <br>
     * N=2:--------1  1 <br>
     * N=3:------1  2  1 <br>
     * N=4:----1  3  3  1 <br>
     * N=5:--1  4  6  4  1 <br>
     * a 值的改变规则为：杨辉三角 <br>
     *
     * @param poss      贝塞尔曲线控制点坐标
     * @param precision 精度，该条贝塞尔曲线上的点的数目
     * @return 该条贝塞尔曲线上的点
     */
    @Override
    public float[][] calculate(float[][] poss, int precision) {

        //维度，坐标轴数（二维坐标，三维坐标...）
        int dimersion = poss[0].length;

        //贝塞尔曲线控制点数（阶数）
        int number = poss.length;

        //控制点数不小于 2 ，至少为二维坐标系
        if (number < 2 || dimersion < 2)
            return null;

        float[][] result = new float[precision][dimersion];

        //计算杨辉三角
        int[] mi = new int[number];
        mi[0] = mi[1] = 1;
        for (int i = 3; i <= number; i++) {

            int[] t = new int[i - 1];
            for (int j = 0; j < t.length; j++) {
                t[j] = mi[j];
            }

            mi[0] = mi[i - 1] = 1;
            for (int j = 0; j < i - 2; j++) {
                mi[j + 1] = t[j] + t[j + 1];
            }
        }

        //计算坐标点
        for (int i = 0; i < precision; i++) {
            float t = (float) i / precision;
            for (int j = 0; j < dimersion; j++) {
                float temp = 0.0f;
                for (int k = 0; k < number; k++) {
                    temp += Math.pow(1 - t, number - k - 1) * poss[k][j] * Math.pow(t, k) * mi[k];
                }
                result[i][j] = temp;
            }
        }

        return result;
    }

}
