package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelTextureBakingBlock;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.i18n.Translatable;
import com.mndk.bteterrarenderer.core.util.json.JsonString;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public abstract class AbstractTileMapService<TileId> implements TileMapService {

    private static final GraphicsModelTextureBakingBlock<?> MODEL_BAKER = new GraphicsModelTextureBakingBlock<>();
    private static boolean MISSING_TEXTURE_BAKED = false;
    public static final int DEFAULT_MAX_THREAD = 2;

    protected final int nThreads;
    private final Translatable<String> name;
    @Nullable private final Translatable<String> copyrightTextJson;
    @Nullable private final URL iconUrl;
    @Nullable private final GeographicProjection hologramProjection;
    @Getter(value = AccessLevel.PRIVATE)
    private final String dummyTileUrl;

    private transient final List<PropertyAccessor.Localized<?>> stateAccessors = new ArrayList<>();
    private transient ModelMaker modelMaker; // late init
    private final ProcessorCacheStorage<TileId, List<GraphicsModel>> storage;

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
        this.storage = new ProcessorCacheStorage<>(commonYamlObject.cacheConfig);
    }

    @Override
    public final List<GraphicsModel> getModels(McCoord playerPos) {
        // Bake textures
        if (!MISSING_TEXTURE_BAKED) {
            MODEL_BAKER.setDefaultTexture(McConnector.client().glGraphicsManager.getMissingTextureObject());
            MISSING_TEXTURE_BAKED = true;
        }
        this.preRender(playerPos);
        MODEL_BAKER.process(2);

        // Get tileId list
        List<TileId> renderTileIdList;
        try {
            double[] geoCoord = this.getHologramProjection().toGeo(playerPos.getX(), playerPos.getZ());
            renderTileIdList = this.getRenderTileIdList(geoCoord[0], geoCoord[1], playerPos.getY());
        } catch (OutOfProjectionBoundsException e) { return Collections.emptyList(); }

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
        ProcessingState bakedState = this.getModelMaker().getResourceProcessingState(tileId);
        switch (bakedState) {
            case NOT_PROCESSED:
                this.getModelMaker().insertInput(tileId, tileId);
                break;
            case PROCESSED:
                return this.getModelMaker().updateAndGetOutput(tileId);
            case PROCESSING:
                try {
                    List<GraphicsModel> loadingModel = this.getLoadingModel(tileId);
                    if (loadingModel != null) return loadingModel;
                } catch (OutOfProjectionBoundsException ignored) {}
                break;
            case ERROR:
                try {
                    List<GraphicsModel> errorModel = this.getErrorModel(tileId);
                    if (errorModel != null) return errorModel;
                } catch (OutOfProjectionBoundsException ignored) {}
                break;
        }
        return null;
    }

    @Nonnull
    public final GeographicProjection getHologramProjection() {
        return hologramProjection != null ? hologramProjection : Projections.getHologramProjection();
    }

    @Override
    public McCoordTransformer getPositionTransformer() {
        return McCoordTransformer.IDENTITY;
    }

    private CacheableProcessorModel<TileId, TileId, List<GraphicsModel>> getModelMaker() {
        if (this.modelMaker == null) this.modelMaker = new ModelMaker(this.storage);
        return this.modelMaker;
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
        if (this.modelMaker != null) this.modelMaker.close();
        this.storage.close();
    }

    // ######################## <ABSTRACT METHODS> ########################

    /**
     * This method is executed in the getModels thread and before rendering tiles.
     */
    protected abstract void preRender(McCoord playerPos);

    protected abstract CacheableProcessorModel.SequentialBuilder<TileId, TileId, List<PreBakedModel>> getModelSequentialBuilder();
    /**
     * This method is called only once on the constructor
     * @return The property list
     */
    protected abstract List<PropertyAccessor.Localized<?>> makeStateAccessors();

    /**
     * @param longitude Player longitude, in degrees
     * @param latitude  Player latitude, in degrees
     * @param seaLevelHeight    Player height, in meters
     * @return A list of tile ids
     */
    public abstract List<TileId> getRenderTileIdList(double longitude, double latitude, double seaLevelHeight);

    @Nullable
    public abstract List<GraphicsModel> getLoadingModel(TileId tileId) throws OutOfProjectionBoundsException;

    @Nullable
    public abstract List<GraphicsModel> getErrorModel(TileId tileId) throws OutOfProjectionBoundsException;

    // ######################## </ABSTRACT METHODS> ########################

    private class ModelMaker extends CacheableProcessorModel<TileId, TileId, List<GraphicsModel>> {

        protected ModelMaker(ProcessorCacheStorage<TileId, List<GraphicsModel>> storage) {
            super(storage);
        }

        @Override
        protected SequentialBuilder<TileId, TileId, List<GraphicsModel>> getSequentialBuilder() {
            return AbstractTileMapService.this.getModelSequentialBuilder()
                    .then(BTRUtil.uncheckedCast(MODEL_BAKER));
        }

        @Override
        protected void deleteResource(List<GraphicsModel> graphicsModels) {
            for (GraphicsModel model : graphicsModels) {
                McConnector.client().glGraphicsManager.deleteTextureObject(model.getTextureObject());
            }
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
        @Nullable private ProcessorCacheStorage.Config cacheConfig;

        @JsonCreator
        public CommonYamlObject(
                @JsonProperty(value = "name", required = true) Translatable<String> name,
                @JsonProperty(value = "tile_url", required = true) String tileUrl,
                @Nullable @JsonProperty("max_thread") Integer nThreads,
                @Nullable @JsonProperty("copyright") Translatable<JsonString> copyrightTextJson,
                @Nullable @JsonProperty("icon_url") URL iconUrl,
                @Nullable @JsonProperty("hologram_projection") GeographicProjection hologramProjection,
                @Nullable @JsonProperty("cache") ProcessorCacheStorage.Config cacheConfig
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
