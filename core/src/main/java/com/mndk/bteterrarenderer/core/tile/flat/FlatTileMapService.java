package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.ImageTexturePair;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.loader.yml.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.TMSIdPair;
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
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Getter
@JsonSerialize(using = FlatTileMapService.Serializer.class)
@JsonDeserialize(using = FlatTileMapService.Deserializer.class)
public class FlatTileMapService extends TileMapService<FlatTileKey> {

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;
    public static final int DEFAULT_ZOOM = 18;

    private static final ImageTexturePair SOMETHING_WENT_WRONG, LOADING;
    private static boolean STATIC_IMAGES_BAKED = false;

    private transient int relativeZoom = 0;
    @Setter
    private transient int radius = 3;

    private final String urlTemplate;
    private final FlatTileCoordTranslator coordTranslator;
    private final FlatTileURLConverter urlConverter;

    private transient final ImmediateBlock<TMSIdPair<FlatTileKey>, FlatTileKey, String> tileKeyToUrl;
    private transient final MappedQueueBlock<TMSIdPair<FlatTileKey>, Integer, String, ByteBuf> imageFetcher;
    private transient final ImmediateBlock<TMSIdPair<FlatTileKey>, ByteBuf, BufferedImage> byteBufToImage;
    // This is to avoid quirky concurrent thingy
    private transient final SingleQueueBlock<TMSIdPair<FlatTileKey>, BufferedImage, PreBakedModel> imageToPreModel;

    @JsonCreator
    private FlatTileMapService(CommonYamlObject commonYamlObject,
                               FlatTileCoordTranslator coordTranslator,
                               FlatTileURLConverter urlConverter) {
        super(commonYamlObject);
        this.urlTemplate = commonYamlObject.getTileUrl();
        this.coordTranslator = coordTranslator;
        this.urlConverter = urlConverter;

        this.tileKeyToUrl = ImmediateBlock.of((key, input) -> this.getUrlFromTileCoordinate(input));
        this.imageFetcher = new FlatTileResourceDownloadingBlock(this.nThreads, 3, true);
        this.byteBufToImage = ImmediateBlock.of((key, input) -> ImageIO.read(new ByteBufInputStream(input)));
        this.imageToPreModel = SingleQueueBlock.of((key, image) -> new PreBakedModel(image, this.computeTileQuad(key.getRight())));
        this.imageFetcher.setQueueKey(0);
    }

    @Override
    protected PositionTransformer getPositionTransformer(double px, double py, double pz) {
        double yAlign = BTETerraRendererConfig.HOLOGRAM.getFlatMapYAxis() + Y_EPSILON;
        return (x, y, z) -> new double[] { x - px, y - py + yAlign, z - pz };
    }

    @Override
    protected void preRender(double px, double py, double pz) {
        this.imageToPreModel.process(2);
        if(!STATIC_IMAGES_BAKED) {
            SOMETHING_WENT_WRONG.bake();
            LOADING.bake();
            STATIC_IMAGES_BAKED = true;
        }
    }

    @Override
    public void moveAlongYAxis(double amount) {
        BTETerraRendererConfig.HOLOGRAM.flatMapYAxis += amount;
    }

