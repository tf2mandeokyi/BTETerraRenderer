package com.mndk.bteterrarenderer.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnector;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.graphics.GraphicsModelManager;
import com.mndk.bteterrarenderer.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.projection.Projections;
import com.mndk.bteterrarenderer.projection.TileProjection;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import com.mndk.bteterrarenderer.util.RangedIntPropertyAccessor;
import lombok.*;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@EqualsAndHashCode(callSuper = false)
public class FlatTileMapService extends TileMapService {

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;

    public static final int DEFAULT_MAX_THREAD = 2;
    public static final int DEFAULT_ZOOM = 18;
    private static final Timer TIMER = new Timer();

    @Getter @Setter
    private transient int relativeZoom = 0, radius = 3;

    private final String name;
    private final String urlTemplate;
    private final TileProjection tileProjection;
    private final FlatTileURLConverter urlConverter;
    private final ExecutorService downloadExecutor;

    /**
     * @throws NullPointerException If the projection corresponding to its id does not exist
     */
    @JsonCreator
    public FlatTileMapService(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "tile_url", required = true) String urlTemplate,
            @JsonProperty(value = "projection", required = true) String projectionName,
            @JsonProperty(value = "max_thread", defaultValue = "2") Integer maxThread,
            @JsonProperty(value = "default_zoom", defaultValue = "18") Integer defaultZoom,
            @JsonProperty(value = "invert_lat", defaultValue = "false") Boolean invertLatitude,
            @JsonProperty(value = "flip_vert", defaultValue = "false") Boolean flipVertically,
            @JsonProperty(value = "invert_zoom", defaultValue = "false") Boolean invertZoom
    ) throws NullPointerException, CloneNotSupportedException {

        this.name = name;
        this.urlTemplate = urlTemplate;

        TileProjection projectionSearchResult = ProjectionYamlLoader.INSTANCE.result.get(projectionName);
        int _defaultZoom = BtrUtil.validateNull(defaultZoom, DEFAULT_ZOOM);
        boolean _invertZoom = BtrUtil.validateNull(invertZoom, false);

        if(projectionSearchResult != null) {
            this.tileProjection = projectionSearchResult.clone();
            tileProjection.setDefaultZoom(_defaultZoom);
            tileProjection.setInvertZoom(_invertZoom);
            tileProjection.setFlipVertically(BtrUtil.validateNull(flipVertically, false));
            tileProjection.setInvertLatitude(BtrUtil.validateNull(invertLatitude, false));
        } else {
            BTETerraRendererConstants.LOGGER.error("Couldn't find tile projection named \"" + projectionName + "\"");
            this.tileProjection = null;
        }

        this.urlConverter = new FlatTileURLConverter(_defaultZoom, _invertZoom);
        this.downloadExecutor = Executors.newFixedThreadPool(BtrUtil.validateNull(maxThread, DEFAULT_MAX_THREAD));

        this.properties.add(new PropertyAccessor.Localized<>("zoom", "gui.bteterrarenderer.settings.zoom",
                RangedIntPropertyAccessor.of(this::getRelativeZoom, this::setRelativeZoom,
                        this::isRelativeZoomAvailable, -4, 4)));

        this.properties.add(new PropertyAccessor.Localized<>("radius", "gui.bteterrarenderer.settings.size",
                RangedIntPropertyAccessor.of(this::getRadius, this::setRadius, 1, 10)));
    }

    public String getUrlFromGeoCoordinate(double longitude, double latitude, int relativeZoom) throws OutOfProjectionBoundsException {
        int[] tileCoord = this.tileProjection.geoCoordToTileCoord(longitude, latitude, relativeZoom);
        return this.getUrlFromTileCoordinate(tileCoord[0], tileCoord[1], relativeZoom);
    }

    public String getUrlFromTileCoordinate(int tileX, int tileY, int relativeZoom) {
        return this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
    }

    @Override
    protected double getYAlign() {
        return BTRConfigConnector.INSTANCE.getRenderSettings().getFlatMapYAxis() + Y_EPSILON;
    }

    @Override
    protected Set<GraphicsModel> getTileModels(Object poseStack, String tmsId,
                                               double px, double py, double pz) {
        if(this.tileProjection == null) return null;

        try {
            Set<GraphicsModel> result = new HashSet<>();
            double[] geoCoord = Projections.getServerProjection().toGeo(px, pz);
            int[] tileCoord = this.tileProjection.geoCoordToTileCoord(geoCoord[0], geoCoord[1], relativeZoom);

            GraphicsModelManager.INSTANCE.registerAllModelsInQueue();

            for(int i = 0; i < 2 * this.radius + 1; ++i) {
                if(i == 0) {
                    this.checkTile(result, tmsId, tileCoord[0], tileCoord[1]);
                }
                for(int j = 0; j < i; ++j) {
                    this.checkTile(result, tmsId, tileCoord, -j, j - i);
                    this.checkTile(result, tmsId, tileCoord, j - i, +j);
                    this.checkTile(result, tmsId, tileCoord, j, i - j);
                    this.checkTile(result, tmsId, tileCoord, i - j, -j);
                }
            }

            return result;

        } catch(OutOfProjectionBoundsException ignored) {
        } catch(Exception e) {
            BTETerraRendererConstants.LOGGER.warn("Caught exception while rendering tile images", e);
        }
        return null;
    }

    private void checkTile(Set<GraphicsModel> set, String tmsId, int[] tileCoord, int dx, int dy)
            throws OutOfProjectionBoundsException {

        if(Math.abs(dx) > radius || Math.abs(dy) > radius) return;
        this.checkTile(set, tmsId, tileCoord[0] + dx, tileCoord[1] + dy);
    }

    private void checkTile(Set<GraphicsModel> set, String tmsId, int tileX, int tileY)
            throws OutOfProjectionBoundsException {

        String tileKey = this.genTileKey(tmsId, tileX, tileY, relativeZoom);

        // Return if the requested tile is still in the downloading state
        if(GraphicsModelManager.INSTANCE.isTextureInDownloadingState(tileKey)) return;

        if(GraphicsModelManager.INSTANCE.modelExists(tileKey)) {
            set.add(GraphicsModelManager.INSTANCE.updateAndGetModel(tileKey));
            return;
        }

        // If the requested tile is not loaded, load it in the new thread
        /*
         *  i=0 ------ i=1
         *   |          |
         *   |   TILE   |
         *   |          |
         *  i=3 ------ i=2
         */
        GraphicsQuad<GraphicsQuad.PosTexColor> quad = new GraphicsQuad<>();
        for (int i = 0; i < 4; i++) {
            int[] mat = this.tileProjection.getCornerMatrix(i);
            double[] geoCoord = tileProjection.tileCoordToGeoCoord(tileX + mat[0], tileY + mat[1], relativeZoom);
            double[] gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

            quad.setVertex(i, new GraphicsQuad.PosTexColor(
                    gameCoord[0], 0, gameCoord[1],
                    mat[2], mat[3],
                    1f, 1f, 1f, 1f
            ));
        }
        String url = this.getUrlFromTileCoordinate(tileX, tileY, relativeZoom);

        // download tile
        GraphicsModelManager.INSTANCE.setTextureInDownloadingState(tileKey);
        this.downloadExecutor.execute(new TileImageDownloadingTask(tileKey, url, quad, 0));
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return tileProjection != null && tileProjection.isRelativeZoomAvailable(relativeZoom);
    }

    public String genTileKey(String id, int tileX, int tileY, int zoom) {
        return "tilemap_" + id + "_" + tileX + "_" + tileY + "_" + zoom;
    }

    @Override
    public String toString() {
        return FlatTileMapService.class.getName() + "{name=" + name + ", tile_url=" + urlTemplate + "}";
    }

    @RequiredArgsConstructor
    protected class TileImageDownloadingTask implements Runnable {

        private final String tileKey, url;
        private final GraphicsQuad<GraphicsQuad.PosTexColor> quad;
        private final int retry;

        @Override
        public void run() {
            GraphicsModelManager cache = GraphicsModelManager.INSTANCE;

            if (retry >= DOWNLOAD_RETRY_COUNT + 1) {
                cache.textureDownloadingComplete(tileKey, SOMETHING_WENT_WRONG, Collections.singletonList(quad));
                return;
            }

            try {
                InputStream stream = HttpConnector.INSTANCE.download(url);
                cache.textureDownloadingComplete(tileKey, ImageIO.read(stream), Collections.singletonList(quad));
                return;
            } catch (Exception e) {
                BTETerraRendererConstants.LOGGER.error("Caught exception while downloading a tile image (" +
                        "TileKey=" + tileKey + ", Retry #" + (retry + 1) + ")");
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    downloadExecutor.execute(new TileImageDownloadingTask(tileKey, url, quad, retry + 1));
                }
            }, 1000);
        }
    }
}
