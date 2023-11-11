package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class FlatTileProjection {


    private static final int[][] CORNER_MATRIX = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };
    private static final int[][] LAT_INVERTED_CORNER_MATRIX = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };


    @Setter
    protected int defaultZoom = FlatTileMapService.DEFAULT_ZOOM;
    @Setter
    private boolean invertLatitude = false, invertZoom = false, flipVertically = false;


    /**
     * Converts the player's position into the tile coordinates
     *
     * @param longitude Longitude of the player's position, in degrees
     * @param latitude Latitude of the player's position, in degrees
     * @param relativeZoom Tile zoom relative to the default one
     * @return Tile coordinate
     * @throws OutOfProjectionBoundsException When the player is out of bounds from the projection
     */
    public final int[] geoCoordToTileCoord(double longitude, double latitude, int relativeZoom) throws OutOfProjectionBoundsException {
        return this.toTileCoord(longitude, invertLatitude ? -latitude : latitude, relativeZoomToAbsolute(relativeZoom));
    }
    public abstract int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException;


    /**
     * Converts a tile coordinates into its corresponding geographic coordinates (WGS84)
     * @param tileX Tile X
     * @param tileY Tile Y
     * @param relativeZoom Tile zoom relative to the default one
     * @return Geographic coordinate (WGS84)
     * @throws OutOfProjectionBoundsException When the tile is out of bounds from the projection
     */
    public final double[] tileCoordToGeoCoord(int tileX, int tileY, int relativeZoom) throws OutOfProjectionBoundsException {
        double[] coord = this.toGeoCoord(tileX, tileY, relativeZoomToAbsolute(relativeZoom));
        if(invertLatitude) coord[1] = -coord[1];
        return coord;
    }
    protected abstract double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) throws OutOfProjectionBoundsException;


    @Override
    public abstract FlatTileProjection clone();


    public final int relativeZoomToAbsolute(int relativeZoom) {
        return defaultZoom + (invertZoom ? -relativeZoom : relativeZoom);
    }
    public final boolean isRelativeZoomAvailable(int relativeZoom) {
        return this.isAbsoluteZoomAvailable(defaultZoom + (invertZoom ? -relativeZoom : relativeZoom));
    }
    public abstract boolean isAbsoluteZoomAvailable(int absoluteZoom);


    public int[] getCornerMatrix(int i) {
        return invertLatitude ^ flipVertically ? LAT_INVERTED_CORNER_MATRIX[i] : CORNER_MATRIX[i];
    }
}
