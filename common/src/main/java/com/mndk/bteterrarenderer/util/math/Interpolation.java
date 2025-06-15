package com.mndk.bteterrarenderer.util.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Interpolation {
    public double linear(double t0, double t1, double t) {
        return t0 + (t1 - t0) * t;
    }

    public double[] linear(int[] t0, int[] t1, double t) {
        return new double[] {
                linear(t0[0], t1[0], t),
                linear(t0[1], t1[1], t)
        };
    }

    public double[] bilinear(int[] lt, int[] rt, int[] lb, int[] rb, double fx, double fy) {
        double[] t0 = linear(lt, rt, fx);
        double[] t1 = linear(lb, rb, fx);
        return new double[] {
                linear(t0[0], t1[0], fy),
                linear(t0[1], t1[1], fy)
        };
    }

    public static float cubic(float p0, float p1, float p2, float p3, float x) {
        return p1 + 0.5f * x * (p2 - p0 + x * (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3 + x * (3.0f * (p1 - p2) + p3 - p0)));
    }
}
