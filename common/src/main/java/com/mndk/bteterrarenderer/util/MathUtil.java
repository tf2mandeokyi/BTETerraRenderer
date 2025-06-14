package com.mndk.bteterrarenderer.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {

    public double lerp(double t0, double t1, double t) {
        return t0 + (t1 - t0) * t;
    }

    public double[] lerp(int[] t0, int[] t1, double t) {
        return new double[] {
                lerp(t0[0], t1[0], t),
                lerp(t0[1], t1[1], t)
        };
    }

    public double[] bilerp(int[] lt, int[] rt, int[] lb, int[] rb, double fx, double fy) {
        double[] t0 = lerp(lt, rt, fx);
        double[] t1 = lerp(lb, rb, fx);
        return new double[] {
                lerp(t0[0], t1[0], fy),
                lerp(t0[1], t1[1], fy)
        };
    }

}
