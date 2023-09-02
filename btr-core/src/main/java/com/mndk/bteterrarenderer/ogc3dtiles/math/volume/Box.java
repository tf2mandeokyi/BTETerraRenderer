package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.RayMath;
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
    private final Cartesian3 center;
    private final Matrix3 halfLength;
    @ToString.Exclude
    private transient final Matrix4 boxMatrix;

    public Box(Cartesian3 center, Matrix3 halfLength) {
        this.center = center;
        this.halfLength = halfLength;

        Matrix centerMatrix = center.getTransformableMatrix();
        this.boxMatrix = new Matrix4((c, r) -> {
            if(c == 3) return centerMatrix.get(0, r);
            else if(r != 3) return halfLength.get(c, r);
            else return 0;
        });
    }

    @Override
    public boolean containsCartesian(Cartesian3 cartesian, Matrix4 transform) {
        Matrix actualBoxMatrix = transform.multiply(this.boxMatrix);
        Matrix4 inverse = actualBoxMatrix.inverse().toMatrix4();

        Cartesian3 unitCartesian = cartesian.transform(inverse);
        double x = unitCartesian.getX();
        double y = unitCartesian.getY();
        double z = unitCartesian.getZ();
        return -1 <= x && x <= 1 &&
                -1 <= y && y <= 1 &&
                -1 <= z && z <= 1;
    }

    @Override
    public boolean intersectsRay(Cartesian3 rayStart, Cartesian3 rayEnd, Matrix4 transform) {
        Matrix actualBoxMatrix = transform.multiply(this.boxMatrix);
        Matrix4 inverse = actualBoxMatrix.inverse().toMatrix4();

        Cartesian3 unitRayStart = rayStart.transform(inverse);
        Cartesian3 unitRayEnd = rayEnd.transform(inverse);
        return RayMath.checkUnitCubeIntersection(unitRayStart, unitRayEnd);
    }

    public static Box fromArray(double[] array) {
        return new Box(Cartesian3.fromArray(array, 0), Matrix3.fromArray(array, 3, MatrixMajor.COLUMN));
    }
}
