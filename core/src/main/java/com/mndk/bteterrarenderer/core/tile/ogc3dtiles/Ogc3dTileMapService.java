package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.AbstractTileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.core.util.processor.block.ImmediateBlock;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.geoid.GeoidHeightFunction;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidFrustum;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@JsonSerialize(using = Ogc3dTileMapService.Serializer.class)
@JsonDeserialize(using = Ogc3dTileMapService.Deserializer.class)
public class Ogc3dTileMapService extends AbstractTileMapService<TileGlobalKey> {

    private static final Ogc3dTileParsingBlock TILE_PARSER = new Ogc3dTileParsingBlock(
            Executors.newCachedThreadPool(), 3, 100, false);

    @Setter private transient double radius = 40;
    @Setter private transient boolean yDistortion = false;
    @Setter private transient boolean renderSurroundings = false;
    @Setter private transient double lodFactor = 1;
    private transient double magnitude = 1;

    private final URL rootTilesetUrl;
    private final SpheroidCoordinatesConverter coordConverter;
    private final boolean rotateModelAlongEarthXAxis;
    private final String geoidType;

    private transient final TileDataStorage tileDataStorage;
    private transient final ImmediateBlock<TileGlobalKey, TileGlobalKey, Pair<Matrix4f, TileData>> storageFetcher;
    private transient final ImmediateBlock<TileGlobalKey, Pair<Matrix4f, InputStream>, Pair<Matrix4f, TileData>> streamParser;

    @Builder
    @SneakyThrows(MalformedURLException.class)
    private Ogc3dTileMapService(CommonYamlObject commonYamlObject, SpheroidCoordinatesConverter coordConverter,
                                boolean rotateModelAlongEarthXAxis, String geoidType) {
        super(commonYamlObject);
        this.rootTilesetUrl = new URL(commonYamlObject.getTileUrl());
        this.coordConverter = coordConverter;
        this.rotateModelAlongEarthXAxis = rotateModelAlongEarthXAxis;
        this.geoidType = geoidType;

        this.tileDataStorage = new TileDataStorage(new ProcessorCacheStorage<>(1000 * 60 * 10 /* 10 minutes */, 10000, false));
        this.storageFetcher = ImmediateBlock.of((key, input) -> {
            ProcessingState state = tileDataStorage.getResourceProcessingState(input);
            if (state != ProcessingState.PROCESSED) return null;
            return tileDataStorage.updateAndGetOutput(input);
        });
        this.streamParser = ImmediateBlock.of((key, pair) -> {
            TileData tileData = TileResourceManager.parse(pair.getRight(), this.coordConverter);
            return Pair.of(pair.getLeft(), tileData);
        });
    }

    @Override
    public McCoordTransformer getModelPositionTransformer() {
        float yAlign = (float) BTETerraRendererConfig.HOLOGRAM.getYAlign();
        if (!this.yDistortion) {
            McCoord offset = new McCoord(0, yAlign, 0);
            return pos -> pos.add(offset);
        }
        else {
            return pos -> new McCoord(pos.getX(), (float) (pos.getY() * this.magnitude + yAlign), pos.getZ());
        }
    }

    @Override
    protected void preRender(McCoord playerPos) {
        GeographicProjection projection = this.getHologramProjection();
        try {
            // Normally it's good to calculate distortion for individual model vertices...
            // But that significantly drops the fps value (100 -> 4), so I'll put the player position instead.
            // After all the difference wouldn't be noticeable within around 400m from the player.
            // When the distortion calculation becomes fast enough, I'll go back to the original plan.
            double[] geoCoord = projection.toGeo(playerPos.getX(), playerPos.getZ());
            double[] tissot = projection.tissot(geoCoord[0], geoCoord[1]);
            this.magnitude = Math.sqrt(Math.abs(tissot[0]));
        } catch (OutOfProjectionBoundsException ignored) {}
    }

    @Override
    public void moveAlongYAxis(double amount) {
        BTETerraRendererConfig.HOLOGRAM.yAlign += amount;
    }

