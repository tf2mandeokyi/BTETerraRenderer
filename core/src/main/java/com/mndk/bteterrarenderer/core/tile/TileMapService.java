package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelTextureBakingBlock;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.i18n.Translatable;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.core.util.json.JsonString;
import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.core.util.processor.ProcessorCacheStorage;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.PositionTransformer;
import lombok.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = TileMapService.Serializer.class)
@JsonDeserialize(using = TileMapService.Deserializer.class)
public abstract class TileMapService<TileId> implements AutoCloseable {

    public static final ProcessorCacheStorage<TMSIdPair<?>, List<GraphicsModel>> STORAGE
            = new ProcessorCacheStorage<>(1000 * 60 * 30 /* 30 minutes */, 10000, false);
    private static final GraphicsModelTextureBakingBlock<?> MODEL_BAKER = new GraphicsModelTextureBakingBlock<>();
    public static final int DEFAULT_MAX_THREAD = 2;

    protected final int nThreads;
    private final Translatable<String> name;
    @Nullable private final Translatable<String> copyrightTextJson;
    @Nullable private final URL iconUrl;
    @Setter private Object iconTextureObject;
    @Getter(value = AccessLevel.PACKAGE)
    private final String dummyTileUrl;

    /**
     * This property should be configured on the constructor.
     * One should put localization key as a key, and the property accessor as a value.
     * */
    private transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();
    private transient ModelMaker modelMaker; // late init

    protected TileMapService(CommonYamlObject commonYamlObject) {
        this.name = commonYamlObject.name;
        this.dummyTileUrl = commonYamlObject.tileUrl;
        this.iconUrl = commonYamlObject.iconUrl;
        this.copyrightTextJson = Optional.ofNullable(commonYamlObject.copyrightTextJson)
                .map(json -> json.map(JsonString::getValue))
                .orElse(null);
        this.nThreads = commonYamlObject.nThreads;
        this.properties.addAll(this.makeProperties());
    }

    public final void render(@Nonnull DrawContextWrapper<?> drawContextWrapper,
                             double px, double py, double pz, float opacity) {
        // Bake textures
        this.preRender(px, py, pz);
        MODEL_BAKER.process(2);

        // Get tileId list
        List<TileId> renderTileIdList;
        try {
            double[] geoCoord = Projections.getServerProjection().toGeo(px, pz);
            renderTileIdList = this.getRenderTileIdList(geoCoord[0], geoCoord[1], py);
        } catch(OutOfProjectionBoundsException e) { return; }

        // Make position transformer
        PositionTransformer transformer = this.getPositionTransformer(px, py, pz);

        // Render tileId list
        for(TileId tileId : renderTileIdList) {
            List<GraphicsModel> models = this.getModelsForId(tileId);
            if (models == null) continue;
            models.forEach(model -> model.drawAndRender(drawContextWrapper, transformer, opacity));
        }
    }

    @Nullable
    private List<GraphicsModel> getModelsForId(TileId tileId) {
        TMSIdPair<TileId> idPair = new TMSIdPair<>(this, tileId);
        ProcessingState bakedState = this.getModelMaker().getResourceProcessingState(idPair);
        switch (bakedState) {
            case NOT_PROCESSED:
                this.getModelMaker().insertInput(idPair, tileId);
                break;
            case PROCESSED:
                return this.getModelMaker().updateAndGetOutput(idPair);
            case PROCESSING:
                try {
                    List<GraphicsModel> loadingModel = this.getLoadingModel(tileId);
                    if(loadingModel != null) return loadingModel;
                } catch(OutOfProjectionBoundsException ignored) {}
                break;
            case ERROR:
                try {
                    List<GraphicsModel> errorModel = this.getErrorModel(tileId);
                    if(errorModel != null) return errorModel;
                } catch(OutOfProjectionBoundsException ignored) {}
                break;
        }
        return null;
    }

    protected PositionTransformer getPositionTransformer(double px, double py, double pz) {
        return (x, y, z) -> new double[] { x - px, y - py, z - pz };
    }

    public CacheableProcessorModel<TMSIdPair<TileId>, TileId, List<GraphicsModel>> getModelMaker() {
        if (this.modelMaker == null) this.modelMaker = new ModelMaker(BTRUtil.uncheckedCast(STORAGE));
        return this.modelMaker;
    }

    // Default method overrides
    @Override
    public final String toString() {
        String name = this.name.get();
        int hash = this.hashCode();
        return String.format("%s(name=%s,hash=%08x)", this.getClass().getSimpleName(), name, hash);
    }
    @Override
    public final boolean equals(Object obj) { return super.equals(obj); }
    @Override
    public final int hashCode() { return super.hashCode(); }
    @Override
    public void close() throws IOException {
        if(this.modelMaker != null) this.modelMaker.close();
    }

    /**
     * This method is executed in the render thread and before rendering tiles.
     */
    protected abstract void preRender(double px, double py, double pz);

    protected abstract CacheableProcessorModel.SequentialBuilder<TMSIdPair<TileId>, TileId, List<PreBakedModel>> getModelSequentialBuilder();
    /**
     * This method is called only once on the constructor
     * @return The property list
     */
    protected abstract List<PropertyAccessor.Localized<?>> makeProperties();

