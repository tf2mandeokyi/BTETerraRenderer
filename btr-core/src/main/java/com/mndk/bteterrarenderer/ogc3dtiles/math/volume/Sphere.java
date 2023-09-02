package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.RayMath;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
public class Sphere extends Volume {
    private final Cartesian3 center;
    private final double radius;
    @ToString.Exclude
    private transient final Matrix4 sphereMatrix;

    public Sphere(Cartesian3 center, double radius) {
        this.center = center;
        this.radius = radius;

        Matrix centerMatrix = center.getTransformableMatrix();
        this.sphereMatrix = new Matrix4((c, r) -> {
            if(c == 3) return centerMatrix.get(0, r);
            else if(r != 3) return c == r ? radius : 0;
            else return 0;
        });
    }

    @Override
    public boolean containsCartesian(Cartesian3 cartesian, Matrix4 transform) {
        Matrix actualSphereMatrix = transform.multiply(this.sphereMatrix);
        Matrix4 inverse = actualSphereMatrix.inverse().toMatrix4();

        Cartesian3 unitCartesian = cartesian.transform(inverse);
        double x = unitCartesian.getX();
        double y = unitCartesian.getY();
        double z = unitCartesian.getZ();
        return x*x + y*y + z*z <= 1;
    }

    @Override
    public boolean intersectsRay(Cartesian3 rayStart, Cartesian3 rayEnd, Matrix4 transform) {
        Matrix actualSphereMatrix = transform.multiply(this.sphereMatrix);
        Matrix4 inverse = actualSphereMatrix.inverse().toMatrix4();

        Cartesian3 unitRayStart = rayStart.transform(inverse);
        Cartesian3 unitRayEnd = rayEnd.transform(inverse);
        return RayMath.checkUnitSphereIntersection(unitRayStart, unitRayEnd);
    }

    public static Sphere fromArray(double[] array) {
        return new Sphere(Cartesian3.fromArray(array, 0), array[3]);
    }
}
