package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;

import javax.annotation.Nullable;

public interface FlatTileProjection {
    @Nullable
    String getName();
    void setName(String name);

    int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException;
    double[] toGeoCoord(double tileX, double tileY, int absoluteZoom) throws OutOfProjectionBoundsException;
    boolean isAbsoluteZoomAvailable(int absoluteZoom);
    boolean isTileCoordInBounds(int tileX, int tileY, int absoluteZoom);
}
