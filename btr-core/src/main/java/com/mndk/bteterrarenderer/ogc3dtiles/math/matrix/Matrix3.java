package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;

import java.util.Arrays;

public class Matrix3 extends Matrix {
    public Matrix3(ColumnRowFunction columnRowFunction) {
        super(3, 3, columnRowFunction);
    }

    public static Matrix3 fromCoordinates(Cartesian3 u0, Cartesian3 u1, Cartesian3 u2) {
        Matrix[] temp = { u0.toMatrix(), u1.toMatrix(), u2.toMatrix() };
        return new Matrix3((c, r) -> temp[c].get(0, r));
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
