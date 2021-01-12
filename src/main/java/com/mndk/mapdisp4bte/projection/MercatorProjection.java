package com.mndk.mapdisp4bte.projection;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;

public class MercatorProjection extends GeographicProjection {
    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return new double[0];
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return new double[0];
    }

    @Override
    public double metersPerUnit() {
        return 0;
    }
}
