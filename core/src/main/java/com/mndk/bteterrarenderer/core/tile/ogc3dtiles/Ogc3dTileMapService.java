package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.ImageTexturePair;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.AbstractTileMapService;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceCommonProperties;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.LocalTileNode;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileLocalKey;
import com.mndk.bteterrarenderer.util.concurrent.CacheStorage;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.*;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.client.gui.HorizontalAlign;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFX;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidFrustum;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import de.javagl.jgltf.model.GltfModel;
import io.netty.buffer.ByteBufInputStream;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@JsonSerialize(using = Ogc3dTileMapServiceSerializer.class)
@JsonDeserialize(using = Ogc3dTileMapServiceDeserializer.class)
public class Ogc3dTileMapService extends AbstractTileMapService<Ogc3dTileMapService.Key> {

    // From 6.7.1.6.2.2. y-up to z-up:
    // Next, for consistency with the z-up coordinate system of 3D Tiles,
    // glTFs shall be transformed from y-up to z-up at runtime.
    // This is done by rotating the model about the x-axis by pi/2 radians.
    private static final Matrix4d ROTATE_X_AXIS = new Matrix4d().rotateX(Math.PI / 2);

    private static final ImageTexturePair WHITE_TEXTURE;
    private static final ExecutorService TILE_PARSER = Executors.newCachedThreadPool();

    @Setter private transient double radius = 40;
    @Setter private transient boolean yDistortion = false;
    @Setter private transient boolean renderSurroundings = false;
    @Setter private transient double lodFactor = 0;
    @Setter private transient boolean enableTexture = true;
    @Setter private transient boolean enableCulling = false;
    private transient double yDistMagnitude = 1;

    private final URL rootTilesetUrl;
    private final SpheroidCoordinatesConverter coordConverter;
    private final boolean rotateModelAlongEarthXAxis;
    private final String geoidType;

    private transient final ExecutorService tileFetcher;
    private transient final CacheStorage<Key, Pair<Matrix4d, TileData>> tileDataStorage;
    private transient final Map<String, Integer> copyrightOccurrences = new HashMap<>();
    private transient final McFXVerticalList hudList = McFX.vList(0, 0);

    @Builder
    @SneakyThrows(MalformedURLException.class)
    private Ogc3dTileMapService(TileMapServiceCommonProperties properties, SpheroidCoordinatesConverter coordConverter,
                                boolean rotateModelAlongEarthXAxis, String geoidType) {
        super(properties);
        this.rootTilesetUrl = new URL(properties.getTileUrl());
        this.coordConverter = coordConverter;
        this.rotateModelAlongEarthXAxis = rotateModelAlongEarthXAxis;
        this.geoidType = geoidType;

        this.tileFetcher = Executors.newFixedThreadPool(this.getNThreads());
        this.tileDataStorage = new CacheStorage<>(properties.getCacheConfig());
    }

    @Override
    public McCoordTransformer getModelPositionTransformer() {
        float yAlign = (float) BTETerraRendererConfig.HOLOGRAM.getYAlign();
        return this.yDistortion
                ? (pos -> new McCoord(pos.getX(), (float) (pos.getY() * this.yDistMagnitude + yAlign), pos.getZ()))
                : (pos -> pos.add(new McCoord(0, yAlign, 0)));
    }

