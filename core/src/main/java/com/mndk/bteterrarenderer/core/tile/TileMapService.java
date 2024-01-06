package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelTextureBakingBlock;
import com.mndk.bteterrarenderer.core.graphics.ImageTexturePair;
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
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.graphics.IBufferBuilder;
import com.mndk.bteterrarenderer.mcconnector.graphics.format.PosTex;
import com.mndk.bteterrarenderer.mcconnector.graphics.shape.GraphicsShape;
import lombok.*;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = TileMapService.Deserializer.class)
public abstract class TileMapService<TileId> implements AutoCloseable {

    public static final ProcessorCacheStorage<TMSIdPair<?>, List<GraphicsModel>> STORAGE
            = new ProcessorCacheStorage<>(1000 * 60 * 30 /* 30 minutes */, 10000, false);
    private static final GraphicsModelTextureBakingBlock<?> MODEL_BAKER = new GraphicsModelTextureBakingBlock<>();
    public static final int DEFAULT_MAX_THREAD = 2;

    private static final ImageTexturePair SOMETHING_WENT_WRONG, LOADING;
    private static boolean STATIC_IMAGES_BAKED = false;

    protected final int nThreads;
    private final Translatable<String> name;
    @Nullable private final Translatable<String> copyrightTextJson;
    @Nullable private final URL iconUrl;
    @Setter private Object iconTextureObject;

    /**
     * This property should be configured on the constructor.
     * One should put localization key as a key, and the property accessor as a value.
     * */
    private transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();
    private transient ModelMaker modelMaker; // late init

    protected TileMapService(CommonYamlObject commonYamlObject) {
        this.name = commonYamlObject.name;
        this.iconUrl = commonYamlObject.iconUrl;
        this.copyrightTextJson = Optional.ofNullable(commonYamlObject.copyrightTextJson)
                .map(json -> json.map(JsonString::getValue))
                .orElse(null);
        this.nThreads = commonYamlObject.nThreads;
        this.properties.addAll(this.makeProperties());
    }

    public final void render(Object poseStack, double px, double py, double pz, float opacity) {

        // Bake textures
        this.preRender(px, py, pz);
        MODEL_BAKER.process(2);
        if(!STATIC_IMAGES_BAKED) {
            SOMETHING_WENT_WRONG.bake();
            LOADING.bake();
            STATIC_IMAGES_BAKED = true;
        }

        // Get tileId list
        List<TileId> renderTileIdList;
        try {
            double[] geoCoord = Projections.getServerProjection().toGeo(px, pz);
            renderTileIdList = this.getRenderTileIdList(geoCoord[0], geoCoord[1], py);
        } catch(OutOfProjectionBoundsException e) { return; }

        // Render tileId list
        for(TileId tileId : renderTileIdList) {
            List<GraphicsModel> models = this.getModelsForId(tileId);
            if (models == null) continue;
            for(GraphicsModel model : models) {
                this.drawModel(poseStack, model, px, py - this.getYAlign(), pz, opacity);
            }
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
            case ERROR:
                List<GraphicsShape<?>> nonTexturedModel;
                try {
                    nonTexturedModel = this.getNonTexturedModel(tileId);
                } catch(OutOfProjectionBoundsException ignored) { break; }

                if (nonTexturedModel != null) {
                    ImageTexturePair texturePair = bakedState == ProcessingState.ERROR ? SOMETHING_WENT_WRONG : LOADING;
                    GraphicsModel model = new GraphicsModel(texturePair.getTextureObject(), nonTexturedModel);
                    return Collections.singletonList(model);
                }
                break;
        }
        return null;
    }

    private void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GlGraphicsManager.INSTANCE.setPositionTexColorShader();
        GlGraphicsManager.INSTANCE.setShaderTexture(model.getTextureObject());
        IBufferBuilder<?> bufferBuilder = IBufferBuilder.getTessellatorInstance();

