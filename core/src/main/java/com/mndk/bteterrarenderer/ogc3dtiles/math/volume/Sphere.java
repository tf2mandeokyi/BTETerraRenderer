package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

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
public class Sphere extends Volume {
    final Cartesian3f center;
    final float radius;
    @ToString.Exclude
    private transient final Matrix4f sphereMatrix;

    public Sphere(Cartesian3f center, float radius) {
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
        Cartesian3f sx = new Cartesian3f(actualSphereMatrix.get(0, 0), actualSphereMatrix.get(0, 1), actualSphereMatrix.get(0, 2));
        Cartesian3f sy = new Cartesian3f(actualSphereMatrix.get(1, 0), actualSphereMatrix.get(1, 1), actualSphereMatrix.get(1, 2));
        Cartesian3f sz = new Cartesian3f(actualSphereMatrix.get(2, 0), actualSphereMatrix.get(2, 1), actualSphereMatrix.get(2, 2));
        Cartesian3f c = new Cartesian3f(actualSphereMatrix.get(0, 3), actualSphereMatrix.get(1, 3), actualSphereMatrix.get(2, 3));

        for (Plane plane : planes) {
            float dx = sx.dot(plane.getNormal());
            float dy = sy.dot(plane.getNormal());
            float dz = sz.dot(plane.getNormal());
            float r = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            float s = plane.getNormal().dot(c.subtract(plane.getPoint()));
            if (s + r < 0) return false;
        }
        return true;
    }

    public static Sphere fromArray(double[] array) {
        return new Sphere(Cartesian3f.fromArray(array), (float) array[3]);
    }
}
