package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import java.util.Arrays;

public class Matrix3 extends Matrix {
    public static final Matrix3 IDENTITY = new Matrix3(IDENTITY_FUNCTION);

    public Matrix3(ColumnRowFunction columnRowFunction) {
        super(3, 3, columnRowFunction);
    }

    public static Matrix3 fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix3((c, r) -> array[r*3+c]);
        } else {
            return new Matrix3((c, r) -> array[c*3+r]);
        }
    }

    public static Matrix3 fromArray(double[] array, int start, MatrixMajor matrixMajor) {
        return fromArray(Arrays.copyOfRange(array, start, start+9), matrixMajor);
    }
}
