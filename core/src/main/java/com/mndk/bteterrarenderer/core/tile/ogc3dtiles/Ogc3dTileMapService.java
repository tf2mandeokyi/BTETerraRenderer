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
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.AbstractTileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileKeyManager;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.core.util.processor.block.ImmediateBlock;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    private static final ImmediateBlock<TileGlobalKey, Pair<Matrix4, InputStream>, ParsedData> STREAM_PARSER = ImmediateBlock.of((key, pair) -> {
        TileData tileData = TileResourceManager.parse(pair.getRight());
        return new ParsedData(pair.getLeft(), tileData);
    });
    private static final Ogc3dTileParsingBlock TILE_PARSER = new Ogc3dTileParsingBlock(
            Executors.newCachedThreadPool(), 3, 100, false);

    @Setter
    private transient double radius = 40;
    private transient double magnitude = 1;
    @Setter
    private transient boolean yDistortion = false;
    private final URL rootTilesetUrl;

    private transient final TileDataStorage tileDataStorage;
    private transient final ImmediateBlock<TileGlobalKey, TileGlobalKey, ParsedData> storageFetcher;

    private Ogc3dTileMapService(CommonYamlObject commonYamlObject) {
        super(commonYamlObject);
        this.tileDataStorage = new TileDataStorage(new ProcessorCacheStorage<>(1000 * 60 * 10 /* 10 minutes */, 10000, false));
        this.storageFetcher = ImmediateBlock.of((key, input) -> {
            ProcessingState state = tileDataStorage.getResourceProcessingState(input);
            if(state != ProcessingState.PROCESSED) return null;
            return tileDataStorage.updateAndGetOutput(input);
        });

        try {
            this.rootTilesetUrl = new URL(commonYamlObject.getTileUrl());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PositionTransformer getPositionTransformer(double px, double py, double pz) {
        double yAlign = BTETerraRendererConfig.HOLOGRAM.getYAlign();
        if(!this.yDistortion) {
            return (x, y, z) -> new double[] { x - px, yAlign + y - py, z - pz };
        }
        else {
            return (x, y, z) -> new double[] { x - px, yAlign + y * this.magnitude - py, z - pz };
        }
    }

    @Override
    protected void preRender(double px, double py, double pz) {
        GeographicProjection projection = Projections.getServerProjection();
        if(projection == null) return;

        try {
            // Normally it's good to calculate distortion for individual model vertices...
            // But that significantly drops the fps value (100 -> 4), so I'll put the player position instead.
            // After all the difference wouldn't be noticeable within around 400m from the player.
            // When the distortion calculation becomes fast enough, I'll go back to the original plan.
            double[] geoCoord = projection.toGeo(px, pz);
            double[] tissot = projection.tissot(geoCoord[0], geoCoord[1]);
            this.magnitude = Math.sqrt(Math.abs(tissot[0]));
        } catch(OutOfProjectionBoundsException ignored) {}
    }

    @Override
    public void moveAlongYAxis(double amount) {
        BTETerraRendererConfig.HOLOGRAM.yAlign += amount;
    }

    @Override
    protected CacheableProcessorModel.SequentialBuilder<TileGlobalKey, TileGlobalKey, List<PreBakedModel>> getModelSequentialBuilder() {
        return new CacheableProcessorModel.SequentialBuilder<>(this.storageFetcher).then(TILE_PARSER);
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeProperties() {
        PropertyAccessor<Double> radiusProperty = RangedDoublePropertyAccessor.of(
                this::getRadius, this::setRadius, 1, 1000);
        PropertyAccessor<Boolean> yDistortionProperty = PropertyAccessor.of(
                this::isYDistortion, this::setYDistortion);

        return Arrays.asList(
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.3d_radius", radiusProperty),
                PropertyAccessor.localized("y_dist", "gui.bteterrarenderer.settings.y_distortion", yDistortionProperty)
        );
    }

    @Override
    protected List<TileGlobalKey> getRenderTileIdList(double longitude, double latitude, double height) {
        if(radius == 0) return Collections.emptyList();

        Cartesian3 cartesian = new Spheroid3(Math.toRadians(longitude), Math.toRadians(latitude), height)
                .toCartesianCoordinate();
        Sphere playerSphere = new Sphere(cartesian, radius);
        return this.getIdListRecursively(playerSphere);
    }

    @Nullable
    private Tileset getRootTileset() {
        TileGlobalKey key = TileGlobalKey.ROOT_KEY;
        ParsedData parsedData = tileDataStorage.updateOrInsert(key, () -> Pair.of(Matrix4.IDENTITY, this.rootTilesetUrl));
        if(parsedData == null) return null;

        TileData tileData = parsedData.getTileData();
        return tileData instanceof Tileset ? (Tileset) tileData : null;
    }

    public List<TileGlobalKey> getIdListRecursively(Sphere playerSphere) {
        List<TileGlobalKey> result = new ArrayList<>();

        @RequiredArgsConstructor
        class Node {
            final Tileset tileset;
            final URL parentUrl;
            final TileLocalKey[] parentKeys;
            final Matrix4 parentTransform;
        }

        Stack<Node> nodes = new Stack<>();
        Tileset rootTileset = this.getRootTileset();
        if(rootTileset == null) return Collections.emptyList();
        nodes.add(new Node(rootTileset, this.rootTilesetUrl, new TileLocalKey[0], Matrix4.IDENTITY));

        do {
            Node node = nodes.pop();
            Tileset currentTileset = node.tileset;
            URL parentUrl = node.parentUrl;
            TileLocalKey[] parentKeys = node.parentKeys;
            Matrix4 parentTransform = node.parentTransform;

            // Get intersections from the current tileset
            List<LocalTileNode> intersections =
                    TileKeyManager.getIntersectionsFromTileset(currentTileset, playerSphere, parentTransform);

            for (LocalTileNode localTileNode : intersections) {
                TileLocalKey localKey = localTileNode.getKey();
                TileContentLink contentLink = localTileNode.getContentLink();
                Matrix4 currentTransform = localTileNode.getTransform();

                // Skip if the url is malformed
                URL currentUrl;
                try {
                    currentUrl = contentLink.getTrueUrl(parentUrl);
                } catch(MalformedURLException e) { continue; }

                TileLocalKey[] currentKeys = ArrayUtil.expandOne(parentKeys, localKey, TileLocalKey[]::new);
                TileGlobalKey currentKey = new TileGlobalKey(currentKeys);

                // Get data from cache
                ParsedData parsedData = tileDataStorage.updateOrInsert(currentKey, () -> Pair.of(currentTransform, currentUrl));
                if(parsedData == null) continue;

				TileData tileData = parsedData.getTileData();
                if(tileData.getGltfModelInstance() != null) {
                    result.add(currentKey);
                }
                if(tileData instanceof Tileset) {
                    Tileset newTileset = (Tileset) tileData;
                    nodes.add(new Node(newTileset, currentUrl, currentKeys, currentTransform));
                }
            }
        } while(!nodes.isEmpty());

        return result;
    }

    @Override
    protected List<GraphicsModel> getLoadingModel(TileGlobalKey o) {
        return null;
    }

    @Override
    protected List<GraphicsModel> getErrorModel(TileGlobalKey o) {
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
        protected void serializeTMS(Ogc3dTileMapService value, JsonGenerator gen, SerializerProvider serializers) {}
    }

    static class Deserializer extends TMSDeserializer<Ogc3dTileMapService> {
        @Override
        protected Ogc3dTileMapService deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt) throws IOException {
            return new Ogc3dTileMapService(commonYamlObject);
        }
    }

    private class TileDataStorage extends CacheableProcessorModel<TileGlobalKey, Pair<Matrix4, URL>, ParsedData>
            implements Closeable {

        private final ExecutorService executorService;
        private final MultiThreadedBlock<TileGlobalKey, Pair<Matrix4, URL>, Pair<Matrix4, InputStream>> tileStreamFetcher;

        protected TileDataStorage(ProcessorCacheStorage<TileGlobalKey, ParsedData> storage) {
            super(storage);

            this.executorService = Executors.newFixedThreadPool(Ogc3dTileMapService.this.nThreads);
            this.tileStreamFetcher = MultiThreadedBlock.of((key, pair) -> {
                ByteBuf buf = HttpResourceManager.download(pair.getRight().toString());
                return Pair.of(pair.getLeft(), new ByteBufInputStream(buf));
            }, this.executorService, 3, 200, true);
        }

        @Override
        protected SequentialBuilder<TileGlobalKey, Pair<Matrix4, URL>, ParsedData> getSequentialBuilder() {
            return new CacheableProcessorModel.SequentialBuilder<>(this.tileStreamFetcher)
                    .then(STREAM_PARSER);
        }

        @Override
        protected void deleteResource(ParsedData parsedData) {}

        @Override
        public void close() {
            this.executorService.shutdownNow();
        }
    }
}
