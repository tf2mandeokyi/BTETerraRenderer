package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Plane;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.MatrixMajor;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrixf;
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
    public boolean intersectsPositiveSides(Plane[] planes, Matrix4f thisTransform,
                                           SpheroidCoordinatesConverter converter) {
        Matrix4f actualBoxMatrix = thisTransform.multiply(this.boxMatrix);
        Cartesian3f[] vertices = {
                new Cartesian3f(+1, +1, +1).transform(actualBoxMatrix),
                new Cartesian3f(+1, +1, -1).transform(actualBoxMatrix),
                new Cartesian3f(+1, -1, +1).transform(actualBoxMatrix),
                new Cartesian3f(+1, -1, -1).transform(actualBoxMatrix),
                new Cartesian3f(-1, +1, +1).transform(actualBoxMatrix),
                new Cartesian3f(-1, +1, -1).transform(actualBoxMatrix),
                new Cartesian3f(-1, -1, +1).transform(actualBoxMatrix),
                new Cartesian3f(-1, -1, -1).transform(actualBoxMatrix)
        };
        for (Plane plane : planes) {
            // Condition: At least one vertex is on the positive side of the plane
            boolean atLeastOneVertexOnPositiveSide = false;
            for (int i = 0; i < 8; ++i) {
                // Positive side: n * (p - p0) >= 0
                boolean result = plane.getNormal().dot(vertices[i].subtract(plane.getPoint())) >= 0;
                if (!result) continue;
                atLeastOneVertexOnPositiveSide = true;
                break;
            }
            if (!atLeastOneVertexOnPositiveSide) return false;
        }
        return true;
    }

    public static Box fromArray(double[] array) {
        return new Box(Cartesian3f.fromArray(array), Matrix3f.fromArray(array, 3, MatrixMajor.COLUMN));
    }
}