    @Override
    protected CacheableProcessorModel.SequentialBuilder<TMSIdPair<FlatTileKey>, FlatTileKey, List<PreBakedModel>> getModelSequentialBuilder() {
        return new CacheableProcessorModel.SequentialBuilder<>(this.tileKeyToUrl)
                .then(this.imageFetcher)
                .then(this.byteBufToImage)
                .then(this.imageToPreModel)
                .then(ImmediateBlock.singletonList());
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
        if(this.coordTranslator == null) return Collections.emptyList();

        try {
            List<FlatTileKey> result = new ArrayList<>();
            int[] tileCoord = this.coordTranslator.geoCoordToTileCoord(longitude, latitude, relativeZoom);

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

    @Override
    protected List<GraphicsModel> getLoadingModel(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsShapes shapes = this.computeTileQuad(tileKey);
        return Collections.singletonList(new GraphicsModel(LOADING.getTextureObject(), shapes));
    }

    @Override
    protected List<GraphicsModel> getErrorModel(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsShapes shapes = this.computeTileQuad(tileKey);
        return Collections.singletonList(new GraphicsModel(SOMETHING_WENT_WRONG.getTextureObject(), shapes));
    }

    public void setRelativeZoom(int newZoom) {
        if(this.relativeZoom == newZoom) return;
        this.relativeZoom = newZoom;
        this.imageFetcher.setQueueKey(newZoom);
    }

    public String getUrlFromTileCoordinate(FlatTileKey flatTileKey) {
        return this.urlConverter.convertToUrl(this.urlTemplate, flatTileKey);
    }

    private GraphicsShapes computeTileQuad(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        /*
         *  i=0 ------ i=1
         *   |          |
         *   |   TILE   |
         *   |          |
         *  i=3 ------ i=2
         */
        GraphicsQuad<PosTex> quad = GraphicsQuad.newPosTex();
        for (int i = 0; i < 4; i++) {
            int[] mat = this.coordTranslator.getCornerMatrix(i);
            double[] geoCoord = this.coordTranslator.tileCoordToGeoCoord(tileKey.x + mat[0], tileKey.y + mat[1], tileKey.relativeZoom);
            double[] gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

            quad.setVertex(i, new PosTex(
                    gameCoord[0], 0, gameCoord[1], // position
                    mat[2], mat[3] // texture coordinate
            ));
        }

        GraphicsShapes shapes = new GraphicsShapes();
        shapes.add(DrawingFormat.QUAD_PT_ALPHA, quad);
        return shapes;
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return this.coordTranslator != null && this.coordTranslator.isRelativeZoomAvailable(relativeZoom);
    }

    public static class Serializer extends TMSSerializer<FlatTileMapService> {
        protected Serializer() {
            super(FlatTileMapService.class);
        }

        @Override
        public void serializeTMS(FlatTileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            FlatTileCoordTranslator translator = value.getCoordTranslator();
            gen.writeNumberField("default_zoom", translator.getDefaultZoom());
            gen.writeBooleanField("invert_zoom", translator.isInvertZoom());
            gen.writeBooleanField("invert_lat", translator.isInvertLatitude());
            gen.writeBooleanField("flip_vert", translator.isFlipVertically());

            FlatTileProjection projection = translator.getProjection();
            if(projection.getName() != null) {
                gen.writeStringField("projection", projection.getName());
            }
            else {
                gen.writeObjectField("projection", projection);
            }
        }
    }

    public static class Deserializer extends TMSDeserializer<FlatTileMapService> {
        @Override
        protected FlatTileMapService deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt) throws IOException {
            int defaultZoom = JsonParserUtil.getOrDefault(node, "default_zoom", DEFAULT_ZOOM);
            boolean invertZoom = JsonParserUtil.getOrDefault(node, "invert_zoom", false);
            boolean invertLatitude = JsonParserUtil.getOrDefault(node, "invert_lat", false);
            boolean flipVertically = JsonParserUtil.getOrDefault(node, "flip_vert", false);

            // Get projection
            JsonNode projectionNode = node.get("projection");
            FlatTileProjection projection;
            if(projectionNode.isTextual()) {
                String projectionName = projectionNode.asText();
                projection = FlatTileProjectionYamlLoader.INSTANCE.getResult().get(projectionName);
                if(projection == null) {
                    throw JsonMappingException.from(ctxt, "unknown projection name: " + projectionName);
                }
            }
            else if(projectionNode.isObject()) {
                // Do not set projection name for this anonymous value.
                projection = ctxt.readTreeAsValue(node, FlatTileProjectionImpl.class);
            }
            else throw JsonMappingException.from(ctxt, "projection should be an object or a name");

            // Modify projection
            FlatTileCoordTranslator coordTranslator = new FlatTileCoordTranslator(projection)
                    .setDefaultZoom(defaultZoom)
                    .setInvertZoom(invertZoom)
                    .setFlipVertically(flipVertically)
                    .setInvertLatitude(invertLatitude);

            FlatTileURLConverter urlConverter = new FlatTileURLConverter(defaultZoom, invertZoom);
            return new FlatTileMapService(commonYamlObject, coordTranslator, urlConverter);
        }
    }

    static {
        try {
            ClassLoader loader = TileMapService.class.getClassLoader();

            String errorImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error.png";
            InputStream errorImageStream = loader.getResourceAsStream(errorImagePath);
            SOMETHING_WENT_WRONG = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(errorImageStream)));
            errorImageStream.close();

            String loadingImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/loading.png";
            InputStream loadingImageStream = loader.getResourceAsStream(loadingImagePath);
            LOADING = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(loadingImageStream)));
            loadingImageStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
