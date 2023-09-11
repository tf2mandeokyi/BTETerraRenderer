package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.ogc3dtiles.GltfModelConverter;
import com.mndk.bteterrarenderer.core.ogc3dtiles.tile.TileGlobalKey;
import com.mndk.bteterrarenderer.core.ogc3dtiles.tile.TileKeyManager;
import com.mndk.bteterrarenderer.core.ogc3dtiles.tile.TileLocalKey;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import com.mndk.bteterrarenderer.core.util.JsonParserUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.processor.MultiThreadedResourceCacheProcessor;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import de.javagl.jgltf.model.GltfModel;
import lombok.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = Ogc3dTileMapService.Deserializer.class)
public class Ogc3dTileMapService extends TileMapService<TileGlobalKey> {

    private static final ExecutorService TILE_PARSING_SERVICE = Executors.newCachedThreadPool();

    @Setter
    private transient double radius = 40;

    private final URL rootTilesetUrl;

    private transient final CachedTileParser<TileGlobalKey> cachedTileParser = new CachedTileParser<>();

    public Ogc3dTileMapService(String name, ExecutorService downloadExecutor,
                               URL rootTilesetUrl) {
        super(name, downloadExecutor);
        this.rootTilesetUrl = rootTilesetUrl;

        this.properties.add(new PropertyAccessor.Localized<>("radius", "gui.bteterrarenderer.settings.3d_radius",
                RangedDoublePropertyAccessor.of(this::getRadius, this::setRadius, 1, 1000)));
    }

    @Override
    protected List<TileGlobalKey> getRenderTileIdList(double longitude, double latitude, double height) {
		if(radius == 0) return Collections.emptyList();

        Cartesian3 cartesian = new Spheroid3(Math.toRadians(longitude), Math.toRadians(latitude), height)
                .toCartesianCoordinate();
        Sphere playerSphere = new Sphere(cartesian, radius);
        return this.getRecursively(playerSphere);
    }

    @Nullable
    private Tileset getRootTileset() {
        TileGlobalKey key = new TileGlobalKey(new TileLocalKey[0]);
        ProcessingState state = cachedTileParser.getResourceProcessingState(key);
        switch(state) {
            case PROCESSED:
                ParsedData parsedData = cachedTileParser.updateAndGetResource(key);
                if(!(parsedData.tileData instanceof Tileset)) return null;
                return (Tileset) parsedData.tileData;
            case NOT_PROCESSED:
                InputStream stream;
                try {
                    stream = this.fetchData(key, this.rootTilesetUrl);
                } catch(IOException e) { return null; }
                if(stream == null) return null;

                cachedTileParser.resourceProcessingReady(key, new PreParsedData(Matrix4.IDENTITY, stream));
                return null;
            default:
                return null;
        }
    }

    public List<TileGlobalKey> getRecursively(Sphere playerSphere) {
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
            List<TileLocalKey> intersections =
                    TileKeyManager.getIntersectionsFromTileset(currentTileset, playerSphere, parentTransform);

            for (TileLocalKey localKey : intersections) {

                Map.Entry<TileContentLink, Matrix4> entry =
                        TileKeyManager.findTileContentLink(currentTileset, localKey, parentTransform);
                if(entry == null) continue;
                TileContentLink contentLink = entry.getKey();
                Matrix4 currentTransform = entry.getValue();

                // Skip if the content is null or its url is malformed
                if (contentLink == null) continue;
                URL currentUrl;
                try {
                    currentUrl = contentLink.getTrueUrl(parentUrl);
                } catch(MalformedURLException e) { continue; }

                TileLocalKey[] currentKeys = ArrayUtil.expandOne(parentKeys, localKey, TileLocalKey[]::new);
                TileGlobalKey currentKey = new TileGlobalKey(currentKeys);

                // Get data from cache
                ParsedData parsedData;
                ProcessingState state = cachedTileParser.getResourceProcessingState(currentKey);
                switch(state) {
                    case PROCESSED:
                        parsedData = cachedTileParser.updateAndGetResource(currentKey);
                        break;
                    case NOT_PROCESSED:
                        InputStream stream;
                        try {
                            stream = this.fetchData(currentKey, currentUrl);
                        } catch(IOException e) { continue; }
                        if(stream == null) continue;

                        cachedTileParser.resourceProcessingReady(currentKey, new PreParsedData(currentTransform, stream));
                        continue;
                    default:
                        continue;
                }

				TileData tileData = parsedData.tileData;

                // TODO: Add case for i3dm
                if(tileData instanceof TileGltfModel) {
                    result.add(currentKey);
                }
                else if(tileData instanceof Batched3DModel) {
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
    protected List<PreBakedModel> getPreBakedModels(TileGlobalKey key) {
        ProcessingState state = cachedTileParser.getResourceProcessingState(key);
        if(state == ProcessingState.ERROR) return Collections.emptyList();
        if(state != ProcessingState.PROCESSED) return null;

        ParsedData parsedData = cachedTileParser.updateAndGetResource(key);
        TileData tileData = parsedData.tileData;
        Matrix4 transform = parsedData.transform;

        GltfModel gltfModel = getGltfModel(tileData);
        if(gltfModel == null) return null;
        return GltfModelConverter.convertModel(gltfModel, transform, Projections.getServerProjection());
    }

    @Nullable
    private static GltfModel getGltfModel(TileData tileData) {
        if(tileData instanceof TileGltfModel) {
            return ((TileGltfModel) tileData).getInstance();
        }
        else if(tileData instanceof Batched3DModel) {
            return ((Batched3DModel) tileData).getGltfModel().getInstance();
        }
        else if(tileData instanceof Tileset) {
            return null;
        }
        // TODO: support i3dm
        throw new UnsupportedOperationException("Unsupported tile data format: " + tileData.getDataFormat());
    }

    @Nullable
    @Override
    protected List<GraphicsQuad<?>> getNonTexturedModel(TileGlobalKey o) {
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Ogc3dTileMapService> {
        @Override
        public Ogc3dTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            String name = node.get("name").asText();
            String rootTileset = node.get("tile_url").asText();
            URL rootTilesetUrl = new URL(rootTileset);

            int maxThread = JsonParserUtil.getOrDefault(node, "max_thread", DEFAULT_MAX_THREAD);
            ExecutorService downloadExecutor = Executors.newFixedThreadPool(maxThread);

            return new Ogc3dTileMapService(name, downloadExecutor, rootTilesetUrl);
        }
    }

    private static class CachedTileParser<Key> extends MultiThreadedResourceCacheProcessor<Key, PreParsedData, ParsedData> {

        protected CachedTileParser() {
            super(TILE_PARSING_SERVICE, 1000 * 60 * 10 /* 10 minutes */, 10000, -1, 100, false);
        }

        @Override
        protected ParsedData processResource(PreParsedData preParsedData) throws Exception {
            Matrix4 transform = preParsedData.transform;
            InputStream stream = preParsedData.stream;
            return new ParsedData(transform, TileResourceManager.parse(stream));
        }

        @Override
        protected void deleteResource(ParsedData tileData) {}
    }

    @RequiredArgsConstructor
    private static class PreParsedData {
        private final Matrix4 transform;
        private final InputStream stream;
    }

    @RequiredArgsConstructor
    private static class ParsedData {
        private final Matrix4 transform;
        private final TileData tileData;
    }
}