    @Override
    public VertexBeginner getVertexBeginner(BufferBuildersManager manager, float opacity) {
        return new VertexBeginner() {
            public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture) {
                throw new UnsupportedOperationException("Quads are not supported in 3D tiles");
            }
            public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture) {
                NativeTextureWrapper white = WHITE_TEXTURE.getTextureObject();
                if (!enableTexture && white != null) {
                    return manager.begin3dTri(white, opacity, true, enableCulling);
                } else {
                    // Disabling the normal value (or fixing it to (0, 1, 0)) will give us
                    // the full brightness. So even though PosTexNorm has normal values,
                    // we will ignore it or otherwise the models will have somewhat
                    // "random" brightness values, making the models look weird.
                    return manager.begin3dTri(texture, opacity, false, enableCulling);
                }
            }
        };
    }

    @Override
    protected McFXElement makeHudElement() {
        return this.hudList;
    }

    @Override
    protected void preRender(McCoord playerPos) {
        WHITE_TEXTURE.bake();
        GeographicProjection projection = this.getHologramProjection();
        try {
            // Normally it's good to calculate distortion for individual model vertices...
            // But that significantly drops the fps value (100 -> 4), so I'll put the player position instead.
            // After all the difference wouldn't be noticeable within around 400m from the player.
            // When the distortion calculation becomes fast enough, I'll go back to the original plan.
            double[] geoCoord = projection.toGeo(playerPos.getX(), playerPos.getZ());
            double[] tissot = projection.tissot(geoCoord[0], geoCoord[1]);
            this.yDistMagnitude = Math.sqrt(Math.abs(tissot[0]));
        } catch (OutOfProjectionBoundsException ignored) {}
    }

    @Override
    public void moveAlongYAxis(double amount) {
        BTETerraRendererConfig.HOLOGRAM.yAlign += amount;
    }

    @Override
    protected List<PropertyAccessor.Localized<?>> makeStateAccessors() {
        PropertyAccessor<Double> radius = PropertyAccessor.ranged(this::getRadius, this::setRadius, 1, 1000);
        PropertyAccessor<Double> lodFactor = PropertyAccessor.ranged(this::getLodFactor, this::setLodFactor, 0, 5);
        PropertyAccessor<Boolean> enableTexture = PropertyAccessor.of(this::isEnableTexture, this::setEnableTexture);
        PropertyAccessor<Boolean> enableCulling = PropertyAccessor.of(this::isEnableCulling, this::setEnableCulling);
        PropertyAccessor<Boolean> renderSurroundings = PropertyAccessor.of(this::isRenderSurroundings, this::setRenderSurroundings);
        PropertyAccessor<Boolean> yDistortion = PropertyAccessor.of(this::isYDistortion, this::setYDistortion);

        return Arrays.asList(
                PropertyAccessor.localized("radius", "gui.bteterrarenderer.settings.3d_radius", radius),
                PropertyAccessor.localized("lod_factor", "gui.bteterrarenderer.settings.3d_lod_factor", lodFactor),
                PropertyAccessor.localized("enable_texture", "gui.bteterrarenderer.settings.3d_texture", enableTexture),
                PropertyAccessor.localized("enable_culling", "gui.bteterrarenderer.settings.3d_culling", enableCulling),
                PropertyAccessor.localized("render_surroundings", "gui.bteterrarenderer.settings.3d_render_surroundings", renderSurroundings),
                PropertyAccessor.localized("y_dist", "gui.bteterrarenderer.settings.3d_y_distortion", yDistortion)
        );
    }

    private Spheroid3 mcCoordToSpheroid(McCoord coord) throws OutOfProjectionBoundsException {
        double[] geo = this.getHologramProjection().toGeo(coord.getX(), coord.getZ());
        return Spheroid3.fromDegrees(geo[0], geo[1], coord.getY());
    }

    @Override
    public List<Key> getRenderTileIdList(McCoord cameraPos, double yawDegrees, double pitchDegrees) {
        if (radius == 0) return Collections.emptyList();

        Vector3d point, viewDirection, rightDirection;
        try {
            float y = cameraPos.getY();
            y = this.yDistortion
                    ? (float) ((y - BTETerraRendererConfig.HOLOGRAM.getYAlign()) / this.yDistMagnitude)
                    : (float) (y - BTETerraRendererConfig.HOLOGRAM.getYAlign());
            cameraPos = new McCoord(cameraPos.getX(), y, cameraPos.getZ());
            McCoord mcViewDirection = cameraPos.add(McCoord.fromYawPitch(yawDegrees, pitchDegrees));
            McCoord mcRightDirection = cameraPos.add(McCoord.fromYawPitch(yawDegrees + 90, 0));

            Spheroid3 pointSpheroid = this.mcCoordToSpheroid(cameraPos);
            Spheroid3 viewDirectionSpheroid = this.mcCoordToSpheroid(mcViewDirection);
            Spheroid3 rightDirectionSpheroid = this.mcCoordToSpheroid(mcRightDirection);

            point = this.coordConverter.toCartesian(pointSpheroid);
            viewDirection = this.coordConverter.toCartesian(viewDirectionSpheroid).sub(point).normalize();
            rightDirection = this.coordConverter.toCartesian(rightDirectionSpheroid).sub(point).normalize();
        } catch (OutOfProjectionBoundsException e) { return Collections.emptyList(); }

        SpheroidFrustum frustum = this.getFrustum(point, viewDirection, rightDirection);
        List<Key> result = this.getIdListRecursively(frustum);
        this.updateHudList();
        return result;
    }

    private void updateHudList() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(this.copyrightOccurrences.entrySet());
        sorted.sort(Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(sorted);

        List<McFXElement> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            list.add(McFX.div()
                    .setStringContent("© " + entry.getKey())
                    .setHorizontalAlign(HorizontalAlign.LEFT)
                    .setColor(0xFFFFFFFF));
        }

        int count = this.tileDataStorage.getProcessingCount();
        if (count != 0) {
            list.add(McFX.div()
                    .setStringContent("Loading " + count + " model(s)...")
                    .setHorizontalAlign(HorizontalAlign.LEFT)
                    .setColor(0xFFFFFFFF));
        }
        this.hudList.clear().addAll(list);
    }

    private SpheroidFrustum getFrustum(Vector3d point, Vector3d viewDirection, Vector3d rightDirection) {
        WindowDimension dimension = McConnector.client().getWindowSize();
        float aspectRatio = dimension.getScaledWidth() / (float) dimension.getScaledHeight();
        float verticalFovRadians = (float) Math.toRadians(McConnector.client().getFovDegrees());
        float horizontalFovRadians = 2 * (float) Math.atan(Math.tan(verticalFovRadians / 2) * aspectRatio);
        return new SpheroidFrustum(
                point, viewDirection, rightDirection,
                horizontalFovRadians, verticalFovRadians,
                0.0, radius == 1000 ? null : radius
        );
    }

    @Nullable
    private Tileset getRootTileset() {
        Key key = Key.ROOT;
        Pair<Matrix4d, TileData> pair = this.downloadModel(key, new Matrix4d(), this.rootTilesetUrl);
        if (pair == null) return null;

        TileData tileData = pair.getRight();
        if (tileData instanceof Tileset) return (Tileset) tileData;
        Loggers.get(this).warn("Root tile url is not a tile set");
        return null;
    }

    public List<Key> getIdListRecursively(SpheroidFrustum frustum) {
        List<Key> result = new ArrayList<>();

        Stack<Ogc3dTilesetBfsNode> nodes = new Stack<>();
        Tileset rootTileset = this.getRootTileset();
        if (rootTileset == null) return Collections.emptyList();
        nodes.add(Ogc3dTilesetBfsNode.fromRoot(this, rootTileset, this.rootTilesetUrl));
        this.copyrightOccurrences.clear();

        while (!nodes.isEmpty()) {

            // Get intersections from the current tileset
            Ogc3dTilesetBfsNode node = nodes.pop();
            List<LocalTileNode> intersections = node.selectIntersections(frustum);

            for (LocalTileNode localTileNode : intersections) {
                TileContentLink contentLink = localTileNode.getContentLink();
                Matrix4d currentTransform = localTileNode.getTransform();

                // Skip if the url is malformed
                URL currentUrl;
                try { currentUrl = contentLink.getTrueUrl(node.getParentUrl()); }
                catch (MalformedURLException e) {
                    Loggers.get(this).warn("Malformed URL: {} (parent: {})", contentLink, node.getParentUrl());
                    continue;
                }

                TileLocalKey[] currentKeys = node.attachKey(localTileNode);
                Key currentKey = new Key(currentKeys);

                // Get data from cache
                Pair<Matrix4d, TileData> parsedData = this.downloadModel(currentKey, currentTransform, currentUrl);
                if (parsedData == null) continue;

				TileData child = parsedData.getRight();
                if (child.getGltfModelInstance() != null) {
                    result.add(currentKey);
                }

                String copyright = child.getCopyright();
                if (copyright != null) {
                    // According to the Google Earth API specification:
                    // 1. Extract all the copyright information from all the tiles in view.
                    // 2. Separate multiple copyright sources with a semicolon.
                    // 3. Sort the information based on the number of occurrences.
                    // 4. Display the copyright sources on-screen, ordered from most occurrences to the least.
                    String[] sources = copyright.split(";");
                    for (String source : sources) {
                        if (source.isEmpty()) continue;
                        int count = this.copyrightOccurrences.getOrDefault(source, 0);
                        this.copyrightOccurrences.put(source, count + 1);
                    }
                }

                if (child instanceof Tileset) {
                    Tileset childTileset = (Tileset) child;
                    Ogc3dTilesetBfsNode newNode = new Ogc3dTilesetBfsNode(this, childTileset, currentUrl, currentKeys, currentTransform);
                    nodes.add(newNode);
                }
            }
        }

        return result;
    }

    @Override
    protected @Nullable CompletableFuture<List<PreBakedModel>> processModel(Key key) {
        Pair<Matrix4d, TileData> pair = this.tileDataStorage.getOrCompute(key, () -> null);
        if (pair == null) return null;
        return CompletableFuture.supplyAsync(() -> this.parse(pair.getLeft(), pair.getRight()), this.tileFetcher);
    }

    private List<PreBakedModel> parse(Matrix4d transform, TileData tileData) {
        // From 6.7.1.6. Transforms:
        // ...
        // More broadly the order of transformations is:
        //  1. glTF node hierarchy transformations
        //  2. glTF y-up to z-up transform
        //  3. Tile glTF transform
        if (this.isRotateModelAlongEarthXAxis()) {
            transform.mul(ROTATE_X_AXIS);
        }

        GltfModel gltfModel = tileData.getGltfModelInstance();
        if (gltfModel == null) return Collections.emptyList();

        SpheroidCoordinatesConverter coordConverter = this.getCoordConverter();
        return GltfModelConverter.convertModel(gltfModel, transform, this.getHologramProjection(), coordConverter);
    }

    private Pair<Matrix4d, TileData> downloadModel(Key key, Matrix4d transform, URL url) {
        return this.tileDataStorage.getOrCompute(key, () -> HttpResourceManager.download(url.toString(), this.getNThreads())
                .thenApply(ByteBufInputStream::new)
                .thenApplyAsync(stream -> {
                    try { return Pair.of(transform, TileResourceManager.parse(stream, this.coordConverter)); }
                    catch (IOException e) { throw new RuntimeException(e); }
                }, TILE_PARSER));
    }

    @Override
    public List<GraphicsModel> getLoadingModel(Key o) {
        return null;
    }

    @Override
    public List<GraphicsModel> getErrorModel(Key o) {
        return null;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.tileDataStorage.close();
    }

    static {
        BufferedImage white = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) white.setRGB(x, y, 0xFFFFFFFF);
        }
        WHITE_TEXTURE = new ImageTexturePair(white);
    }

    @Data
    public static class Key {
        public static final Key ROOT = new Key(new TileLocalKey[0]);

        private final TileLocalKey[] keys;

        @Override
        public String toString() {
            String result = Arrays.stream(keys)
                    .map(TileLocalKey::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            return "TileGlobalKey[" + result + "]";
        }
    }
}
