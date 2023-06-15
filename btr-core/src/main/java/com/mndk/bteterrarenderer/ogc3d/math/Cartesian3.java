package com.mndk.bteterrarenderer.ogc3d.math;

import lombok.Data;

import java.util.Arrays;

@Data
public class Cartesian3 {
    private final double x, y, z;

    public static Cartesian3 fromArray(double[] array) {
        return new Cartesian3(array[0], array[1], array[2]);
    }

    public static Cartesian3 fromArray(double[] array, int start) {
        return fromArray(Arrays.copyOfRange(array, start, start+3));
    }
}
