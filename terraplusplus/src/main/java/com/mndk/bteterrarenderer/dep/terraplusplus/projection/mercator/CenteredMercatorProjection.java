package com.mndk.bteterrarenderer.dep.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.dep.terraplusplus.TerraConstants;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;

/**
 * Implementation of the Mercator projection, normalized between -1 and 1.
 *
 * @see WebMercatorProjection
 * @see <a href="https://en.wikipedia.org/wiki/Mercator_projection"> Wikipedia's article on the Mercator projection</a>
 */
@JsonDeserialize
public class CenteredMercatorProjection implements GeographicProjection {

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(x, y, 1, 1);
        return new double[]{
                x * 180.0,
                Math.toDegrees(Math.atan(Math.exp(-y * Math.PI)) * 2 - Math.PI / 2)
        };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, WebMercatorProjection.LIMIT_LATITUDE);
        return new double[]{
                longitude / 180.0,
                -(Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / Math.PI
        };
    }

    @Override
    public double[] bounds() {
        return new double[]{ -1, -1, 1, 1 };
    }

    @Override
    public double metersPerUnit() {
        return Math.cos(Math.toRadians(30)) * TerraConstants.EARTH_CIRCUMFERENCE / 2; //Accurate at about 30 degrees
    }

    @Override
    public boolean upright() {
        return true;
    }

    @Override
    public String toString() {
        return "Mercator";
    }
}
