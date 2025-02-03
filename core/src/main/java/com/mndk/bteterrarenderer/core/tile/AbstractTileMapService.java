package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.core.graphics.ManualThreadExecutor;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.concurrent.CacheStorage;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFX;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.util.json.JsonString;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public abstract class AbstractTileMapService<TileId> implements TileMapService {

    private static final ManualThreadExecutor MODEL_BAKER = new ManualThreadExecutor();

    public static final int DEFAULT_MAX_THREAD = 2;

    private final int nThreads;
    private final Translatable<String> name;
    @Nullable private final Translatable<String> copyrightTextJson;
    @Nullable private final URL iconUrl;
    @Nullable private final GeographicProjection hologramProjection;
    @Getter(value = AccessLevel.PRIVATE)
    private final String dummyTileUrl;

    private transient final List<PropertyAccessor.Localized<?>> stateAccessors = new ArrayList<>();
    private transient final ModelStorage storage;
    private transient McFXElement hudElement;
    private final transient McFXElement bakingIndicatorWrapper = McFX.div().setColor(0xFFFFFFFF);

    protected AbstractTileMapService(CommonYamlObject commonYamlObject) {
        this.name = commonYamlObject.name;
        this.dummyTileUrl = commonYamlObject.tileUrl;
        this.iconUrl = commonYamlObject.iconUrl;
        this.copyrightTextJson = Optional.ofNullable(commonYamlObject.copyrightTextJson)
                .map(json -> json.map(JsonString::getValue))
                .orElse(null);
        this.nThreads = commonYamlObject.nThreads;
        this.hologramProjection = commonYamlObject.hologramProjection;
        this.stateAccessors.addAll(this.makeStateAccessors());
        this.storage = new ModelStorage(commonYamlObject.cacheConfig);
    }

    @Override
    public final List<GraphicsModel> getModels(McCoord cameraPos, double yawDegrees, double pitchDegrees) {
        // Bake textures
        this.preRender(cameraPos);
        MODEL_BAKER.process(2);

        // Get tileId list
        List<TileId> renderTileIdList = this.getRenderTileIdList(cameraPos, yawDegrees, pitchDegrees);

        // Get models based on tileId list
        List<GraphicsModel> result = new ArrayList<>();
        for (TileId tileId : renderTileIdList) {
            List<GraphicsModel> models = this.getModelsForId(tileId);
            if (models == null) continue;
            result.addAll(models);
        }
        return result;
    }

    @Nullable
    private List<GraphicsModel> getModelsForId(TileId tileId) {
        return this.storage.getOrCompute(tileId, () -> {
            CompletableFuture<List<PreBakedModel>> future = this.processModel(tileId);
            return future == null ? null : future.thenApplyAsync(this::bake, MODEL_BAKER);
        });
    }

    private List<GraphicsModel> bake(List<PreBakedModel> preBakedModels) {
        List<GraphicsModel> models = new ArrayList<>(preBakedModels.size());
        for (PreBakedModel preBakedModel : preBakedModels) {
            BufferedImage image = preBakedModel.getImage();
            NativeTextureWrapper textureObject = McConnector.client().textureManager
                    .allocateAndGetTextureObject(BTETerraRenderer.MODID, image);
            models.add(new GraphicsModel(textureObject, preBakedModel.getShapes()));
        }
        return models;
    }

    private static void delete(List<GraphicsModel> models) {
        for (GraphicsModel model : models) {
            McConnector.client().textureManager.deleteTextureObject(model.getTextureObject());
        }
    }

    @Nonnull
    public final GeographicProjection getHologramProjection() {
        return hologramProjection != null ? hologramProjection : Projections.getHologramProjection();
    }

    @Override
    public McCoordTransformer getModelPositionTransformer() {
        return McCoordTransformer.IDENTITY;
    }

    @Override
    public final void renderHud(GuiDrawContextWrapper context) {
        WindowDimension dimension = McConnector.client().getWindowSize();
        int width = dimension.getScaledWidth();
        if (this.hudElement == null) {
            McFXElement element = this.makeHudElement();
            if (element == null) element = McFX.div();
            // side padding: 4px
            this.hudElement = McFX.vList(0, 4).addAll(
                    McFX.div(4), // top padding: 4px
                    element,
                    this.bakingIndicatorWrapper
            );
            this.hudElement.init(width);
        }

        int bakingCounter = this.storage.getProcessingCount();
        this.bakingIndicatorWrapper.setStringContent(bakingCounter != 0
                ? "Baking " + bakingCounter + " model(s)..." : "");
        this.hudElement.onWidthChange(width);
        context.pushMatrix();
        this.hudElement.drawComponent(context);
        context.popMatrix();
    }

    @Override
    public final void cleanUp() {
        this.storage.cleanUp();
    }

    // Default method overrides
    @Override public final String toString() {
        String name = this.name.get();
        int hash = this.hashCode();
        return String.format("%s(name=%s,hash=%08x)", this.getClass().getSimpleName(), name, hash);
    }
    @Override public final boolean equals(Object obj) { return super.equals(obj); }
    @Override public final int hashCode() { return super.hashCode(); }
    @Override public void close() throws IOException {
        this.storage.close();
    }

    // ######################## <ABSTRACT METHODS> ########################

    @Nullable
    protected abstract McFXElement makeHudElement();

    /**
     * This method is executed in the getModels thread and before rendering tiles.
     */
    protected abstract void preRender(McCoord playerPos);

    /**
     * Returns a list of pre-baked models for the given tile ID, or {@code null} if the
     * corresponding tile is not yet ready to be processed.
     * @param tileId The tile ID
     * @return A list of pre-baked models, or {@code null} if the tile is not yet ready.
     */
    @Nullable
    protected abstract CompletableFuture<List<PreBakedModel>> processModel(TileId tileId);

    /**
     * This method is called only once on the constructor
     * @return The property list
     */
    protected abstract List<PropertyAccessor.Localized<?>> makeStateAccessors();

    /**
     * Retrieves a list of tile IDs to be rendered based on the given spheroid coordinates and frustum.
     *
     * @param cameraPos The camera position
     * @param yawDegrees The yaw angle of the camera
     * @param pitchDegrees The pitch angle of the camera
     * @return A list of tile IDs to be rendered.
     */
    public abstract List<TileId> getRenderTileIdList(McCoord cameraPos, double yawDegrees, double pitchDegrees);

    @Nullable
    public abstract List<GraphicsModel> getLoadingModel(TileId tileId) throws OutOfProjectionBoundsException;

    @Nullable
    public abstract List<GraphicsModel> getErrorModel(TileId tileId) throws OutOfProjectionBoundsException;

    // ######################## </ABSTRACT METHODS> ########################

    private final class ModelStorage extends CacheStorage<TileId, List<GraphicsModel>> {

        public ModelStorage(@Nullable Config config) {
            super(config);
        }

        @Override
        protected List<GraphicsModel> whenProcessing(TileId key) {
            try { return AbstractTileMapService.this.getLoadingModel(key); }
            catch (OutOfProjectionBoundsException ignored) {}
            return null;
        }

        @Override
        protected List<GraphicsModel> whenError(TileId key, Throwable error) {
            try { return AbstractTileMapService.this.getErrorModel(key); }
            catch (OutOfProjectionBoundsException ignored) {}
            return null;
        }

        @Override
        protected void delete(List<GraphicsModel> value) {
            AbstractTileMapService.delete(value);
        }
    }

    protected abstract static class TMSSerializer<T extends AbstractTileMapService<?>> extends JsonSerializer<T> {
        private final String type;
        protected TMSSerializer(Class<T> clazz) {
            this.type = TileMapServiceParseRegistries.TYPE_MAP.inverse().get(clazz);
        }

        @Override
        public final void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("type", this.type);

            CommonYamlObject.from(value).write(gen);
            this.serializeTMS(value, gen, serializers);
            gen.writeEndObject();
        }

        protected abstract void serializeTMS(T value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException;
    }

    protected static abstract class TMSDeserializer<T extends AbstractTileMapService<?>> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            CommonYamlObject commonYamlObject = ctxt.readTreeAsValue(node, CommonYamlObject.class);
            return this.deserialize(node, commonYamlObject, ctxt);
        }
        protected abstract T deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt)
                throws IOException;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class CommonYamlObject {

        private Translatable<String> name;
        private String tileUrl;
        private URL iconUrl;
        private int nThreads;
        @Nullable private Translatable<JsonString> copyrightTextJson;
        @Nullable private GeographicProjection hologramProjection;
        @Nullable private CacheStorage.Config cacheConfig;

        @JsonCreator
        public CommonYamlObject(
                @JsonProperty(value = "name", required = true) Translatable<String> name,
                @JsonProperty(value = "tile_url", required = true) String tileUrl,
                @Nullable @JsonProperty("max_thread") Integer nThreads,
                @Nullable @JsonProperty("copyright") Translatable<JsonString> copyrightTextJson,
                @Nullable @JsonProperty("icon_url") URL iconUrl,
                @Nullable @JsonProperty("hologram_projection") GeographicProjection hologramProjection,
                @Nullable @JsonProperty("cache") CacheStorage.Config cacheConfig
        ) {
            this.name = name;
            this.copyrightTextJson = copyrightTextJson;
            this.tileUrl = tileUrl;
            this.iconUrl = iconUrl;
            this.nThreads = nThreads != null ? nThreads : DEFAULT_MAX_THREAD;
            this.hologramProjection = hologramProjection;
            this.cacheConfig = cacheConfig;
        }

        private void write(JsonGenerator gen) throws IOException {
            gen.writeObjectField("name", this.name);
            gen.writeStringField("tile_url", this.tileUrl);
            if (this.iconUrl != null) {
                gen.writeStringField("icon_url", this.iconUrl.toString());
            }
            gen.writeNumberField("max_thread", this.nThreads);
            gen.writeObjectField("copyright", this.copyrightTextJson);
            gen.writeObjectField("hologram_projection", this.hologramProjection);
        }

        private static CommonYamlObject from(AbstractTileMapService<?> tms) {
            CommonYamlObject result = new CommonYamlObject();
            result.name = tms.getName();
            result.tileUrl = tms.getDummyTileUrl();
            result.iconUrl = tms.getIconUrl();
            result.nThreads = tms.getNThreads();
            result.copyrightTextJson = Optional.ofNullable(tms.copyrightTextJson)
                    .map(json -> json.map(JsonString::fromUnsafe))
                    .orElse(null);
            result.hologramProjection = tms.getHologramProjection();

            return result;
        }
    }
}
