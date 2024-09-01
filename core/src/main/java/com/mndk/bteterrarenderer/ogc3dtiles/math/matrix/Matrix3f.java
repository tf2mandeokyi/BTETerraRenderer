package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;

import java.util.Arrays;

public class Matrix3f extends Matrixf {
    public Matrix3f(ColumnRowFunction columnRowFunction) {
        super(3, 3, columnRowFunction);
    }

    public static Matrix3f fromCoordinates(Cartesian3f u0, Cartesian3f u1, Cartesian3f u2) {
        Matrixf[] temp = { u0.toMatrix(), u1.toMatrix(), u2.toMatrix() };
        return new Matrix3f((c, r) -> temp[c].get(0, r));
    }

    public static Matrix3f fromArray(double[] array, MatrixMajor matrixMajor) {
        if(matrixMajor == MatrixMajor.ROW) {
            return new Matrix3f((c, r) -> (float) array[r*3+c]);
        } else {
            return new Matrix3f((c, r) -> (float) array[c*3+r]);
        }
    }

    public static Matrix3f fromArray(double[] array, int start, MatrixMajor matrixMajor) {
        return fromArray(Arrays.copyOfRange(array, start, start+9), matrixMajor);
    }
}