    @Override
    protected CacheableProcessorModel.SequentialBuilder<TileGlobalKey, TileGlobalKey, List<PreBakedModel>> getModelSequentialBuilder() {
        return new CacheableProcessorModel.SequentialBuilder<>(this.storageFetcher)
                .then(ImmediateBlock.of((key, pair) -> Ogc3dTileParsingBlock.payload(pair.getLeft(), pair.getRight(), this)))
                .then(TILE_PARSER);
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeStateAccessors() {
        PropertyAccessor<Double> radius = PropertyAccessor.ranged(
                this::getRadius, this::setRadius, 1, 1000);
        PropertyAccessor<Double> lodFactor = PropertyAccessor.ranged(
                this::getLodFactor, this::setLodFactor, 0.5, 10);
        PropertyAccessor<Boolean> renderSurroundings = PropertyAccessor.of(
                this::isRenderSurroundings, this::setRenderSurroundings);
        PropertyAccessor<Boolean> yDistortion = PropertyAccessor.of(
                this::isYDistortion, this::setYDistortion);

        return Arrays.asList(
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.3d_radius", radius),
                PropertyAccessor.localized("lod_factor", "gui.bteterrarenderer.settings.lod_factor", lodFactor),
                PropertyAccessor.localized("render_surroundings", "gui.bteterrarenderer.settings.render_surroundings", renderSurroundings),
                PropertyAccessor.localized("y_dist", "gui.bteterrarenderer.settings.y_distortion", yDistortion)
        );
    }

    private Spheroid3 mcCoordToSpheroid(McCoord coord) throws OutOfProjectionBoundsException {
        double[] geo = this.getHologramProjection().toGeo(coord.getX(), coord.getZ());
        return Spheroid3.fromDegrees(geo[0], geo[1], coord.getY());
    }

    @Override
    public List<TileGlobalKey> getRenderTileIdList(McCoord cameraPos, double yawDegrees, double pitchDegrees) {
        if (radius == 0) return Collections.emptyList();

        Cartesian3f point, viewDirection, rightDirection;
        try {
            McCoord mcViewDirection = cameraPos.add(McCoord.fromYawPitch(yawDegrees, pitchDegrees));
            McCoord mcRightDirection = cameraPos.add(McCoord.fromYawPitch(yawDegrees + 90, 0));

            Spheroid3 pointSpheroid = this.mcCoordToSpheroid(cameraPos);
            Spheroid3 viewDirectionSpheroid = this.mcCoordToSpheroid(mcViewDirection);
            Spheroid3 rightDirectionSpheroid = this.mcCoordToSpheroid(mcRightDirection);

            point = this.coordConverter.toCartesian(pointSpheroid);
            viewDirection = this.coordConverter.toCartesian(viewDirectionSpheroid).subtract(point).toNormalized();
            rightDirection = this.coordConverter.toCartesian(rightDirectionSpheroid).subtract(point).toNormalized();
        } catch (OutOfProjectionBoundsException e) { return Collections.emptyList(); }

        SpheroidFrustum frustum = this.getFrustum(point, viewDirection, rightDirection);
        return this.getIdListRecursively(frustum);
    }

    private SpheroidFrustum getFrustum(Cartesian3f point, Cartesian3f viewDirection, Cartesian3f rightDirection) {
        WindowDimension dimension = McConnector.client().getWindowSize();
        float aspectRatio = dimension.getScaledWidth() / (float) dimension.getScaledHeight();
        float verticalFovRadians = (float) Math.toRadians(McConnector.client().getFovDegrees());
        float horizontalFovRadians = 2 * (float) Math.atan(Math.tan(verticalFovRadians / 2) * aspectRatio);
        return new SpheroidFrustum(
                point, viewDirection, rightDirection,
                horizontalFovRadians, verticalFovRadians,
                0f, (float) radius
        );
    }

    @Nullable
    private Tileset getRootTileset() {
        TileGlobalKey key = TileGlobalKey.ROOT_KEY;
        Pair<Matrix4f, TileData> pair = tileDataStorage.updateOrInsert(key, Pair.of(Matrix4f.IDENTITY, this.rootTilesetUrl));
        if (pair == null) return null;

        TileData tileData = pair.getRight();
        if (tileData instanceof Tileset) return (Tileset) tileData;
        Loggers.get(this).warn("Root tile url is not a tile set");
        return null;
    }

    public List<TileGlobalKey> getIdListRecursively(SpheroidFrustum frustum) {
        List<TileGlobalKey> result = new ArrayList<>();

        Stack<Ogc3dBfsNode> nodes = new Stack<>();
        Tileset rootTileset = this.getRootTileset();
        if (rootTileset == null) return Collections.emptyList();
        nodes.add(Ogc3dBfsNode.fromRoot(this, rootTileset, this.rootTilesetUrl));

        while (!nodes.isEmpty()) {

            // Get intersections from the current tileset
            Ogc3dBfsNode node = nodes.pop();
            List<LocalTileNode> intersections = node.selectIntersections(frustum);

            for (LocalTileNode localTileNode : intersections) {
                TileContentLink contentLink = localTileNode.getContentLink();
                Matrix4f currentTransform = localTileNode.getTransform();

                // Skip if the url is malformed
                URL currentUrl;
                try { currentUrl = contentLink.getTrueUrl(node.getParentUrl()); }
                catch (MalformedURLException e) {
                    Loggers.get(this).warn("Malformed URL: {} (parent: {})", contentLink, node.getParentUrl());
                    continue;
                }

                TileLocalKey[] currentKeys = node.attachKey(localTileNode);
                TileGlobalKey currentKey = new TileGlobalKey(currentKeys);

                // Get data from cache
                Pair<Matrix4f, TileData> parsedData = tileDataStorage.updateOrInsert(currentKey,
                        Pair.of(currentTransform, currentUrl));
                if (parsedData == null) continue;

				TileData tileData = parsedData.getRight();
                if (tileData.getGltfModelInstance() != null) {
                    result.add(currentKey);
                }
                if (tileData instanceof Tileset) {
                    Tileset newTileset = (Tileset) tileData;
                    Ogc3dBfsNode newNode = new Ogc3dBfsNode(this, newTileset, currentUrl, currentKeys, currentTransform);
                    nodes.add(newNode);
                }
            }
        }

        return result;
    }

    @Override
    public List<GraphicsModel> getLoadingModel(TileGlobalKey o) {
        return null;
    }

    @Override
    public List<GraphicsModel> getErrorModel(TileGlobalKey o) {
        return null;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.tileDataStorage.close();
    }

    static class Serializer extends TMSSerializer<Ogc3dTileMapService> {
        protected Serializer() {
            super(Ogc3dTileMapService.class);
        }
        @Override
        protected void serializeTMS(Ogc3dTileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumberField("semi_major", value.coordConverter.getSemiMajorAxis());
            gen.writeNumberField("semi_minor", value.coordConverter.getSemiMinorAxis());
            gen.writeBooleanField("rotate_model_x", value.rotateModelAlongEarthXAxis);
            gen.writeStringField("geoid", value.geoidType);
        }
    }

    static class Deserializer extends TMSDeserializer<Ogc3dTileMapService> {
        @Override
        protected Ogc3dTileMapService deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt) throws IOException {
            double semiMajorAxis = JsonParserUtil.getOrDefault(node, "semi_major", Wgs84Constants.SEMI_MAJOR_AXIS);
            double semiMinorAxis = JsonParserUtil.getOrDefault(node, "semi_minor", Wgs84Constants.SEMI_MINOR_AXIS);
            String geoidType = JsonParserUtil.getOrDefault(node, "geoid", "wgs84");
            GeoidHeightFunction function;
            switch (geoidType) {
                case "wgs84": function = GeoidHeightFunction.WGS84_ELLIPSOID; break;
                case "egm96": function = GeoidHeightFunction.EGM96_WW15MGH; break;
                default: throw new IOException("Unknown geoid type: " + geoidType);
            }
            SpheroidCoordinatesConverter coordConverter = new SpheroidCoordinatesConverter(semiMajorAxis, semiMinorAxis, function);

            boolean rotateModelAlongXAxis = JsonParserUtil.getOrDefault(node, "rotate_model_x", false);
            return Ogc3dTileMapService.builder()
                    .commonYamlObject(commonYamlObject)
                    .coordConverter(coordConverter)
                    .rotateModelAlongEarthXAxis(rotateModelAlongXAxis)
                    .geoidType(geoidType)
                    .build();
        }
    }

