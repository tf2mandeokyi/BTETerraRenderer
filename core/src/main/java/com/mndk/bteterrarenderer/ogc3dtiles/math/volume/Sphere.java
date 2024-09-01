package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.UnitSphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrixf;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
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

    public Sphere(Cartesian3f center, double radius) {
        this(center, (float) radius);
    }

    public Sphere(Cartesian3f center, float radius) {
        this.center = center;
        this.radius = radius;

        Matrixf centerMatrix = center.toTransformableMatrix();
        this.sphereMatrix = new Matrix4f((c, r) -> {
            if(c == 3) return centerMatrix.get(0, r);
            else if(r != 3) return c == r ? radius : 0;
            else return 0;
        });
    }

    @Override
    public boolean intersectsSphere(Sphere other, Matrix4f thisTransform) {
        Matrixf actualSphereMatrix = thisTransform.multiply(this.sphereMatrix);
        Matrixf inverseOtherMatrix = other.sphereMatrix.inverse();

        // "Unit-alize" the other sphere
        Matrix4f transformedSphereMatrix = inverseOtherMatrix.multiply(actualSphereMatrix).toMatrix4();
        return UnitSphere.checkEllipsoidIntersection(transformedSphereMatrix);
    }

    @Override
    public boolean intersectsRay(Cartesian3f rayStart, Cartesian3f rayEnd, Matrix4f thisTransform) {
        Matrixf actualSphereMatrix = thisTransform.multiply(this.sphereMatrix);
        Matrix4f inverse = actualSphereMatrix.inverse().toMatrix4();

        Cartesian3f unitRayStart = rayStart.transform(inverse);
        Cartesian3f unitRayEnd = rayEnd.transform(inverse);
        return UnitSphere.checkRayIntersection(unitRayStart, unitRayEnd);
    }

    public Region[] toBoundingRegions() {
        // For simplicity, we will consider that the Earth is a sphere, instead of a spheroid

        Spheroid3 coordinate = SpheroidCoordinatesConverter.WGS84.toSpheroid(this.center);
        double longitude = coordinate.getLongitude();
        double latitude = coordinate.getLatitude();

        double distance = this.center.distance();
        if(distance == 0) return new Region[0];
        if(distance > this.radius) {

            double latitudeDiff = Math.asin(this.radius / distance);
            if(this.center.xyDistance() > this.radius) {
                double longitudeDiff = Math.asin(this.radius / this.center.xyDistance());
                return new Region[] {
                        new Region(
                                longitude - longitudeDiff, latitude - latitudeDiff,
                                longitude + longitudeDiff, latitude + latitudeDiff,
                                coordinate.getHeight() - radius,
                                coordinate.getHeight() + radius
                        )
                };
            } else {
                Region sameSide = new Region(
                        longitude - Math.PI, latitude - latitudeDiff,
                        longitude + Math.PI, Math.PI / 2,
                        coordinate.getHeight() - radius,
                        coordinate.getHeight() + radius
                );
                Region otherSide = new Region(
                        longitude + Math.PI, Math.PI - (latitude + latitudeDiff),
                        longitude - Math.PI, Math.PI / 2,
                        coordinate.getHeight() - radius,
                        coordinate.getHeight() + radius
                );
                return new Region[] { sameSide, otherSide };
            }
        } else {
            return new Region[] {
                    new Region(-Math.PI, -Math.PI / 2, Math.PI, Math.PI / 2, 0, distance + radius)
            };
        }
    }

    public static Sphere fromArray(double[] array) {
        return new Sphere(Cartesian3f.fromArray(array), (float) array[3]);
    }
}
