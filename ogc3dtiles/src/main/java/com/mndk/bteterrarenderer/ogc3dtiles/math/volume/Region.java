package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joml.Matrix4d;

@Data
@EqualsAndHashCode(callSuper = true)
public class Region extends Volume {

    private final SpheroidArc southInnerArc;
    private final SpheroidArc northInnerArc;
    private final SpheroidArc southOuterArc;
    private final SpheroidArc northOuterArc;

    public Region(double westLon, double southLat, double eastLon, double northLat, double minHeight, double maxHeight) {
        this.southInnerArc = new SpheroidArc(westLon, eastLon, southLat, minHeight);
        this.northInnerArc = new SpheroidArc(westLon, eastLon, northLat, minHeight);
        this.southOuterArc = new SpheroidArc(westLon, eastLon, southLat, maxHeight);
        this.northOuterArc = new SpheroidArc(westLon, eastLon, northLat, maxHeight);
    }

    public double getWestLonRadian() { return southInnerArc.getWest(); }
    public double getEastLonRadian() { return southInnerArc.getEast(); }
    public double getSouthLatRadian() { return southInnerArc.getLatitude(); }
    public double getNorthLatRadian() { return northOuterArc.getLatitude(); }
    public double getMinHeight() { return southInnerArc.getHeight(); }
    public double getMaxHeight() { return northOuterArc.getHeight(); }

    @Override
    public boolean intersectsPositiveSides(Plane[] planes, Matrix4d thisTransform,
                                           SpheroidCoordinatesConverter converter) {
        AABB boundingBox = this.getBoundingBox(converter);
        Parallelepiped parallelepiped = boundingBox.toBox();
        return parallelepiped.intersectsPositiveSides(planes, new Matrix4d(), converter);
    }

    @Override
    public BoundingSphere getLevelOfDetailSphere(Matrix4d thisTransform, SpheroidCoordinatesConverter converter) {
        AABB boundingBox = this.getBoundingBox(converter);
        Parallelepiped parallelepiped = boundingBox.toBox();
        return parallelepiped.getLevelOfDetailSphere(new Matrix4d(), converter);
    }

    public AABB getBoundingBox(SpheroidCoordinatesConverter converter) {
        return southInnerArc.getBoundingBox(converter)
                .include(northInnerArc.getBoundingBox(converter))
                .include(southOuterArc.getBoundingBox(converter))
                .include(northOuterArc.getBoundingBox(converter));
    }

    public String toString() {
        return String.format(
                "Region(lon=[W=%.7f°, E=%.7f°], lat=[N=%.7f°, S=%.7f°], height=[%.1fm ~ %.1fm])",
                Math.toDegrees(getWestLonRadian()), Math.toDegrees(getEastLonRadian()),
                Math.toDegrees(getNorthLatRadian()), Math.toDegrees(getSouthLatRadian()),
                southInnerArc.getHeight(), northOuterArc.getHeight()
        );
    }

    public static Region fromArray(double[] array) {
        return new Region(array[0], array[1], array[2], array[3], array[4], array[5]);
    }
}