        if(!model.getQuads().isEmpty()) {
            bufferBuilder.beginPTCQuads();
            drawShapeList(poseStack, model.getQuads(), px, py, pz, opacity);
            bufferBuilder.drawAndRender();
        }
        if(!model.getTriangles().isEmpty()) {
            bufferBuilder.beginPTCTriangles();
            drawShapeList(poseStack, model.getTriangles(), px, py, pz, opacity);
            bufferBuilder.drawAndRender();
        }
    }

    private void drawShapeList(Object poseStack, List<? extends GraphicsShape<?>> shapes, double px, double py, double pz, float opacity) {
        for(GraphicsShape<?> shape : shapes) {
            if(shape.getVertexClass() != PosTex.class) {
                throw new UnsupportedOperationException("Not implemented");
            }
            this.drawShape(poseStack, shape, px, py, pz, opacity);
        }
    }

    protected void drawShape(Object poseStack, GraphicsShape<?> shape, double px, double py, double pz, float opacity) {
        IBufferBuilder<Object> bufferBuilder = IBufferBuilder.getTessellatorInstance();

        for (int i = 0; i < shape.getVerticesCount(); i++) {
            PosTex vertex = (PosTex) shape.getVertex(i);
            float x = (float) (vertex.x - px);
            float y = (float) (vertex.y - py);
            float z = (float) (vertex.z - pz);
            bufferBuilder.ptc(poseStack, x, y, z, vertex.u, vertex.v, 1f, 1f, 1f, opacity);
        }
    }

    public CacheableProcessorModel<TMSIdPair<TileId>, TileId, List<GraphicsModel>> getModelMaker() {
        if (this.modelMaker == null) this.modelMaker = new ModelMaker(BTRUtil.uncheckedCast(STORAGE));
        return this.modelMaker;
    }

    @Override
    public final String toString() {
        String name = this.name.get();
        int hash = this.hashCode();
        return String.format("%s(name=%s,hash=%08x)", this.getClass().getSimpleName(), name, hash);
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public void close() throws IOException {
        if(this.modelMaker != null) this.modelMaker.close();
    }

    protected double getYAlign() { return 0; }

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
    protected abstract List<GraphicsShape<?>> getNonTexturedModel(TileId tileId) throws OutOfProjectionBoundsException;

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
                GlGraphicsManager.INSTANCE.deleteTextureObject(model.getTextureObject());
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<TileMapService<?>> {
        @Override
        public TileMapService<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            String type = JsonParserUtil.getOrDefault(node, "type", "flat");
            Class<? extends TileMapService<?>> clazz = TileMapServiceParseRegistries.TYPE_MAP.get(type);
            if(clazz == null) {
                throw JsonMappingException.from(p, "unknown map type" + type);
            }

            return ctxt.readTreeAsValue(node, clazz);
        }
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

        public static CommonYamlObject from(JsonNode node) throws MalformedURLException, JsonProcessingException {
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
            TypeReference<Translatable<String>> stringTypeRef = new TypeReference<Translatable<String>>() {};
            TRANSLATABLE_STRING_JAVATYPE = BTETerraRendererConstants.JSON_MAPPER.getTypeFactory().constructType(stringTypeRef);

            TypeReference<Translatable<JsonString>> jsonStringTypeRef = new TypeReference<Translatable<JsonString>>() {};
            TRANSLATABLE_JSONSTRING_JAVATYPE = BTETerraRendererConstants.JSON_MAPPER.getTypeFactory().constructType(jsonStringTypeRef);
        }
    }

    static {
        try {
            ClassLoader loader = TileMapService.class.getClassLoader();

            String errorImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error.png";
            InputStream errorImageStream = loader.getResourceAsStream(errorImagePath);
            SOMETHING_WENT_WRONG = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(errorImageStream)));
            errorImageStream.close();

            String loadingImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/loading.png";
            InputStream loadingImageStream = loader.getResourceAsStream(loadingImagePath);
            LOADING = new ImageTexturePair(ImageIO.read(Objects.requireNonNull(loadingImageStream)));
            loadingImageStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
