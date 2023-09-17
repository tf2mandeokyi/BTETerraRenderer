package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitCube;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.MatrixMajor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Data
public class Box extends Volume {
    // Read these from array in order
    public final Cartesian3 center;
    public final Matrix3 halfLength;
    @ToString.Exclude
    public transient final Matrix4 boxMatrix;

    public Box(Cartesian3 center, Matrix3 halfLength) {
        this.center = center;
        this.halfLength = halfLength;

        Matrix centerMatrix = center.toTransformableMatrix();
        this.boxMatrix = new Matrix4((c, r) -> {
            if(c == 3) return centerMatrix.get(0, r);
            else if(r != 3) return halfLength.get(c, r);
            else return 0;
        });
    }

    @Override
    public boolean intersectsSphere(Sphere sphere, Matrix4 thisTransform) {
        Matrix actualBoxMatrix = thisTransform.multiply(this.boxMatrix);
        Matrix inverseSphereMatrix = sphere.getSphereMatrix().inverse();

        // "Unit-alize" the sphere
        Matrix4 transformedBoxMatrix = inverseSphereMatrix.multiply(actualBoxMatrix).toMatrix4();
        return UnitSphere.checkParallelepipedIntersection(transformedBoxMatrix);
    }

    @Override
    public boolean intersectsRay(Cartesian3 rayStart, Cartesian3 rayEnd, Matrix4 thisTransform) {
        Matrix actualBoxMatrix = thisTransform.multiply(this.boxMatrix);
        Matrix4 inverse = actualBoxMatrix.inverse().toMatrix4();

        // "Unit-alize" the box
        Cartesian3 unitRayStart = rayStart.transform(inverse);
        Cartesian3 unitRayEnd = rayEnd.transform(inverse);
        return UnitCube.checkRayIntersection(unitRayStart, unitRayEnd);
    }

    public static Box fromArray(double[] array) {
        return new Box(Cartesian3.fromArray(array, 0), Matrix3.fromArray(array, 3, MatrixMajor.COLUMN));
    }
}
