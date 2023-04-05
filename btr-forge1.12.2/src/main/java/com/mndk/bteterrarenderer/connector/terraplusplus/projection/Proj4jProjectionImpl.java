package com.mndk.bteterrarenderer.connector.terraplusplus.projection;

import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class Proj4jProjectionImpl implements GeographicProjection {
    private final Proj4jProjection delegate;

    public Proj4jProjectionImpl(String crsName, String crsParameter) {
        this.delegate = new Proj4jProjection(crsName, crsParameter);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return delegate.toGeo(x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return delegate.fromGeo(longitude, latitude);
    }

    @Override
    public double metersPerUnit() {
        return 0;
    }

    public static void registerProjection() {
        GlobalParseRegistries.PROJECTIONS.putIfAbsent("proj4", Proj4jProjectionImpl.class);
    }
}
