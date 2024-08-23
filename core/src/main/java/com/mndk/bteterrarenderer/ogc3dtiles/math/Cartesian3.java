package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Arrays;

@Data
@RequiredArgsConstructor
@JsonDeserialize(using = Cartesian3.Deserializer.class)
public class Cartesian3 {
    public static final Cartesian3 ORIGIN = new Cartesian3(0, 0, 0);
    public static final Cartesian3 UNIT_AXES = new Cartesian3(1, 1, 1);
    public static final Cartesian3 UNIT_X = new Cartesian3(1, 0, 0);
    public static final Cartesian3 UNIT_Y = new Cartesian3(0, 1, 0);
    public static final Cartesian3 UNIT_Z = new Cartesian3(0, 0, 1);

    private static final int LATITUDE_APPROX_ITERATION = 5;

    // TODO: Consider changing these to float and add a center coordinate
    private final double x, y, z;

    public Cartesian3(float[] array) { this(array[0], array[1], array[2]); }
    public Cartesian3(double[] array) { this(array[0], array[1], array[2]); }

    /**
     * @link <a href="https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion">
     *     Ellipsoidal and Cartesian Coordinates Conversion</a>
     * @return The Earth's spheroidal coordinate corresponding to the cartesian coordinate
     */
    public Spheroid3 toSpheroidalCoordinate() {
        double longitude = Math.atan2(y, x);
        double p = Math.sqrt(x*x + y*y);

        double latitude = Math.atan2(z, (1 - Wgs84Constants.ECCENTRICITY2) * p);
        double height = 0;
        for (int i = 0; i < LATITUDE_APPROX_ITERATION; i++) {
            double N = SpheroidalMath.getEarthCurvatureRadius(latitude);
            height = p / Math.cos(latitude) - N;
            latitude = Math.atan2(z, (1 - Wgs84Constants.ECCENTRICITY2 * N / (N + height)) * p);
        }

        return new Spheroid3(longitude, latitude, height);
    }

    /**
     * Returns a transformable 1x3 matrix of itself: {@code [x, y, z]^T}<br>
     * @return The matrix
     */
    public Matrix toMatrix() {
        double[] temp = { x, y, z };
        return new Matrix(1, 3, (c, r) -> temp[r]);
    }

    /**
     * Returns a transformable 1x4 matrix of itself: {@code [x, y, z, 1]^T}<br>
     * Used for {@link Cartesian3#transform(Matrix4)}
     * @return The matrix
     */
    public Matrix toTransformableMatrix() {
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
        Matrix multiplied = transformMatrix.multiply(this.toTransformableMatrix());
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
     * Returns a scaled version of itself
     * @param other The scaling factor
     * @return The result
     */
    public Cartesian3 scale(Cartesian3 other) {
        return new Cartesian3(x*other.x, y*other.y, z*other.z);
    }

    /**
     * Returns a normalized version of itself. The length of it is 1
     * @return The result
     */
    public Cartesian3 toNormalized() {
        return this.scale(1 / this.distance());
    }

	/**
	 * Returns the xy distance from the origin
	 * @return The distance
	 */
	public double xyDistance() {
		return Math.sqrt(x*x + y*y);
	}

    /**
     * Returns the distance from the origin
     * @return The distance
     */
    public double distance() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    /**
     * Returns the distance from the origin squared
     * @return The distance
     */
    public double distance2() {
        return x*x + y*y + z*z;
    }

    /**
     * Performs a dot product and returns its result.
     * @param other The other coordinate
     * @return The result
     */
    public double dot(Cartesian3 other) {
        return x*other.x + y*other.y + z*other.z;
    }

    /**
     * Performs a cross product {@code A x B} and returns its result, where {@code A} being itself and
     * {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3 cross(Cartesian3 other) {
        return new Cartesian3(y*other.z - z*other.y, z*other.x - x*other.z, x*other.y - y*other.x);
    }

    @Override
    public String toString() {
        return "Cartesian3[" + x + ", " + y + ", " + z + "]";
    }

    public static Cartesian3 fromArray(double[] array) {
        return new Cartesian3(array[0], array[1], array[2]);
    }

    public static Cartesian3 fromArray(float[] array) {
        return new Cartesian3(array[0], array[1], array[2]);
    }

    public static Cartesian3 fromArray(Float[] array) {
        return new Cartesian3(array[0], array[1], array[2]);
    }

    public static Cartesian3 fromArray(double[] array, int start) {
        return fromArray(Arrays.copyOfRange(array, start, start+3));
    }

    private static int signNotZero(float value) {
        return value >= 0.0 ? 1 : -1;
    }

    public static Cartesian3 fromOctEncoding(float x, float y) {
        float x3 = x, y3 = y, z3 = 1 - (Math.abs(x) + Math.abs(y));
        if(z3 < 0) {
            float oldX3 = x3;
            x3 = (1.0f - Math.abs(y3)) * signNotZero(oldX3);
            y3 = (1.0f - Math.abs(oldX3)) * signNotZero(y3);
        }
        return new Cartesian3(x3, y3, z3).toNormalized();
    }

    static class Deserializer extends JsonDeserializer<Cartesian3> {
        @Override
        public Cartesian3 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p);
            return Cartesian3.fromArray(array);
        }
    }
}
