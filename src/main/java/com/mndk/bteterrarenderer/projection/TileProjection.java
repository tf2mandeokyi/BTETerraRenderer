package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.tile.TileMapService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class TileProjection {


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
    protected int defaultZoom = TileMapService.DEFAULT_ZOOM;
    @Setter
    protected boolean invertLatitude = false, invertZoom = false;


    /**
     * Converts the player's position into the tile coordinates
     *
     * @param longitude Longitude of the player's position
     * @param latitude Latitude of the player's position
     * @param relativeZoom Tile zoom relative to the default one
     * @return Tile coordinate
     * @throws OutOfProjectionBoundsException When the player is out of bounds from the projection
     */
    public final int[] geoCoordToTileCoord(double longitude, double latitude, int relativeZoom)
            throws OutOfProjectionBoundsException {

        return this.toTileCoord(longitude, latitude, defaultZoom + (invertZoom ? -relativeZoom : relativeZoom));
    }
    protected abstract int[] toTileCoord(double longitude, double latitude, int absoluteZoom)
            throws OutOfProjectionBoundsException;


    /**
     * Converts a tile coordinates into its corresponding geographic coordinates (WGS84)
     * @param tileX Tile X
     * @param tileY Tile Y
     * @param relativeZoom Tile zoom relative to the default one
     * @return Geographic coordinate (WGS84)
     * @throws OutOfProjectionBoundsException When the tile is out of bounds from the projection
     */
    public final double[] tileCoordToGeoCoord(int tileX, int tileY, int relativeZoom)
            throws OutOfProjectionBoundsException {

        return this.toGeoCoord(tileX, tileY, defaultZoom + (invertZoom ? -relativeZoom : relativeZoom));
    }
    protected abstract double[] toGeoCoord(int tileX, int tileY, int absoluteZoom)
            throws OutOfProjectionBoundsException;


    @Override
    public abstract TileProjection clone() throws CloneNotSupportedException;


    public int[] getCornerMatrix(int i) {
        return invertLatitude ? LAT_INVERTED_CORNER_MATRIX[i] : CORNER_MATRIX[i];
    }
}
