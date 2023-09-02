package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;

import java.util.Arrays;

@Data
public class Cartesian3 {
    private static final int LATITUDE_APPROX_ITERATION = 4;

    private final double x, y, z;

    /**
     * @link <a href="https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion">
     *     Ellipsoidal and Cartesian Coordinates Conversion</a>
     * @return The Earth's ellipsoidal coordinate corresponding to the cartesian coordinate
     */
    public Ellipsoidal3 toEllipsoidalCoordinate() {
        double longitude = Math.atan2(y, x);
        double p = Math.sqrt(x*x + y*y);

        double latitude = Math.atan2(z, (1 - Wgs84Constants.ECCENTRICITY2) * p);
        double height = 0;
        for (int i = 0; i < LATITUDE_APPROX_ITERATION; i++) {
            double N = EllipsoidalMath.getEarthCurvatureRadius(latitude);
            height = p / Math.cos(latitude) - N;
            latitude = Math.atan2(z, (1 - Wgs84Constants.ECCENTRICITY2 * N / (N + height)) * p);
        }

        return new Ellipsoidal3(longitude, latitude, height);
    }

    /**
     * Returns a transformable 1x4 matrix of itself: {@code [x, y, z, 1]^T}<br>
     * Used for {@link Cartesian3#transform(Matrix4)}
     * @return The matrix
     */
    public Matrix getTransformableMatrix() {
        double[] temp = { x, y, z, 1 };
        return new Matrix(1, 4, (c, r) -> temp[r]);
    }

    /**
     * Returns a transformed result of itself from the multiplication result of {@code Av},
     * where {@code A} being the transform matrix and {@code v} being the cartesian coordinate itself
     * @param transformMatrix The transform matrix
     * @return The result
     */
    public Cartesian3 transform(Matrix4 transformMatrix) {
        Matrix multiplied = transformMatrix.multiply(this.getTransformableMatrix());
        return new Cartesian3(multiplied.get(0, 0), multiplied.get(0, 1), multiplied.get(0, 2));
    }

    /**
     * Returns {@code A+B}, where {@code A} being itself and {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3 add(Cartesian3 other) {
        return new Cartesian3(x+other.x, y+other.y, z+other.z);
    }

    /**
     * Returns {@code A-B}, where {@code A} being itself and {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3 subtract(Cartesian3 other) {
        return new Cartesian3(x-other.x, y-other.y, z-other.z);
    }

    /**
     * Returns a scaled version of itself
     * @param magnitude The scaling factor
     * @return The result
     */
    public Cartesian3 scale(double magnitude) {
        return new Cartesian3(x*magnitude, y*magnitude, z*magnitude);
    }

    /**
     * Performs a dot product and returns its result.
     * @param other The other coordinate
     * @return The result
     */
    public double dot(Cartesian3 other) {
        return x*other.x + y*other.y + z*other.z;
    }

    public static Cartesian3 fromArray(double[] array) {
        return new Cartesian3(array[0], array[1], array[2]);
    }

    public static Cartesian3 fromArray(double[] array, int start) {
        return fromArray(Arrays.copyOfRange(array, start, start+3));
    }

}
