package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.projection.TileProjection;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelBaker;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.util.JsonParserUtil;
import com.mndk.bteterrarenderer.core.util.accessor.RangedIntPropertyAccessor;
import lombok.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
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

    private transient int relativeZoom = 0;
    @Setter
    private transient int radius = 3;

    private final String urlTemplate;
    private final TileProjection tileProjection;
    private final FlatTileURLConverter urlConverter;

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

    @Override
    protected double getYAlign() {
        return BTETerraRendererConfig.HologramConfig.INSTANCE.getFlatMapYAxis() + Y_EPSILON;
    }

    public void setRelativeZoom(int newZoom) {
        if(this.relativeZoom == newZoom) return;
        this.relativeZoom = newZoom;
        GraphicsModelBaker.INSTANCE.newQueue();
    }

    public String getUrlFromTileCoordinate(int tileX, int tileY, int relativeZoom) {
        return this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
    }

    @Override
    protected Set<GraphicsModel> getTileModels(Object poseStack, String tmsId, double px, double py, double pz) {
        if(this.tileProjection == null) return null;

        try {
            Set<GraphicsModel> result = new HashSet<>();
            double[] geoCoord = Projections.getServerProjection().toGeo(px, pz);
            int[] tileCoord = this.tileProjection.geoCoordToTileCoord(geoCoord[0], geoCoord[1], relativeZoom);

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
        TileKey tileKey = new TileKey(tmsId, tileX, tileY, relativeZoom);

        // Return if the requested tile is downloaded but not yet ready
        if(GraphicsModelBaker.INSTANCE.isTextureNotReady(tileKey)) {
            set.add(new GraphicsModel(LOADING.getId(), Collections.singletonList(this.computeTileQuad(tileKey))));
            return;
        }

        // Return if error
        if(GraphicsModelBaker.INSTANCE.isTextureError(tileKey)) {
            set.add(new GraphicsModel(SOMETHING_WENT_WRONG.getId(), Collections.singletonList(this.computeTileQuad(tileKey))));
            return;
        }

        if(GraphicsModelBaker.INSTANCE.modelExists(tileKey)) {
            set.add(GraphicsModelBaker.INSTANCE.updateAndGetModel(tileKey));
            return;
        }

        // If the requested tile is not loaded, download it in the new thread
        GraphicsModelBaker.INSTANCE.setTextureInDownloadingState(tileKey);
        String url = this.getUrlFromTileCoordinate(tileKey.x, tileKey.y, relativeZoom);
        this.downloadExecutor.execute(new TileImageDownloadingTask(tileKey, url, this.computeTileQuad(tileKey), 0));
    }

    private GraphicsQuad<GraphicsQuad.PosTexColor> computeTileQuad(TileKey tileKey) throws OutOfProjectionBoundsException {
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
            double[] geoCoord = tileProjection.tileCoordToGeoCoord(tileKey.x + mat[0], tileKey.y + mat[1], relativeZoom);
            double[] gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

            quad.setVertex(i, new GraphicsQuad.PosTexColor(
                    gameCoord[0], 0, gameCoord[1],
                    mat[2], mat[3],
                    1f, 1f, 1f, 1f));
        }
        return quad;
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return tileProjection != null && tileProjection.isRelativeZoomAvailable(relativeZoom);
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class TileKey {
        private final String tmsId;
        private final int x, y, zoom;
    }

    @RequiredArgsConstructor
    protected class TileImageDownloadingTask implements Runnable {

        private final TileKey tileKey;
        private final String url;
        private final GraphicsQuad<GraphicsQuad.PosTexColor> quad;
        private final int retry;

        @Override
        public void run() {
            if (retry >= DOWNLOAD_RETRY_COUNT) {
                GraphicsModelBaker.INSTANCE.textureDownloadingError(tileKey);
                return;
            }

            try {
                InputStream stream = HttpResourceManager.download(url);
                BufferedImage image = ImageIO.read(stream);
                GraphicsModelBaker.INSTANCE.textureDownloadingComplete(tileKey, image, Collections.singletonList(quad));
                return;
            } catch (Exception ignored) {}

            BTETerraRendererConstants.LOGGER.error("Caught exception while downloading a tile image (" +
                    "TileKey=" + tileKey + ", Retry #" + (retry + 1) + ")");

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
        public FlatTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
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
                /*
                 * If the projection corresponding to its id does not exist
                 */
                throw JsonMappingException.from(p, "unknown projection name: " + projectionName);
            }

            FlatTileURLConverter urlConverter = new FlatTileURLConverter(defaultZoom, invertZoom);
            ExecutorService downloadExecutor = Executors.newFixedThreadPool(maxThread);

            return new FlatTileMapService(name, urlTemplate, projection, urlConverter, downloadExecutor);
        }
    }
}
