package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.loader.yml.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.AbstractModelMaker;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedIntPropertyAccessor;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.block.ImmediateBlock;
import com.mndk.bteterrarenderer.core.util.processor.block.MappedQueueBlock;
import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosTex;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@ToString(callSuper = true)
@JsonDeserialize(using = FlatTileMapService.Deserializer.class)
public class FlatTileMapService extends TileMapService<FlatTileKey> {

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
    private final FlatTileProjection flatTileProjection;
    private final FlatTileURLConverter urlConverter;
    private final ModelMaker modelMaker;

    @JsonCreator
    private FlatTileMapService(CommonYamlObject commonYamlObject,
                               FlatTileProjection flatTileProjection, FlatTileURLConverter urlConverter) {
        super(commonYamlObject);
        this.urlTemplate = commonYamlObject.getTileUrl();
        this.flatTileProjection = flatTileProjection;
        this.urlConverter = urlConverter;
        this.modelMaker = new ModelMaker(1000 * 60 * 5 /* cacheExpireMilliseconds = 5 minutes */, 10000, false);
        this.modelMaker.imageFetcher.setQueueKey(0);
    }

    @Override
    protected double getYAlign() {
        return BTETerraRendererConfig.HOLOGRAM.getFlatMapYAxis() + Y_EPSILON;
    }

    @Override
    protected void preRender(double px, double py, double pz) {
        this.modelMaker.imageToPreModel.process(2);
    }

    @Override
    protected AbstractModelMaker<FlatTileKey> getModelMaker() {
        return this.modelMaker;
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeProperties() {
        PropertyAccessor<Integer> zoomProperty = RangedIntPropertyAccessor.of(
                this::getRelativeZoom, this::setRelativeZoom, this::isRelativeZoomAvailable, -4, 4);
        PropertyAccessor<Integer> radiusProperty =  RangedIntPropertyAccessor.of(
                this::getRadius, this::setRadius, 1, 10);

        return Arrays.asList(
                PropertyAccessor.localized("zoom", "gui.bteterrarenderer.settings.zoom", zoomProperty),
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.size", radiusProperty)
        );
    }

    @Override
    protected List<FlatTileKey> getRenderTileIdList(double longitude, double latitude, double height) {
        if(this.flatTileProjection == null) return Collections.emptyList();

        try {
            List<FlatTileKey> result = new ArrayList<>();
            int[] tileCoord = this.flatTileProjection.geoCoordToTileCoord(longitude, latitude, relativeZoom);

            // Diamond pattern
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
            Loggers.get(this).warn("Caught exception while rendering tile images", e);
        }
        return Collections.emptyList();
    }

    private void addTile(List<FlatTileKey> list, int[] tileCoord, int dx, int dy) {
        if(Math.abs(dx) > radius || Math.abs(dy) > radius) return;
        list.add(new FlatTileKey(tileCoord[0] + dx, tileCoord[1] + dy, relativeZoom));
    }

    @Nullable
    @Override
    protected List<GraphicsShape<?>> getNonTexturedModel(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsQuad<PosTex> quad = this.computeTileQuad(tileKey);
        return Collections.singletonList(quad);
    }

    public void setRelativeZoom(int newZoom) {
        if(this.relativeZoom == newZoom) return;
        this.relativeZoom = newZoom;
        this.modelMaker.imageFetcher.setQueueKey(newZoom);
    }

    public String getUrlFromTileCoordinate(FlatTileKey flatTileKey) {
        return this.urlConverter.convertToUrl(this.urlTemplate, flatTileKey);
    }

    private GraphicsQuad<PosTex> computeTileQuad(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        /*
         *  i=0 ------ i=1
         *   |          |
         *   |   TILE   |
         *   |          |
         *  i=3 ------ i=2
         */
        GraphicsQuad<PosTex> quad = GraphicsQuad.newPosTex();
        for (int i = 0; i < 4; i++) {
            int[] mat = this.flatTileProjection.getCornerMatrix(i);
            double[] geoCoord = flatTileProjection.tileCoordToGeoCoord(tileKey.x + mat[0], tileKey.y + mat[1], tileKey.relativeZoom);
            double[] gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

            quad.setVertex(i, new PosTex(
                    gameCoord[0], 0, gameCoord[1], // position
                    mat[2], mat[3] // texture coordinate
            ));
        }
        return quad;
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return flatTileProjection != null && flatTileProjection.isRelativeZoomAvailable(relativeZoom);
    }

    @Override
    public void close() {
        this.modelMaker.close();
    }

    public static class Deserializer extends JsonDeserializer<FlatTileMapService> {
        @Override
        public FlatTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            CommonYamlObject commonYamlObject = CommonYamlObject.from(node);
            int defaultZoom = JsonParserUtil.getOrDefault(node, "default_zoom", DEFAULT_ZOOM);
            boolean invertZoom = JsonParserUtil.getOrDefault(node, "invert_zoom", false);
            boolean invertLatitude = JsonParserUtil.getOrDefault(node, "invert_lat", false);
            boolean flipVertically = JsonParserUtil.getOrDefault(node, "flip_vert", false);

            String projectionName = node.get("projection").asText();
            FlatTileProjection projection = FlatTileProjectionYamlLoader.INSTANCE.getResult().get(projectionName);
            if(projection != null) {
                projection = projection.clone()
                        .setDefaultZoom(defaultZoom)
                        .setInvertZoom(invertZoom)
                        .setFlipVertically(flipVertically)
                        .setInvertLatitude(invertLatitude);
            } else {
                // If the projection corresponding to its id does not exist
                throw JsonMappingException.from(p, "unknown projection name: " + projectionName);
            }

            FlatTileURLConverter urlConverter = new FlatTileURLConverter(defaultZoom, invertZoom);
            return new FlatTileMapService(commonYamlObject, projection, urlConverter);
        }
    }

