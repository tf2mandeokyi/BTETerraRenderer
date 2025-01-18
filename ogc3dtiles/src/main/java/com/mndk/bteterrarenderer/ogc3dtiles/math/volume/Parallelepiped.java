package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.BoundingSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Plane;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joml.Matrix4d;
import org.joml.Vector3d;

@EqualsAndHashCode(callSuper = false)
@Data
public class Parallelepiped extends Volume {

    public transient final Matrix4d matrix;

    public Parallelepiped(Vector3d center, Vector3d s0, Vector3d s1, Vector3d s2) {
        this.matrix = new Matrix4d(
                s0.x, s0.y, s0.z, 0,
                s1.x, s1.y, s1.z, 0,
                s2.x, s2.y, s2.z, 0,
                center.x, center.y, center.z, 1
        );
    }

    public Vector3d getCenter() {
        return this.matrix.getColumn(3, new Vector3d());
    }

    @Override
    public boolean intersectsPositiveSides(Plane[] planes, Matrix4d thisTransform,
                                           SpheroidCoordinatesConverter converter) {
        Vector3d[] vertices = this.getVertices(thisTransform);
        for (Plane plane : planes) {
            // Condition: At least one vertex is on the positive side of the plane
            boolean atLeastOneVertexOnPositiveSide = false;
            for (int i = 0; i < 8; ++i) {
                // Positive side: n * (p - p0) >= 0
                Vector3d sub = vertices[i].sub(plane.getPoint(), new Vector3d());
                boolean result = plane.getNormal().dot(sub) >= 0;
                if (!result) continue;
                atLeastOneVertexOnPositiveSide = true;
                break;
            }
            if (!atLeastOneVertexOnPositiveSide) return false;
        }
        return true;
    }

    @Override
    public BoundingSphere getLevelOfDetailSphere(Matrix4d thisTransform, SpheroidCoordinatesConverter converter) {
        Vector3d[] vertices = this.getVertices(thisTransform);
        Vector3d center = this.getCenter();
        double maxDistance = 0;
        for (Vector3d vertex : vertices) {
            double distance = vertex.distance(center);
            maxDistance = Math.max(maxDistance, distance);
        }
        return new BoundingSphere(center, maxDistance);
    }

    private Vector3d[] getVertices(Matrix4d thisTransform) {
        Matrix4d actualBoxMatrix = thisTransform.mul(this.matrix, new Matrix4d());
        return new Vector3d[] {
                actualBoxMatrix.transformPosition(new Vector3d(+1, +1, +1)),
                actualBoxMatrix.transformPosition(new Vector3d(+1, +1, -1)),
                actualBoxMatrix.transformPosition(new Vector3d(+1, -1, +1)),
                actualBoxMatrix.transformPosition(new Vector3d(+1, -1, -1)),
                actualBoxMatrix.transformPosition(new Vector3d(-1, +1, +1)),
                actualBoxMatrix.transformPosition(new Vector3d(-1, +1, -1)),
                actualBoxMatrix.transformPosition(new Vector3d(-1, -1, +1)),
                actualBoxMatrix.transformPosition(new Vector3d(-1, -1, -1))
        };
    }

    public static Parallelepiped fromArray(double[] array) {
        // From 6.7.1.4.2. Box:
        // ...
        // The first three elements define the x, y, and z values for the center of the box.
        // The next three elements (with indices 3, 4, and 5) define the x-axis direction and half-length.
        // The next three elements (indices 6, 7, and 8) define the y-axis direction and half-length.
        // The last three elements (indices 9, 10, and 11) define the z-axis direction and half-length.
        return new Parallelepiped(
                new Vector3d(array[0], array[1], array[2]),
                new Vector3d(array[3], array[4], array[5]),
                new Vector3d(array[6], array[7], array[8]),
                new Vector3d(array[9], array[10], array[11])
        );
    }
}
