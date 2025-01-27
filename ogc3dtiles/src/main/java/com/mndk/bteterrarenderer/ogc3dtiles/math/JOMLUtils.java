package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.experimental.UtilityClass;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

@UtilityClass
public class JOMLUtils {

    /**
     * Convert an array of 3 values to a JOML {@link Vector3d} object
     * @param data The vector data
     * @return A new instance of {@link Vector3d}
     */
    public Vector3d vector3d(Float[] data) {
        return new Vector3d(data[0], data[1], data[2]);
    }

    /**
     * Return the sign of the value, or 1 if the value is 0
     * @param value The value
     * @return The sign of the value
     */
    private static int signNotZero(float value) {
        return value >= 0.0 ? 1 : -1;
    }

    /**
     * Convert an oct-encoded pair of values to a JOML {@link Vector3d} object
     * @param x The x value
     * @param y The y value
     * @return A new instance of {@link Vector3d}
     */
    public static Vector3d fromOctEncoding(float x, float y) {
        float x3 = x, y3 = y, z3 = 1 - (Math.abs(x) + Math.abs(y));
        if (z3 < 0) {
            float oldX3 = x3;
            x3 = (1.0f - Math.abs(y3)) * signNotZero(oldX3);
            y3 = (1.0f - Math.abs(oldX3)) * signNotZero(y3);
        }
        return new Vector3d(x3, y3, z3).normalize();
    }

    /**
     * Convert an array of 4 values to a JOML {@link Quaterniond} object in XYZW order
     * @param data The quaternion data
     * @return A new instance of {@link Quaterniond}
     */
    public Quaterniond quaternionXYZW(float[] data) {
        return new Quaterniond(data[0], data[1], data[2], data[3]);
    }

    /**
     * Convert a column-major 4x4 matrix to a JOML {@link Matrix4d} object
     * @param data The column-major matrix data
     * @return A new instance of {@link Matrix4d}
     */
    public Matrix4d columnMajor4d(double[] data) {
        return new Matrix4d(
                data[0], data[1], data[2], data[3],
                data[4], data[5], data[6], data[7],
                data[8], data[9], data[10], data[11],
                data[12], data[13], data[14], data[15]
        );
    }

    /**
     * Convert a column-major 4x4 matrix to a JOML {@link Matrix4d} object
     * @param data The column-major matrix data
     * @return A new instance of {@link Matrix4d}
     */
    public Matrix4d columnMajor4d(float[] data) {
        return new Matrix4d(
                data[0], data[1], data[2], data[3],
                data[4], data[5], data[6], data[7],
                data[8], data[9], data[10], data[11],
                data[12], data[13], data[14], data[15]
        );
    }

}