    private class ModelMaker extends AbstractModelMaker<FlatTileKey> implements Closeable {

        private final ImmediateBlock<FlatTileKey, FlatTileKey, String> tileKeyToUrl;
        private final MappedQueueBlock<FlatTileKey, Integer, String, ByteBuf> imageFetcher;
        private final ImmediateBlock<FlatTileKey, ByteBuf, BufferedImage> byteBufToImage;
        // This is to avoid quirky concurrent thingy
        private final SingleQueueBlock<FlatTileKey, BufferedImage, PreBakedModel> imageToPreModel;

        /**
         * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
         * @param maximumSize        Maximum cache size. Set to -1 for no limits
         * @param debug              debug
         */
        protected ModelMaker(long expireMilliseconds, int maximumSize, boolean debug) {
            super(expireMilliseconds, maximumSize, debug);
            this.tileKeyToUrl = ImmediateBlock.of((key, input) -> getUrlFromTileCoordinate(input));
            this.imageFetcher = new FlatTileResourceDownloadingBlock(FlatTileMapService.this.nThreads, 3);
            this.byteBufToImage = ImmediateBlock.of((key, input) -> ImageIO.read(new ByteBufInputStream(input)));
            this.imageToPreModel = SingleQueueBlock.of((key, image) -> new PreBakedModel(image, computeTileQuad(key)));
        }

        @Override
        protected SequentialBuilder<FlatTileKey, FlatTileKey, List<GraphicsModel>> getSequentialBuilder() {
            return new CacheableProcessorModel.SequentialBuilder<>(this.tileKeyToUrl)
                    .then(this.imageFetcher)
                    .then(this.byteBufToImage)
                    .then(this.imageToPreModel)
                    .then(ImmediateBlock.singletonList())
                    .then(FlatTileMapService.this.modelTextureBaker);
        }

        @Override
        public void close() {
            this.imageFetcher.close();
        }
    }
}
