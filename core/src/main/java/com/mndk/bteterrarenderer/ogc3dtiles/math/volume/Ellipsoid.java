package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Plane;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrixf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
public class Ellipsoid extends Volume {
    final Cartesian3f center;
    final float radius;
    @ToString.Exclude
    private transient final Matrix4f sphereMatrix;

    public Ellipsoid(Cartesian3f center, float radius) {
        this.center = center;
        this.radius = radius;

        Matrixf centerMatrix = center.toTransformableMatrix();
        this.sphereMatrix = new Matrix4f((c, r) -> {
            if (c == 3) return centerMatrix.get(0, r);
            else if (r != 3) return c == r ? radius : 0;
            else return 0;
        });
    }

    @Override
    public boolean intersectsPositiveSides(Plane[] planes, Matrix4f thisTransform,
                                           SpheroidCoordinatesConverter converter) {
        Matrix4f actualSphereMatrix = thisTransform.multiply(this.sphereMatrix);
        Cartesian3f[] scales = actualSphereMatrix.getScaleRowVectors();
        Cartesian3f c = new Cartesian3f(actualSphereMatrix.get(0, 3), actualSphereMatrix.get(1, 3), actualSphereMatrix.get(2, 3));

        for (Plane plane : planes) {
            float dx = scales[0].dot(plane.getNormal());
            float dy = scales[1].dot(plane.getNormal());
            float dz = scales[2].dot(plane.getNormal());
            float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            float s = plane.getNormal().dot(c.subtract(plane.getPoint()));
            if (s + r < 0) return false;
        }
        return true;
    }

    @Override
    public BoundingSphere getLevelOfDetailSphere(Matrix4f thisTransform, SpheroidCoordinatesConverter converter) {
        if (thisTransform.equals(Matrix4f.IDENTITY)) {
            return new BoundingSphere(center, radius);
        }
        Matrix4f actualSphereMatrix = thisTransform.multiply(this.sphereMatrix);
        Cartesian3f[] scales = actualSphereMatrix.getScaleRowVectors();
        float maxScale = Math.max(scales[0].distance(), Math.max(scales[1].distance(), scales[2].distance()));
        return new BoundingSphere(center, maxScale);
    }

    public static Ellipsoid fromArray(double[] array) {
        return new Ellipsoid(Cartesian3f.fromArray(array), (float) array[3]);
    }
}
