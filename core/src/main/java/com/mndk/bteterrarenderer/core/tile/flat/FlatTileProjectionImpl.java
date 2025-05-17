package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

@JsonSerialize
@JsonDeserialize
public class FlatTileProjectionImpl implements FlatTileProjection {

    @Getter @Setter @Nullable
    private transient String name;
    @JsonProperty(value = "projection")
    private final GeographicProjection projection;
    @JsonProperty(value = "tile_matrices")
    private final Map<Integer, TileMatrix> matrices;

    @JsonCreator
    public FlatTileProjectionImpl(
            @JsonProperty(value = "projection", required = true)
            GeographicProjection projection,
            @JsonProperty(value = "tile_matrices", required = true)
            Map<Integer, TileMatrix> matrices
    ) {
        this.projection = projection;
        this.matrices = matrices;
    }

    @Override
    public int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException {
        double[] coordinate = this.projection.fromGeo(longitude, latitude);
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        // tileSize is precomputed in constructor
        double tileSizeX = matrix.actualTileSize[0];
        double tileSizeY = matrix.actualTileSize[1];
        int tileX = (int) Math.floor((coordinate[0] - matrix.pointOfOrigin[0]) / tileSizeX);
        int tileY = (int) Math.floor((matrix.pointOfOrigin[1] - coordinate[1]) / tileSizeY);

        // enforce tile bounds if defined
        if (matrix.rangeX != null && (tileX < matrix.rangeX[0] || tileX > matrix.rangeX[1])) {
            throw OutOfProjectionBoundsException.get();
        }
        if (matrix.rangeY != null && (tileY < matrix.rangeY[0] || tileY > matrix.rangeY[1])) {
            throw OutOfProjectionBoundsException.get();
        }

        return new int[] { tileX, tileY };
    }

    @Override
    public double[] toGeoCoord(double tileX, double tileY, int absoluteZoom) throws OutOfProjectionBoundsException {
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        // tileSize is precomputed in constructor
        double tileSizeX = matrix.actualTileSize[0];
        double tileSizeY = matrix.actualTileSize[1];
        double tileCoordinateX = tileX * tileSizeX + matrix.pointOfOrigin[0];
        double tileCoordinateY = matrix.pointOfOrigin[1] - tileY * tileSizeY;

        return this.projection.toGeo(tileCoordinateX, tileCoordinateY);
    }

    @Override
    public boolean isAbsoluteZoomAvailable(int absoluteZoom) {
        return this.matrices.containsKey(absoluteZoom);
    }

    @Override
    public boolean isTileCoordInBounds(int tileX, int tileY, int absoluteZoom) {
        TileMatrix matrix = this.matrices.get(absoluteZoom);
        return matrix != null
                && (matrix.rangeX == null || (tileX >= matrix.rangeX[0] && tileX <= matrix.rangeX[1]))
                && (matrix.rangeY == null || (tileY >= matrix.rangeY[0] && tileY <= matrix.rangeY[1]));
    }

    @JsonSerialize
    @JsonDeserialize
    public static class TileMatrix {
        @JsonProperty("origin") private final double[] pointOfOrigin;
        @JsonProperty("full_extent") private final double[] fullExtent;
        @JsonProperty("tile_size") private final double[] tileSize;
        @JsonProperty("range_x") private final int[] rangeX;
        @JsonProperty("range_y") private final int[] rangeY;
        private final transient double[] actualTileSize;

        @JsonCreator
        public TileMatrix(
                @JsonProperty(value="origin", required = true) double[] pointOfOrigin,
                @JsonProperty(value="full_extent") double[] fullExtent,
                @JsonProperty(value="tile_size") double[] tileSize,
                @JsonProperty(value="range_x", required=true) int[] rangeX,
                @JsonProperty(value="range_y", required=true) int[] rangeY
        ) {
            this.pointOfOrigin = pointOfOrigin;
            this.fullExtent = fullExtent;
            this.tileSize = tileSize;
            this.rangeX = rangeX;
            this.rangeY = rangeY;
            // validate and compute tileSize
            if (tileSize != null && fullExtent != null) {
                throw new IllegalArgumentException("Cannot define both fullExtent and tileSize");
            }
            if (tileSize != null) {
                this.actualTileSize = tileSize;
            } else if (fullExtent != null) {
                int countX = rangeX[1] - rangeX[0] + 1;
                int countY = rangeY[1] - rangeY[0] + 1;
                this.actualTileSize = new double[] { fullExtent[0] / countX, fullExtent[1] / countY };
            } else {
                throw new IllegalArgumentException("Either fullExtent or tileSize must be defined");
            }
        }
    }

}
