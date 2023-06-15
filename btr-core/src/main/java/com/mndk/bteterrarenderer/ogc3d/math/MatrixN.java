package com.mndk.bteterrarenderer.ogc3d.math;

import lombok.EqualsAndHashCode;

import java.util.Arrays;

@EqualsAndHashCode
public abstract class MatrixN {
    protected final double[][] elements;

    public MatrixN(int size, ElementFunction elementFunction) {
        this.elements = new double[size][size];
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                elements[r][c] = elementFunction.apply(r, c);
            }
        }
    }

    public double get(int r, int c) {
        return elements[r][c];
    }

    @Override
    public String toString() {
        return Arrays.deepToString(elements);
    }

    public interface ElementFunction {
        double apply(int row, int column);
    }
}
