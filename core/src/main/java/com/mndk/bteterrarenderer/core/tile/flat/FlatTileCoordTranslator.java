package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class FlatTileCoordTranslator {

    private static final MatrixRow[] CORNER_MATRIX = {
            new MatrixRow(0, 1, 0, 1), // top left
            new MatrixRow(1, 1, 1, 1), // top right
            new MatrixRow(1, 0, 1, 0), // bottom right
            new MatrixRow(0, 0, 0, 0)  // bottom left
    };
    private static final MatrixRow[] LAT_INVERTED_CORNER_MATRIX = {
            new MatrixRow(0, 1, 0, 0), // top left
            new MatrixRow(1, 1, 1, 0), // top right
            new MatrixRow(1, 0, 1, 1), // bottom right
            new MatrixRow(0, 0, 0, 1)   // bottom left
    };

    private final FlatTileProjection projection;
    private int defaultZoom = FlatTileMapService.DEFAULT_ZOOM;
    private boolean invertLatitude = false;
    private boolean invertZoom = false;
    private boolean flipVertically = false;

    /**
     * Converts the player's position into the tile coordinates
     *
     * @param longitude Longitude of the player's position, in degrees
     * @param latitude Latitude of the player's position, in degrees
     * @param relativeZoom Tile zoom relative to the default one
     * @return Tile coordinate
     * @throws OutOfProjectionBoundsException When the player is out of bounds from the projection
     */
    public final int[] geoCoordToTileCoord(double longitude, double latitude, int relativeZoom)
            throws OutOfProjectionBoundsException {
        int absoluteZoom = this.relativeZoomToAbsolute(relativeZoom);
        return this.projection.toTileCoord(longitude, this.invertLatitude ? -latitude : latitude, absoluteZoom);
    }

    /**
     * Converts a tile coordinates into its corresponding geographic coordinates (WGS84)
     * @param tileX Tile X
     * @param tileY Tile Y
     * @param relativeZoom Tile zoom relative to the default one
     * @return Geographic coordinate (WGS84)
     * @throws OutOfProjectionBoundsException When the tile is out of bounds from the projection
     */
    public final double[] tileCoordToGeoCoord(double tileX, double tileY, int relativeZoom)
            throws OutOfProjectionBoundsException {
        int absoluteZoom = this.relativeZoomToAbsolute(relativeZoom);
        double[] coord = this.projection.toGeoCoord(tileX, tileY, absoluteZoom);
        if (this.invertLatitude) coord[1] = -coord[1];
        return coord;
    }

    public final int relativeZoomToAbsolute(int relativeZoom) {
        return defaultZoom + (invertZoom ? -relativeZoom : relativeZoom);
    }
    public final boolean isRelativeZoomAvailable(int relativeZoom) {
        int absoluteZoom = this.relativeZoomToAbsolute(relativeZoom);
        return this.projection.isAbsoluteZoomAvailable(absoluteZoom);
    }
    public final boolean isTileCoordInBounds(FlatTileRelCoord tileCoord) {
        int absoluteZoom = this.relativeZoomToAbsolute(tileCoord.getRelativeZoom());
        return this.projection.isTileCoordInBounds(tileCoord.getX(), tileCoord.getY(), absoluteZoom);
    }

    public MatrixRow getCornerMatrixRow(int i) {
        return this.invertLatitude ^ this.flipVertically ? LAT_INVERTED_CORNER_MATRIX[i] : CORNER_MATRIX[i];
    }

    @Getter
    @RequiredArgsConstructor
    public static class MatrixRow {
        private final int x, y, u, v;
    }
}
