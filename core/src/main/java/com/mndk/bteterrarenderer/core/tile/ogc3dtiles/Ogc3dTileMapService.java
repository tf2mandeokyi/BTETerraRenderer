package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.graphics.model.PreBakedModel;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.TmsIdPair;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileKeyManager;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.accessor.RangedDoublePropertyAccessor;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(using = Ogc3dTileMapService.Deserializer.class)
public class Ogc3dTileMapService extends TileMapService<TileGlobalKey> {

    private final Object DEFAULT_QUEUE_KEY = 0;

    @Setter
    private transient double radius = 40;
    private final URL rootTilesetUrl;
    private transient final CachedTileParser<TmsIdPair<TileGlobalKey>> tileParser = CachedTileParser.getInstance();

    private Ogc3dTileMapService(CommonYamlObject commonYamlObject) {
        super(commonYamlObject);
        try {
            this.rootTilesetUrl = new URL(commonYamlObject.getTileUrl());
            this.setFetcherQueueKey(DEFAULT_QUEUE_KEY);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object tileIdToFetcherQueueKey(TileGlobalKey tileGlobalKey) {
        return DEFAULT_QUEUE_KEY;
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
    protected List<TileGlobalKey> getRenderTileIdList(String tmsId, double longitude, double latitude, double height) {
        if(radius == 0) return Collections.emptyList();

        Cartesian3 cartesian = new Spheroid3(Math.toRadians(longitude), Math.toRadians(latitude), height)
                .toCartesianCoordinate();
        Sphere playerSphere = new Sphere(cartesian, radius);
        return this.getIdListRecursively(tmsId, playerSphere);
    }

    @Nullable
    private Tileset getRootTileset(String tmsId) {
        TmsIdPair<TileGlobalKey> idPair = new TmsIdPair<>(tmsId, TileGlobalKey.ROOT_KEY);
        ProcessingState state = tileParser.getResourceProcessingState(idPair);
        switch(state) {
            case PROCESSED:
                ParsedData parsedData = tileParser.updateAndGetResource(idPair);
                if(!(parsedData.getTileData() instanceof Tileset)) return null;
                return (Tileset) parsedData.getTileData();
            case NOT_PROCESSED:
                InputStream stream;
                try {
                    stream = this.fetchData(idPair.getTileId(), this.rootTilesetUrl);
                } catch(Exception e) { return null; }
                if(stream == null) return null;

                tileParser.resourceProcessingReady(idPair, new PreParsedData(Matrix4.IDENTITY, stream));
                return null;
            default:
                return null;
        }
    }

    public List<TileGlobalKey> getIdListRecursively(String tmsId, Sphere playerSphere) {
        List<TileGlobalKey> result = new ArrayList<>();

        @RequiredArgsConstructor
        class Node {
            final Tileset tileset;
            final URL parentUrl;
            final TileLocalKey[] parentKeys;
            final Matrix4 parentTransform;
        }

        Stack<Node> nodes = new Stack<>();
        Tileset rootTileset = this.getRootTileset(tmsId);
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
                TmsIdPair<TileGlobalKey> idPair = new TmsIdPair<>(tmsId, currentKey);

                // Get data from cache
                ParsedData parsedData;
                ProcessingState state = tileParser.getResourceProcessingState(idPair);
                switch(state) {
                    case PROCESSED:
                        parsedData = tileParser.updateAndGetResource(idPair);
                        break;
                    case NOT_PROCESSED:
                        InputStream stream;
                        try {
                            stream = this.fetchData(currentKey, currentUrl);
                        } catch(Exception e) { continue; }
                        if(stream == null) continue;

                        tileParser.resourceProcessingReady(idPair,
                                new PreParsedData(currentTransform, stream));
                        continue;
                    default:
                        continue;
                }

				TileData tileData = parsedData.getTileData();

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
    protected List<PreBakedModel> getPreBakedModels(TmsIdPair<TileGlobalKey> idPair) {
        ProcessingState state = tileParser.getResourceProcessingState(idPair);
        if(state == ProcessingState.ERROR) return Collections.emptyList();
        if(state != ProcessingState.PROCESSED) return null;

        ParsedData parsedData = tileParser.updateAndGetResource(idPair);
        TileData tileData = parsedData.getTileData();
        Matrix4 transform = parsedData.getTransform();

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
    protected List<GraphicsShape<?>> getNonTexturedModel(TileGlobalKey o) {
        return null;
    }

    public static class Deserializer extends JsonDeserializer<Ogc3dTileMapService> {
        @Override
        public Ogc3dTileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            CommonYamlObject commonYamlObject = CommonYamlObject.from(node);
            return new Ogc3dTileMapService(commonYamlObject);
        }
    }

}
