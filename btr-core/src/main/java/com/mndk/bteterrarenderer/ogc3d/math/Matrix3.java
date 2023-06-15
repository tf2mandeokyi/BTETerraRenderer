package com.mndk.bteterrarenderer.ogc3d.math;

import java.util.Arrays;

public class Matrix3 extends MatrixN {
    public static final Matrix3 IDENTITY = new Matrix3((r, c) -> r == c ? 1 : 0);

    public Matrix3(ElementFunction elementFunction) {
        super(3, elementFunction);
    }

    public static Matrix3 fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix3((r, c) -> array[r*3+c]);
        } else {
            return new Matrix3((r, c) -> array[c*3+r]);
        }
    }

    public static Matrix3 fromArray(double[] array, int start, MatrixMajor matrixMajor) {
        return fromArray(Arrays.copyOfRange(array, start, start+9), matrixMajor);
    }
}
