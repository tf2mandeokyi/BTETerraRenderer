package com.mndk.bteterrarenderer.connector.terraplusplus.projection;

public interface IGeographicProjection {
    double[] toGeo(double x, double y) throws Exception;
    double[] fromGeo(double longitude, double latitude) throws Exception;
}
