package com.mndk.bteterrarenderer.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
import com.mndk.bteterrarenderer.util.JsonParserUtil;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import com.mndk.bteterrarenderer.util.RangedIntPropertyAccessor;
import lombok.*;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = FlatTileMapService.Deserializer.class)
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

    private final String urlTemplate;
    private final TileProjection tileProjection;
    private final FlatTileURLConverter urlConverter;

    /**
     * @throws NullPointerException If the projection corresponding to its id does not exist
     */
    @JsonCreator
    public FlatTileMapService(String name, String urlTemplate,
                              TileProjection tileProjection, FlatTileURLConverter urlConverter,
                              ExecutorService downloadExecutor) {
        super(name, downloadExecutor);
        this.urlTemplate = urlTemplate;
        this.tileProjection = tileProjection;
        this.urlConverter = urlConverter;

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

    public static class Deserializer extends JsonDeserializer<FlatTileMapService> {
        @Override
        public FlatTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = ctxt.readTree(p);

            String name = node.get("name").asText();
            String urlTemplate = node.get("tile_url").asText();

            int defaultZoom = JsonParserUtil.getOrDefault(node, "default_zoom", DEFAULT_ZOOM);
            boolean invertZoom = JsonParserUtil.getOrDefault(node, "invert_zoom", false);
            boolean invertLatitude = JsonParserUtil.getOrDefault(node, "invert_lat", false);
            boolean flipVertically = JsonParserUtil.getOrDefault(node, "flip_vert", false);
            int maxThread = JsonParserUtil.getOrDefault(node, "max_thread", DEFAULT_MAX_THREAD);

            String projectionName = node.get("projection").asText();
            TileProjection projection = ProjectionYamlLoader.INSTANCE.result.get(projectionName);
            if(projection != null) {
                projection = projection.clone()
                        .setDefaultZoom(defaultZoom)
                        .setInvertZoom(invertZoom)
                        .setFlipVertically(flipVertically)
                        .setInvertLatitude(invertLatitude);
            } else {
                throw JsonMappingException.from(p, "unknown projection name" + projectionName);
            }

            FlatTileURLConverter urlConverter = new FlatTileURLConverter(defaultZoom, invertZoom);
            ExecutorService downloadExecutor = Executors.newFixedThreadPool(maxThread);

            return new FlatTileMapService(name, urlTemplate, projection, urlConverter, downloadExecutor);
        }
    }
}
