package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Region extends Volume {
    // read these from array in order
    /** In radians */
    private final double westLon, southLat, eastLon, northLat;
    private final double minHeight, maxHeight;

    @Override
    public boolean containsCartesian(Cartesian3 cartesian, Matrix4 transform) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean intersectsGeoCoordinateRay(double[] coordinateDegrees, Matrix4 transform) {
        double lonRad = Math.toRadians(coordinateDegrees[0]), latRad = Math.toRadians(coordinateDegrees[1]);
        if(westLon <= eastLon) {
            if(lonRad < westLon || eastLon < lonRad) return false;
        } else {
            if(eastLon < lonRad && westLon < lonRad) return false;
        }

        return southLat <= latRad && latRad <= northLat;
    }

    public String toString() {
        return String.format(
                "Region(lon=[W=%.7f°, E=%.7f°], lat=[N=%.7f°, S=%.7f°], height=[%.1fm ~ %.1fm])",
                Math.toDegrees(westLon), Math.toDegrees(eastLon),
                Math.toDegrees(northLat), Math.toDegrees(southLat),
                minHeight, maxHeight
        );
    }

    public static Region fromArray(double[] array) {
        return new Region(array[0], array[1], array[2], array[3], array[4], array[5]);
    }
}