    /**
     * @param longitude Player longitude, in degrees
     * @param latitude  Player latitude, in degrees
     * @param height    Player height, in meters
     * @return A list of tile ids
     */
    protected abstract List<TileId> getRenderTileIdList(double longitude, double latitude, double height);

    @Nullable
    protected abstract List<GraphicsModel> getLoadingModel(TileId tileId) throws OutOfProjectionBoundsException;

    @Nullable
    protected abstract List<GraphicsModel> getErrorModel(TileId tileId) throws OutOfProjectionBoundsException;

    private class ModelMaker extends CacheableProcessorModel<TMSIdPair<TileId>, TileId, List<GraphicsModel>> {

        protected ModelMaker(ProcessorCacheStorage<TMSIdPair<TileId>, List<GraphicsModel>> storage) {
            super(storage);
        }

        @Override
        protected SequentialBuilder<TMSIdPair<TileId>, TileId, List<GraphicsModel>> getSequentialBuilder() {
            return TileMapService.this.getModelSequentialBuilder()
                    .then(BTRUtil.uncheckedCast(MODEL_BAKER));
        }

        @Override
        protected void deleteResource(List<GraphicsModel> graphicsModels) {
            for(GraphicsModel model : graphicsModels) {
                McConnector.client().glGraphicsManager.deleteTextureObject(model.getTextureObject());
            }
        }
    }

    public static class Serializer extends JsonSerializer<TileMapService<?>> {
        @Override
        public void serialize(TileMapService<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Class<? extends TileMapService<?>> clazz = BTRUtil.uncheckedCast(value.getClass());
            String type = TileMapServiceParseRegistries.TYPE_MAP.inverse().get(clazz);
            if(type == null) {
                throw JsonMappingException.from(gen, "unknown map class: " + clazz);
            }

            gen.writeObject(value);
        }
    }

    public static class Deserializer extends JsonDeserializer<TileMapService<?>> {
        @Override
        public TileMapService<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            String type = JsonParserUtil.getOrDefault(node, "type", "flat");
            Class<? extends TileMapService<?>> clazz = TileMapServiceParseRegistries.TYPE_MAP.get(type);
            if(clazz == null) {
                throw JsonMappingException.from(p, "unknown map type: " + type);
            }

            return ctxt.readTreeAsValue(node, clazz);
        }
    }

    protected abstract static class TMSSerializer<T extends TileMapService<?>> extends JsonSerializer<T> {
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

    protected static abstract class TMSDeserializer<T extends TileMapService<?>> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            return this.deserialize(node, CommonYamlObject.read(node), ctxt);
        }
        protected abstract T deserialize(JsonNode node, CommonYamlObject commonYamlObject, DeserializationContext ctxt)
                throws IOException;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    protected static class CommonYamlObject {
        private static final JavaType TRANSLATABLE_STRING_JAVATYPE;
        private static final JavaType TRANSLATABLE_JSONSTRING_JAVATYPE;

        private Translatable<String> name;
        @Nullable
        private Translatable<JsonString> copyrightTextJson;
        private String tileUrl;
        private URL iconUrl;
        private int nThreads;

        private void write(JsonGenerator gen) throws IOException {
            gen.writeObjectField("name", this.name);
            gen.writeStringField("tile_url", this.tileUrl);
            if(this.iconUrl != null) {
                gen.writeStringField("icon_url", this.iconUrl.toString());
            }
            gen.writeNumberField("max_thread", this.nThreads);
            gen.writeObjectField("copyright", this.copyrightTextJson);
        }

        private static CommonYamlObject from(TileMapService<?> tms) {
            CommonYamlObject result = new CommonYamlObject();
            result.name = tms.getName();
            result.tileUrl = tms.getDummyTileUrl();
            result.iconUrl = tms.getIconUrl();
            result.nThreads = tms.getNThreads();
            result.copyrightTextJson = Optional.ofNullable(tms.copyrightTextJson)
                    .map(json -> json.map(JsonString::fromUnsafe))
                    .orElse(null);
            return result;
        }

        private static CommonYamlObject read(JsonNode node) throws IOException {
            CommonYamlObject result = new CommonYamlObject();
            result.name = BTETerraRendererConstants.JSON_MAPPER.treeToValue(node.get("name"), TRANSLATABLE_STRING_JAVATYPE);
            result.tileUrl = node.get("tile_url").asText();
            String iconUrl = JsonParserUtil.getOrDefault(node, "icon_url", null);
            result.iconUrl = iconUrl != null ? new URL(iconUrl) : null;
            result.nThreads = JsonParserUtil.getOrDefault(node, "max_thread", DEFAULT_MAX_THREAD);
            result.copyrightTextJson = BTETerraRendererConstants.JSON_MAPPER.treeToValue(node.get("copyright"), TRANSLATABLE_JSONSTRING_JAVATYPE);

            return result;
        }

        static {
            TypeFactory typeFactory = BTETerraRendererConstants.JSON_MAPPER.getTypeFactory();
            TRANSLATABLE_STRING_JAVATYPE = typeFactory.constructType(new TypeReference<Translatable<String>>() {});
            TRANSLATABLE_JSONSTRING_JAVATYPE = typeFactory.constructType(new TypeReference<Translatable<JsonString>>() {});
        }
    }
}
