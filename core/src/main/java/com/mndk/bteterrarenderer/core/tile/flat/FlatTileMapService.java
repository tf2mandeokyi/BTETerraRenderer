package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.ImageTexturePair;
import com.mndk.bteterrarenderer.util.concurrent.ManualThreadExecutor;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.AbstractTileMapService;
import com.mndk.bteterrarenderer.util.concurrent.MappedExecutors;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.*;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Getter
@JsonSerialize(using = FlatTileMapService.Serializer.class)
@JsonDeserialize(using = FlatTileMapService.Deserializer.class)
public class FlatTileMapService extends AbstractTileMapService<FlatTileKey> {

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;
    public static final int DEFAULT_ZOOM = 18;

    private static final ImageTexturePair SOMETHING_WENT_WRONG, LOADING;

    private transient int relativeZoom = 0;
    @Setter private transient int radius = 3;

    private final String urlTemplate;
    private final FlatTileCoordTranslator coordTranslator;
    private final FlatTileURLConverter urlConverter;

    private transient final MappedExecutors<Integer> imageFetcher;
    // This is to avoid quirky concurrent thingy
    private transient final ManualThreadExecutor imageToPreModel;

    @Builder
    private FlatTileMapService(CommonYamlObject commonYamlObject,
                               FlatTileCoordTranslator coordTranslator,
                               FlatTileURLConverter urlConverter) {
        super(commonYamlObject);
        this.urlTemplate = commonYamlObject.getTileUrl();
        this.coordTranslator = coordTranslator;
        this.urlConverter = urlConverter;

        this.imageFetcher = new MappedExecutors<>(Executors.newFixedThreadPool(this.getNThreads()), this.relativeZoom);
        this.imageToPreModel = new ManualThreadExecutor();
    }

    private static float getFlatMapYAxis() {
        return (float) BTETerraRendererConfig.HOLOGRAM.getFlatMapYAxis();
    }

    @Override
    protected McFXElement makeHudElement() {
        return null;
    }

    @Override
    public McCoordTransformer getModelPositionTransformer() {
        float yAlign = (float) (getFlatMapYAxis() + Y_EPSILON);
        return pos -> pos.add(new McCoord(0, yAlign, 0));
    }

