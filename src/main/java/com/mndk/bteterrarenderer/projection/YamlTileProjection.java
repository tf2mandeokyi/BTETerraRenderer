package com.mndk.bteterrarenderer.projection;

import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class YamlTileProjection extends TileProjection {


    private final GeographicProjection projection;
    private final Map<Integer, TileMatrix> matrices;


    @SuppressWarnings("unchecked")
    public YamlTileProjection(Map<String, Object> jsonObject) throws NullPointerException {

        Map<String, Object> projectionObject = (Map<String, Object>) jsonObject.get("projection");
        Map<Integer, Object> tileMatricesObject = (Map<Integer, Object>) jsonObject.get("tile_matrices");

        switch((String) projectionObject.get("type")) {
            case "epsg":
                String name = (String) projectionObject.get("name");
                String parameter = (String) projectionObject.get("param");
                this.projection = new Proj4jProjection(name, parameter);
                break;
            case "bte":
                String json = (String) projectionObject.get("json");
                this.projection = GeographicProjection.parse(json);
                break;
            default:
                throw new RuntimeException("Unknown projection data");
        }

        this.matrices = new HashMap<>();
        for(Map.Entry<Integer, Object> entry : tileMatricesObject.entrySet()) {
            List<Object> matrix = (List<Object>) entry.getValue();
            this.matrices.put(entry.getKey(), new TileMatrix(matrix));
        }
    }


    @Override
    protected int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException {
        double[] coordinate = this.projection.fromGeo(longitude, latitude);
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        int tileX = (int) Math.floor((coordinate[0] - matrix.pointOfOrigin[0]) / matrix.tileSize[0]);
        int tileY = (int) Math.floor((matrix.pointOfOrigin[1] - coordinate[1]) / matrix.tileSize[1]);

        return new int[] { tileX, tileY };
    }


    @Override
    protected double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) throws OutOfProjectionBoundsException {
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        double tileCoordinateX = tileX * matrix.tileSize[0] + matrix.pointOfOrigin[0];
        double tileCoordinateY = matrix.pointOfOrigin[1] - tileY * matrix.tileSize[1];

        return this.projection.toGeo(tileCoordinateX, tileCoordinateY);
    }


    @Override
    public TileProjection clone() {
        return new YamlTileProjection(this.projection, this.matrices);
    }


    private static class TileMatrix {

        final double[] pointOfOrigin, tileSize;

        TileMatrix(List<Object> jsonList) {
            this.pointOfOrigin = new double[] { (double) jsonList.get(0), (double) jsonList.get(1) };
            this.tileSize = new double[] { (double) jsonList.get(2), (double) jsonList.get(3) };
        }

    }

}
