package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrixf;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Data
@RequiredArgsConstructor
@JsonDeserialize(using = Cartesian3f.Deserializer.class)
public class Cartesian3f {
    public static final Cartesian3f ORIGIN = new Cartesian3f(0, 0, 0);
    public static final Cartesian3f UNIT = new Cartesian3f(1, 1, 1);

    private static final int LATITUDE_APPROX_ITERATION = 5;

    private final float x, y, z;

    public Cartesian3f(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public float[] toArray() {
        return new float[] { x, y, z };
    }

    /**
     * Returns a transformable 1x3 matrix of itself: {@code [x, y, z]^T}<br>
     * @return The matrix
     */
    public Matrixf toMatrix() {
        float[] temp = { x, y, z };
        return new Matrixf(1, 3, (c, r) -> temp[r]);
    }

    /**
     * Returns a transformable 1x4 matrix of itself: {@code [x, y, z, 1]^T}<br>
     * Used for {@link Cartesian3f#transform(Matrix4f)}
     * @return The matrix
     */
    public Matrixf toTransformableMatrix() {
        float[] temp = { x, y, z, 1 };
        return new Matrixf(1, 4, (c, r) -> temp[r]);
    }

    /**
     * Returns a transformed result of itself from the multiplication result of {@code Av},
     * where {@code A} being the transform matrix and {@code v} being the cartesian coordinate itself
     * @param transformMatrix The transform matrix
     * @return The result
     */
    public Cartesian3f transform(Matrix4f transformMatrix) {
        Matrixf multiplied = transformMatrix.multiply(this.toTransformableMatrix());
        return new Cartesian3f(multiplied.get(0, 0), multiplied.get(0, 1), multiplied.get(0, 2));
    }

    /**
     * Returns {@code A+B}, where {@code A} being itself and {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3f add(Cartesian3f other) {
        return new Cartesian3f(x+other.x, y+other.y, z+other.z);
    }

    /**
     * Returns {@code A-B}, where {@code A} being itself and {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3f subtract(Cartesian3f other) {
        return new Cartesian3f(x-other.x, y-other.y, z-other.z);
    }

    /**
     * Returns a scaled version of itself
     * @param magnitude The scaling factor
     * @return The result
     */
    public Cartesian3f scale(float magnitude) {
        return new Cartesian3f(x*magnitude, y*magnitude, z*magnitude);
    }

    /**
     * Returns a scaled version of itself
     * @param other The scaling factor
     * @return The result
     */
    public Cartesian3f scale(Cartesian3f other) {
        return new Cartesian3f(x*other.x, y*other.y, z*other.z);
    }

    /**
     * Returns a normalized version of itself. The length of it is 1
     * @return The result
     */
    public Cartesian3f toNormalized() {
        return this.scale(1 / this.distance());
    }

    /**
     * Returns the distance from the origin
     * @return The distance
     */
    public float distance() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    /**
     * Performs a dot product and returns its result.
     * @param other The other coordinate
     * @return The result
     */
    public float dot(Cartesian3f other) {
        return x*other.x + y*other.y + z*other.z;
    }

    /**
     * Performs a cross product {@code A x B} and returns its result, where {@code A} being itself and
     * {@code B} being the other coordinate
     * @param other The other coordinate
     * @return The result
     */
    public Cartesian3f cross(Cartesian3f other) {
        return new Cartesian3f(y*other.z - z*other.y, z*other.x - x*other.z, x*other.y - y*other.x);
    }

    @Override
    public String toString() {
        return "Cartesian3[" + x + ", " + y + ", " + z + "]";
    }

    public static Cartesian3f fromArray(double[] array) {
        return new Cartesian3f((float) array[0], (float) array[1], (float) array[2]);
    }

    public static Cartesian3f fromArray(float[] array) {
        return new Cartesian3f(array[0], array[1], array[2]);
    }

    public static Cartesian3f fromArray(Float[] array) {
        return new Cartesian3f(array[0], array[1], array[2]);
    }

    private static int signNotZero(float value) {
        return value >= 0.0 ? 1 : -1;
    }

    public static Cartesian3f fromOctEncoding(float x, float y) {
        float x3 = x, y3 = y, z3 = 1 - (Math.abs(x) + Math.abs(y));
        if (z3 < 0) {
            float oldX3 = x3;
            x3 = (1.0f - Math.abs(y3)) * signNotZero(oldX3);
            y3 = (1.0f - Math.abs(oldX3)) * signNotZero(y3);
        }
        return new Cartesian3f(x3, y3, z3).toNormalized();
    }

    public static Cartesian3f min(Cartesian3f a, Cartesian3f b) {
        return new Cartesian3f(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
    }
    public static Cartesian3f max(Cartesian3f a, Cartesian3f b) {
        return new Cartesian3f(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
    }

    static class Deserializer extends JsonDeserializer<Cartesian3f> {
        @Override
        public Cartesian3f deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            double[] array = JsonParserUtil.readDoubleArray(p);
            return Cartesian3f.fromArray(array);
        }
    }
}