    @Override
    public VertexBeginner getVertexBeginner(BufferBuildersManager manager, float opacity) {
        return new VertexBeginner() {
            public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture) {
                return manager.begin3dQuad(texture, opacity, false);
            }
            public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture) {
                throw new UnsupportedOperationException("Triangles are not supported in FlatTileMapService");
            }
        };
    }

    @Override
    protected void preRender(McCoord playerPos) {
        this.imageToPreModel.process(2);
        SOMETHING_WENT_WRONG.bake();
        LOADING.bake();
    }

    @Nullable
    @Override
    protected CompletableFuture<List<PreBakedModel>> processModel(FlatTileKey tileId) {
        String url = this.urlConverter.convertToUrl(this.urlTemplate, tileId);
        GraphicsShapes shapes;
        try { shapes = this.computeTileQuad(tileId); }
        catch (OutOfProjectionBoundsException e) { return CompletableFuture.completedFuture(Collections.emptyList()); }

        return CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage image = HttpResourceManager.downloadAsImage(url, this.getNThreads()).get();
                return Collections.singletonList(new PreBakedModel(image, shapes));
            } catch (Exception e) {
                throw new RuntimeException("Failed to download image from " + url, e);
            }
            }, this.imageFetcher.getExecutor(tileId.relativeZoom));
    }

    @Override
    public void moveAlongYAxis(double amount) {
        BTETerraRendererConfig.HOLOGRAM.flatMapYAxis += amount;
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeStateAccessors() {
        PropertyAccessor<Integer> zoom = PropertyAccessor.ranged(this::getRelativeZoom, this::setRelativeZoom, this::isRelativeZoomAvailable, -4, 4);
        PropertyAccessor<Integer> radius =  PropertyAccessor.ranged(this::getRadius, this::setRadius, 1, 10);

        return Arrays.asList(
                PropertyAccessor.localized("zoom", "gui.bteterrarenderer.settings.zoom", zoom),
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.size", radius)
        );
    }

    @Override
    public List<FlatTileKey> getRenderTileIdList(McCoord cameraPos, double yawDegrees, double pitchDegrees) {
        if (this.coordTranslator == null) return Collections.emptyList();

        double yDiff = getFlatMapYAxis() - cameraPos.getY();
        if (Math.abs(yDiff) >= BTETerraRendererConfig.HOLOGRAM.getYDiffLimit()) return Collections.emptyList();

        try {
            List<FlatTileKey> result = new ArrayList<>();
            double[] geo = this.getHologramProjection().toGeo(cameraPos.getX(), cameraPos.getZ());
            int[] tileCoord = this.coordTranslator.geoCoordToTileCoord(geo[0], geo[1], relativeZoom);

            // Diamond pattern
            for (int i = 0; i < 2 * this.radius + 1; ++i) {
                if (i == 0) {
                    this.addTile(result, tileCoord, 0, 0);
                }
                for (int j = 0; j < i; ++j) {
                    this.addTile(result, tileCoord, -j, j - i);
                    this.addTile(result, tileCoord, j - i, +j);
                    this.addTile(result, tileCoord, j, i - j);
                    this.addTile(result, tileCoord, i - j, -j);
                }
            }

            return result;

        } catch (OutOfProjectionBoundsException ignored) {
        } catch (Exception e) {
            Loggers.get(this).warn("Caught exception while rendering tile images", e);
        }
        return Collections.emptyList();
    }

    private void addTile(List<FlatTileKey> list, int[] tileCoord, int dx, int dy) {
        if (Math.abs(dx) > radius || Math.abs(dy) > radius) return;
        list.add(new FlatTileKey(tileCoord[0] + dx, tileCoord[1] + dy, relativeZoom));
    }

    @Override
    public List<GraphicsModel> getLoadingModel(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsShapes shapes = this.computeTileQuad(tileKey);
        return Collections.singletonList(new GraphicsModel(LOADING.getTextureObject(), shapes));
    }

    @Override
    public List<GraphicsModel> getErrorModel(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        GraphicsShapes shapes = this.computeTileQuad(tileKey);
        return Collections.singletonList(new GraphicsModel(SOMETHING_WENT_WRONG.getTextureObject(), shapes));
    }

    public void setRelativeZoom(int newZoom) {
        if (this.relativeZoom == newZoom) return;
        this.relativeZoom = newZoom;
        this.imageFetcher.setCurrentKey(newZoom);
    }

    private GraphicsShapes computeTileQuad(FlatTileKey tileKey) throws OutOfProjectionBoundsException {
        /*
         *  i=0 ------ i=1
         *   |          |
         *   |   TILE   |
         *   |          |
         *  i=3 ------ i=2
         */
        McCoord[] corners = new McCoord[4];
        float[] texCoords = new float[8];
        for (int i = 0; i < 4; i++) {
            int[] mat = this.coordTranslator.getCornerMatrix(i);
            double[] geoCoord = this.coordTranslator.tileCoordToGeoCoord(tileKey.x + mat[0], tileKey.y + mat[1], tileKey.relativeZoom);
            double[] gameCoord = this.getHologramProjection().fromGeo(geoCoord[0], geoCoord[1]);

            corners[i] = new McCoord(gameCoord[0], 0, gameCoord[1]);
            texCoords[i * 2] = mat[2];
            texCoords[i * 2 + 1] = mat[3];
        }

        GraphicsQuad<PosTex> quad = new GraphicsQuad<>(
                new PosTex(corners[0], texCoords[0], texCoords[1]),
                new PosTex(corners[1], texCoords[2], texCoords[3]),
                new PosTex(corners[2], texCoords[4], texCoords[5]),
                new PosTex(corners[3], texCoords[6], texCoords[7])
        );
        GraphicsShapes shapes = new GraphicsShapes();
        shapes.add(DrawingFormat.QUAD_PT, quad);
        return shapes;
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return this.coordTranslator != null && this.coordTranslator.isRelativeZoomAvailable(relativeZoom);
    }

    static class Serializer extends TMSSerializer<FlatTileMapService> {
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
            if (projection.getName() != null) {
                gen.writeStringField("projection", projection.getName());
            } else {
                gen.writeObjectField("projection", projection);
            }
        }
    }

    static class Deserializer extends TMSDeserializer<FlatTileMapService> {
        @Override
        protected FlatTileMapService deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt) throws IOException {
            int defaultZoom = JsonParserUtil.getOrDefault(node, "default_zoom", DEFAULT_ZOOM);
            boolean invertZoom = JsonParserUtil.getOrDefault(node, "invert_zoom", false);
            boolean invertLatitude = JsonParserUtil.getOrDefault(node, "invert_lat", false);
            boolean flipVertically = JsonParserUtil.getOrDefault(node, "flip_vert", false);

            // Get projection
            FlatTileProjection projection = ConfigLoaders.flatProj().get(node.get("projection"));

            // Modify projection
            FlatTileCoordTranslator coordTranslator = new FlatTileCoordTranslator(projection)
                    .setDefaultZoom(defaultZoom)
                    .setInvertZoom(invertZoom)
                    .setInvertLatitude(invertLatitude)
                    .setFlipVertically(flipVertically);

            return FlatTileMapService.builder()
                    .commonYamlObject(commonYamlObject)
                    .coordTranslator(coordTranslator)
                    .urlConverter(new FlatTileURLConverter(defaultZoom, invertZoom))
                    .build();
        }
    }

    static {
        try {
            ClassLoader loader = AbstractTileMapService.class.getClassLoader();

            String errorImagePath = "assets/" + BTETerraRenderer.MODID + "/textures/internal_error.png";
            InputStream errorImageStream = loader.getResourceAsStream(errorImagePath);
            SOMETHING_WENT_WRONG = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(errorImageStream)));
            errorImageStream.close();

            String loadingImagePath = "assets/" + BTETerraRenderer.MODID + "/textures/loading.png";
            InputStream loadingImageStream = loader.getResourceAsStream(loadingImagePath);
            LOADING = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(loadingImageStream)));
            loadingImageStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
