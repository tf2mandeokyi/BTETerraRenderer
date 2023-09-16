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
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.projection.TileProjection;
import com.mndk.bteterrarenderer.core.util.JsonParserUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedIntPropertyAccessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.*;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = FlatTileMapService.Deserializer.class)
public class FlatTileMapService extends TileMapService<FlatTileMapService.TileKey> {

    @Data
    public static class TileKey {
        private final int x, y, relativeZoom;
    }

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;
    public static final int DEFAULT_ZOOM = 18;

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

    @Override
    protected List<TileKey> getRenderTileIdList(String tmsId, double longitude, double latitude, double height) {
        if(this.tileProjection == null) return Collections.emptyList();

        try {
            List<TileKey> result = new ArrayList<>();
            int[] tileCoord = this.tileProjection.geoCoordToTileCoord(longitude, latitude, relativeZoom);

            for(int i = 0; i < 2 * this.radius + 1; ++i) {
                if(i == 0) {
                    this.addTile(result, tileCoord, 0, 0);
                }
                for(int j = 0; j < i; ++j) {
                    this.addTile(result, tileCoord, -j, j - i);
                    this.addTile(result, tileCoord, j - i, +j);
                    this.addTile(result, tileCoord, j, i - j);
                    this.addTile(result, tileCoord, i - j, -j);
                }
            }

            return result;

        } catch(OutOfProjectionBoundsException ignored) {
        } catch(Exception e) {
            BTETerraRendererConstants.LOGGER.warn("Caught exception while rendering tile images", e);
        }
        return Collections.emptyList();
    }

    private void addTile(List<TileKey> list, int[] tileCoord, int dx, int dy)
            throws OutOfProjectionBoundsException {
        if(Math.abs(dx) > radius || Math.abs(dy) > radius) return;
        list.add(new TileKey(tileCoord[0] + dx, tileCoord[1] + dy, relativeZoom));
    }

    @Override
    protected List<PreBakedModel> getPreBakedModels(TileMapService.TmsIdPair<TileKey> idPair) throws IOException, OutOfProjectionBoundsException {
        TileKey tileKey = idPair.getTileId();
        String url = this.getUrlFromTileCoordinate(tileKey.x, tileKey.y, tileKey.relativeZoom);
        InputStream stream = this.fetchData(tileKey, new URL(url));
        if(stream == null) return null;

        BufferedImage image = ImageIO.read(stream);
        GraphicsQuad<GraphicsQuad.PosTexColor> quad = this.computeTileQuad(tileKey);
        return Collections.singletonList(new PreBakedModel(image, Collections.singletonList(quad)));
    }

    @Nullable
    @Override
    protected List<GraphicsQuad<?>> getNonTexturedModel(TileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsQuad<GraphicsQuad.PosTexColor> quad = this.computeTileQuad(tileKey);
        return Collections.singletonList(quad);
    }

    public void setRelativeZoom(int newZoom) {
        if(this.relativeZoom == newZoom) return;
        this.relativeZoom = newZoom;
//        GraphicsModelBaker.getInstance().newQueue();
    }

    public String getUrlFromTileCoordinate(int tileX, int tileY, int relativeZoom) {
        return this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
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
            double[] geoCoord = tileProjection.tileCoordToGeoCoord(tileKey.x + mat[0], tileKey.y + mat[1], tileKey.relativeZoom);
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
