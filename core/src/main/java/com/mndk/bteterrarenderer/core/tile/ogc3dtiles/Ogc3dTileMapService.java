package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.AbstractModelMaker;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileKeyManager;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.core.util.processor.block.ImmediateBlock;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.mcconnector.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.i3dm.Instanced3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = Ogc3dTileMapService.Deserializer.class)
public class Ogc3dTileMapService extends TileMapService<TileGlobalKey> {

    private static final ImmediateBlock<TileGlobalKey, Pair<Matrix4, InputStream>, ParsedData> STREAM_PARSER = ImmediateBlock.of((key, pair) -> {
        TileData tileData = TileResourceManager.parse(pair.getRight());
        return new ParsedData(pair.getLeft(), tileData);
    });
    private static final Ogc3dTileParsingBlock<TileGlobalKey> TILE_PARSER = new Ogc3dTileParsingBlock<>(Executors.newCachedThreadPool());

    @Setter
    private transient double radius = 40;
    private final URL rootTilesetUrl;
    private transient final TileDataStorage cacheStorage;
    private transient final ModelMaker modelMaker;

    private Ogc3dTileMapService(CommonYamlObject commonYamlObject) {
        super(commonYamlObject);
        this.cacheStorage = new TileDataStorage(1000 * 60 * 10 /* 10 minutes */, 10000, false);
        this.modelMaker = new ModelMaker(1000 * 60 * 10 /* 10 minutes */, 10000, false);
        try {
            this.rootTilesetUrl = new URL(commonYamlObject.getTileUrl());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected AbstractModelMaker<TileGlobalKey> getModelMaker() {
        return this.modelMaker;
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeProperties() {
        PropertyAccessor<Double> radiusProperty = RangedDoublePropertyAccessor.of(
                this::getRadius, this::setRadius, 1, 1000);
        return Collections.singletonList(
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.3d_radius", radiusProperty)
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
        ParsedData parsedData = cacheStorage.updateOrInsert(key, () -> Pair.of(Matrix4.IDENTITY, this.rootTilesetUrl));
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
                ParsedData parsedData = cacheStorage.updateOrInsert(currentKey, () -> Pair.of(currentTransform, currentUrl));
                if(parsedData == null) continue;

				TileData tileData = parsedData.getTileData();
                if((tileData instanceof TileGltfModel) || (tileData instanceof Batched3DModel) || (tileData instanceof Instanced3DModel)) {
                    result.add(currentKey);
                }
                else if(tileData instanceof Tileset) {
                    Tileset newTileset = (Tileset) tileData;
                    nodes.add(new Node(newTileset, currentUrl, currentKeys, currentTransform));
                }
            }
        } while(!nodes.isEmpty());

        return result;
    }

    @Override
    protected List<GraphicsShape<?>> getNonTexturedModel(TileGlobalKey o) {
        return null;
    }

    @Override
    public void close() {
        this.cacheStorage.close();
    }

    public static class Deserializer extends JsonDeserializer<Ogc3dTileMapService> {
        @Override
        public Ogc3dTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            CommonYamlObject commonYamlObject = CommonYamlObject.from(node);
            return new Ogc3dTileMapService(commonYamlObject);
        }
    }

    private class TileDataStorage extends CacheableProcessorModel<TileGlobalKey, Pair<Matrix4, URL>, ParsedData>
            implements Closeable {

        private final ExecutorService executorService;
        private final MultiThreadedBlock<TileGlobalKey, Pair<Matrix4, URL>, Pair<Matrix4, InputStream>> tileStreamFetcher;

        /**
         * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
         * @param maximumSize        Maximum cache size. Set to -1 for no limits
         * @param debug              debug
         */
        protected TileDataStorage(long expireMilliseconds, int maximumSize, boolean debug) {
            super(expireMilliseconds, maximumSize, debug);

            this.executorService = Executors.newFixedThreadPool(Ogc3dTileMapService.this.nThreads);
            this.tileStreamFetcher = MultiThreadedBlock.of((key, pair) -> {
                ByteBuf buf = HttpResourceManager.download(pair.getRight().toString());
                return Pair.of(pair.getLeft(), new ByteBufInputStream(buf));
            }, this.executorService, 3, 200);
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

    private class ModelMaker extends AbstractModelMaker<TileGlobalKey> {

        private final ImmediateBlock<TileGlobalKey, TileGlobalKey, ParsedData> storageFetcher;

        /**
         * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
         * @param maximumSize        Maximum cache size. Set to -1 for no limits
         * @param debug              debug
         */
        protected ModelMaker(long expireMilliseconds, int maximumSize, boolean debug) {
            super(expireMilliseconds, maximumSize, debug);
            this.storageFetcher = ImmediateBlock.of((key, input) -> {
                ProcessingState state = cacheStorage.getResourceProcessingState(input);
                if(state != ProcessingState.PROCESSED) return null;
                return cacheStorage.updateAndGetOutput(input);
            });
        }

        @Override
        protected SequentialBuilder<TileGlobalKey, TileGlobalKey, List<GraphicsModel>> getSequentialBuilder() {
            return new SequentialBuilder<>(this.storageFetcher)
                    .then(TILE_PARSER)
                    .then(modelTextureBaker);
        }
    }

}
