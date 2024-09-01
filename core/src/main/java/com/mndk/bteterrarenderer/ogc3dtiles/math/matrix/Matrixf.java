package com.mndk.bteterrarenderer.ogc3dtiles.math.matrix;

import lombok.EqualsAndHashCode;

import java.util.Arrays;

@EqualsAndHashCode
public class Matrixf {
    public static final ColumnRowFunction IDENTITY_FUNCTION = (c, r) -> r == c ? 1 : 0;

    protected final int rows, columns;
    protected final float[][] elements;

    public Matrixf(int columns, int rows, ColumnRowFunction columnRowFunction) {
        this.columns = columns;
        this.rows = rows;

        this.elements = new float[rows][columns];
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < columns; c++) {
                elements[r][c] = columnRowFunction.apply(c, r);
            }
        }
    }

    public float get(int c, int r) {
        return elements[r][c];
    }

    /**
     * Returns a matrix multiplication result of {@code AB},
     * where {@code A} being itself, and {@code B} being the other matrix
     *
     * @param other The other matrix
     * @return The result. {@code null} if {@code A.columns != B.rows}
     */
    public Matrixf multiply(Matrixf other) {
        if(columns != other.rows) return null;

        float[][] result = new float[rows][other.columns];
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < other.columns; c++) {
                float sum = 0;
                for(int i = 0; i < columns; i++) {
                    sum += get(i, r) * other.get(c, i);
                }
                result[r][c] = sum;
            }
        }

        return new Matrixf(other.columns, rows, (c, r) -> result[r][c]);
    }

    /**
     * Returns an inverse version of itself, by using the Gauss-Jordan method.<br>
     * Calling this method is computationally expensive, so try not to call it too much
     * @return The inverse matrix. {@code null} if it's not invertible
     */
    public Matrixf inverse() {
        if(rows != columns) return null;
        int size = rows;

        float[][] augumented = new float[size][2 * size];
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                // A side
                augumented[r][c] = get(c, r);
                // I side
                augumented[r][size+c] = r == c ? 1 : 0;
            }
        }

        for(int i = 0; i < size; i++) {
            // Selecting pivot a row
            int pivot;
            for(pivot = i; pivot < size; pivot++) {
                if(augumented[pivot][i] != 0) break;
            }

            // Return null if there's no row for a pivot, therefore not invertible
            if(pivot == size) return null;

            // Swap rows if the pivot is not in the i-th row
            if(pivot != i) {
                for(int c = i; c < 2 * size; c++) {
                    float temp = augumented[pivot][c];
                    augumented[pivot][c] = augumented[i][c];
                    augumented[i][c] = temp;
                }
            }

            // Divide the i-th row
            float divisor = augumented[i][i];
            for(int c = i; c < 2 * size; c++) {
                augumented[i][c] /= divisor;
            }

            // Multiply + subtract rows from the i-th row
            for(int r = 0; r < size; r++) {
                if(r == i) continue;
                float mult = augumented[r][i];
                if(mult == 0) continue;
                for(int c = i; c < 2 * size; c++) {
                    augumented[r][c] -= mult * augumented[i][c];
                }
            }
        }

        return new Matrixf(size, size, (c, r) -> augumented[r][size+c]);
    }

    public Matrix4f toMatrix4() {
        if(columns != 4 || rows != 4) throw new RuntimeException("this matrix cannot be Matrix4");
        return new Matrix4f((c, r) -> elements[r][c]);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(elements);
    }

    @SuppressWarnings("unused")
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < columns; c++) {
                sb.append(String.format("%11.4e ", get(c, r)));
            }
            if(r != rows - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