    private class TileDataStorage extends CacheableProcessorModel<TileGlobalKey, Pair<Matrix4f, URL>, Pair<Matrix4f, TileData>>
            implements Closeable {

        private final ExecutorService executorService;
        private final MultiThreadedBlock<TileGlobalKey, Pair<Matrix4f, URL>, Pair<Matrix4f, InputStream>> tileStreamFetcher;

        protected TileDataStorage(ProcessorCacheStorage<TileGlobalKey, Pair<Matrix4f, TileData>> storage) {
            super(storage);

            this.executorService = Executors.newFixedThreadPool(Ogc3dTileMapService.this.nThreads);
            this.tileStreamFetcher = MultiThreadedBlock.of((key, pair) -> {
                ByteBuf buf = HttpResourceManager.download(pair.getRight().toString());
                return Pair.of(pair.getLeft(), new ByteBufInputStream(buf));
            }, this.executorService, 3, 200, true);
        }

        @Override
        protected SequentialBuilder<TileGlobalKey, Pair<Matrix4f, URL>, Pair<Matrix4f, TileData>> getSequentialBuilder() {
            return new CacheableProcessorModel.SequentialBuilder<>(this.tileStreamFetcher)
                    .then(Ogc3dTileMapService.this.streamParser);
        }

        @Override
        protected void deleteResource(Pair<Matrix4f, TileData> parsedData) {}

        @Override
        public void close() {
            this.executorService.shutdownNow();
        }
    }
}
