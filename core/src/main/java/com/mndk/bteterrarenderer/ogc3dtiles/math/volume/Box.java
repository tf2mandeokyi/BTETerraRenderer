package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitCube;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrixf;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.MatrixMajor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Data
public class Box extends Volume {
    // Read these from array in order
    public final Cartesian3f center;
    public final Matrix3f halfLength;
    @ToString.Exclude
    public transient final Matrix4f boxMatrix;

    public Box(Cartesian3f center, Matrix3f halfLength) {
        this.center = center;
        this.halfLength = halfLength;

        Matrixf centerMatrix = center.toTransformableMatrix();
        this.boxMatrix = new Matrix4f((c, r) -> {
            if (c == 3) return centerMatrix.get(0, r);
            else if (r != 3) return halfLength.get(c, r);
            else return 0;
        });
    }

    @Override
    public boolean intersectsSphere(Sphere sphere, Matrix4f thisTransform) {
        Matrix4f actualBoxMatrix = thisTransform.multiply(this.boxMatrix);
        Matrix4f inverseSphereMatrix = sphere.getSphereMatrix().inverse();
        if (inverseSphereMatrix == null) return false;

        // "Unit-alize" the sphere
        Matrix4f transformedBoxMatrix = inverseSphereMatrix.multiply(actualBoxMatrix);
        return UnitSphere.checkParallelepipedIntersection(transformedBoxMatrix);
    }

    @Override
    public boolean intersectsRay(Cartesian3f rayStart, Cartesian3f rayEnd, Matrix4f thisTransform) {
        Matrix4f actualBoxMatrix = thisTransform.multiply(this.boxMatrix);
        Matrix4f inverse = actualBoxMatrix.inverse();
        if (inverse == null) return false;

        // "Unit-alize" the box
        Cartesian3f unitRayStart = rayStart.transform(inverse);
        Cartesian3f unitRayEnd = rayEnd.transform(inverse);
        return UnitCube.checkRayIntersection(unitRayStart, unitRayEnd);
    }

    public static Box fromArray(double[] array) {
        return new Box(Cartesian3f.fromArray(array), Matrix3f.fromArray(array, 3, MatrixMajor.COLUMN));
    }
}
