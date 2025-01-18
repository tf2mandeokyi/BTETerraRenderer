package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Plane;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector3d;

@EqualsAndHashCode(callSuper = true)
@Data
public class Ellipsoid extends Volume {
    // [ sx sx sx c ]
    // [ sy sy sy c ]
    // [ sz sz sz c ]
    // [  0  0  0 1 ]
    private transient final Matrix4d matrix;

    public Ellipsoid(Vector3d center, double radius) {
        this.matrix = new Matrix4d().scale(radius).translate(center);
    }

    /**
     * Get the center of the ellipsoid
     * @return A new instance of {@link Vector3d} representing the center of the ellipsoid
     */
    public Vector3d getCenter() {
        return this.matrix.getColumn(3, new Vector3d());
    }

    /**
     * Get the radius matrix of the ellipsoid
     * @return A new instance of {@link Matrix3d} representing the radius matrix of the ellipsoid
     */
    public Matrix3d getRadiusMatrix() {
        return this.matrix.get3x3(new Matrix3d());
    }

    @Override
    public boolean intersectsPositiveSides(Plane[] planes, Matrix4d thisTransform,
                                           SpheroidCoordinatesConverter converter) {
        Matrix4d actualSphereMatrix = thisTransform.mul(this.matrix, new Matrix4d());
        Vector3d s1 = actualSphereMatrix.getRow(0, new Vector3d());
        Vector3d s2 = actualSphereMatrix.getRow(1, new Vector3d());
        Vector3d s3 = actualSphereMatrix.getRow(2, new Vector3d());
        Vector3d center = actualSphereMatrix.getColumn(3, new Vector3d());

        for (Plane plane : planes) {
            double d1 = s1.dot(plane.getNormal());
            double d2 = s2.dot(plane.getNormal());
            double d3 = s3.dot(plane.getNormal());
            double r = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            double s = plane.getNormal().dot(center.sub(plane.getPoint()));
            if (s + r < 0) return false;
        }
        return true;
    }

    @Override
    public BoundingSphere getLevelOfDetailSphere(Matrix4d thisTransform, SpheroidCoordinatesConverter converter) {
        Matrix4d actualSphereMatrix = thisTransform.mul(this.matrix, new Matrix4d());
        Vector3d s1 = actualSphereMatrix.getRow(0, new Vector3d());
        Vector3d s2 = actualSphereMatrix.getRow(1, new Vector3d());
        Vector3d s3 = actualSphereMatrix.getRow(2, new Vector3d());
        Vector3d center = actualSphereMatrix.getColumn(3, new Vector3d());

        double maxScale = Math.max(s1.length(), Math.max(s2.length(), s3.length()));
        return new BoundingSphere(center, maxScale);
    }

    public static Ellipsoid fromArray(double[] array) {
        return new Ellipsoid(new Vector3d(array), (float) array[3]);
    }
}
