package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.graphics.*;
import com.mndk.bteterrarenderer.core.loader.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.Ogc3dTileMapService;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.processor.ProcessingState;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = TileMapService.Deserializer.class)
public abstract class TileMapService<TileId> {

    private static final IOException LIGHT_IOEXCEPTION = new IOException();

    private static final Timer TIMER = new Timer();
    public static final int DEFAULT_MAX_THREAD = 2;

    protected static StaticImageIdPair SOMETHING_WENT_WRONG, LOADING;
    private static boolean STATIC_IMAGES_BAKED = false;

    protected final String name;
    protected transient final TileResourceFetcher<TileId> resourceFetcher;

    /**
     * This property should be configured on the constructor.
     * One should put localization key as a key, and the property accessor as a value.
     * */
    @Getter
    private transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();
    private transient final GraphicsModelTextureBaker<TmsIdPair<TileId>> modelTextureBaker = GraphicsModelTextureBaker.getInstance();

    public TileMapService(String name, ExecutorService downloadExecutor) {
        this.name = name;
        this.resourceFetcher = new TileResourceFetcher<>(downloadExecutor);
        this.properties.addAll(this.makeProperties());
    }

    public final void render(Object poseStack, String tmsId, double px, double py, double pz, float opacity) {

        // Bake images
        modelTextureBaker.process(2);
        bakeStaticImages();

        List<TileId> renderTileIdList;
        try {
            double[] geoCoord = Projections.getServerProjection().toGeo(px, pz);
            renderTileIdList = this.getRenderTileIdList(tmsId, geoCoord[0], geoCoord[1], py);
        } catch(OutOfProjectionBoundsException e) { return; }

        for(TileId tileId : renderTileIdList) {

            List<GraphicsModel> models = null;
            List<GraphicsQuad<?>> nonTexturedModel;

            try {
                TmsIdPair<TileId> idPair = new TmsIdPair<>(tmsId, tileId);
                ProcessingState bakedState = modelTextureBaker.getResourceProcessingState(idPair);
                switch (bakedState) {
                    case PROCESSED:
                        models = modelTextureBaker.updateAndGetResource(idPair);
                        break;
                    case PREPARING:
                        nonTexturedModel = this.getNonTexturedModel(tileId);
                        if (nonTexturedModel != null) {
                            models = Collections.singletonList(
                                    new GraphicsModel(LOADING.getTextureObject(), nonTexturedModel));
                        }
                        this.prepareResource(idPair, false);
                        break;
                    case PROCESSING:
                        nonTexturedModel = this.getNonTexturedModel(tileId);
                        if (nonTexturedModel != null) {
                            models = Collections.singletonList(
                                    new GraphicsModel(LOADING.getTextureObject(), nonTexturedModel));
                        }
                        break;
                    case ERROR:
                        nonTexturedModel = this.getNonTexturedModel(tileId);
                        if (nonTexturedModel != null) {
                            models = Collections.singletonList(
                                    new GraphicsModel(SOMETHING_WENT_WRONG.getTextureObject(), nonTexturedModel));
                        }
                        break;
                    case NOT_PROCESSED:
                        this.prepareResource(idPair, true);
                        break;
                }
            } catch(OutOfProjectionBoundsException ignored) {
            } catch(Exception e) {
                BTETerraRendererConstants.LOGGER.warn("Caught exception while rendering tile: " + tileId, e);
            }

            if (models != null) for(GraphicsModel model : models) {
                GraphicsModelVisualManager.drawModel(poseStack, model, px, py - this.getYAlign(), pz, opacity);
            }
        }
    }

    private void prepareResource(TmsIdPair<TileId> idPair, boolean firstCalling) {
        try {
            if(firstCalling) modelTextureBaker.setResourceInPreparingState(idPair);
            List<PreBakedModel> preBakedModel = this.getPreBakedModels(idPair);
            if(preBakedModel != null) modelTextureBaker.resourceProcessingReady(idPair, preBakedModel);
        } catch(OutOfProjectionBoundsException e) {
            modelTextureBaker.resourcePreparingError(idPair);
        } catch(Exception e) {
            BTETerraRendererConstants.LOGGER.warn(
                    "Caught exception while rendering tile: " + idPair.getTileId(), e);
            modelTextureBaker.resourcePreparingError(idPair);
        }
    }

    /**
     * Immediately returns the fetched data. If the data is not found, the fetcher fetches it
     * in a separate thread and returns {@code null}.
     * @param tileId The tile id
     * @param url The url
     * @return The data stream. {@code null} if the data is still being fetched
     * @throws IOException If something went wrong while fetching the data
     */
    @Nullable
    protected InputStream fetchData(TileId tileId, URL url) throws IOException {
        ProcessingState fetchState = resourceFetcher.getResourceProcessingState(tileId);
        switch(fetchState) {
            case NOT_PROCESSED:
                resourceFetcher.resourceProcessingReady(tileId, url);
                return null;
            case PROCESSED:
                ByteBuf buf = resourceFetcher.updateAndGetResource(tileId);
                return new ByteBufInputStream(buf);
            case ERROR:
                // Reason doing this is because calling the IOException constructor is heavy
                throw LIGHT_IOEXCEPTION;
            default:
                return null;
        }
    }

    protected double getYAlign() { return 0; }

    /**
     * This method is called only once on the constructor
     * @return The property list
     */
    protected abstract List<PropertyAccessor.Localized<?>> makeProperties();
    /**
     * @param tmsId Tile map service ID
     * @param longitude Player longitude, in degrees
     * @param latitude  Player latitude, in degrees
     * @param height    Player height, in meters
     * @return A list of tile ids
     */
    protected abstract List<TileId> getRenderTileIdList(String tmsId, double longitude, double latitude, double height);
    /**
     * @return {@code null} if the model is not ready, i.e. its data is still being fetched
     * */
    @Nullable
    protected abstract List<PreBakedModel> getPreBakedModels(TmsIdPair<TileId> idPair) throws IOException, OutOfProjectionBoundsException;
    @Nullable
    protected abstract List<GraphicsQuad<?>> getNonTexturedModel(TileId tileId) throws OutOfProjectionBoundsException;

    @RequiredArgsConstructor
    protected static class StaticImageIdPair {
        private final BufferedImage image;
        @Getter
        private Object textureObject = null;

        private void bake() {
            if(this.textureObject != null) return;
            this.textureObject = GraphicsModelVisualManager.allocateAndGetTextureObject(this.image);
        }
    }

    private static void bakeStaticImages() {
        if(STATIC_IMAGES_BAKED) return;
        SOMETHING_WENT_WRONG.bake();
        LOADING.bake();
        STATIC_IMAGES_BAKED = true;
    }

    public static class Deserializer extends JsonDeserializer<TileMapService<?>> {
        @Override
        public TileMapService<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            if(!node.has("projection") || !node.get("projection").isTextual()) {
                throw JsonMappingException.from(p, "property \"projection\" missing");
            }

            String projectionName = node.get("projection").asText();
            if(FlatTileProjectionYamlLoader.INSTANCE.getResult().containsKey(projectionName)) {
                return ctxt.readTreeAsValue(node, FlatTileMapService.class);
            } else if("ogc3dtiles".equals(projectionName)) {
                return ctxt.readTreeAsValue(node, Ogc3dTileMapService.class);
            }

            throw JsonMappingException.from(p, "unknown projection name" + projectionName);
        }
    }

    static {
        try {
            ClassLoader loader = FlatTileMapService.class.getClassLoader();

            String errorImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error.png";
            InputStream errorImageStream = loader.getResourceAsStream(errorImagePath);
            SOMETHING_WENT_WRONG = new StaticImageIdPair(ImageIO.read(Objects.requireNonNull(errorImageStream)));

            String loadingImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/loading.png";
            InputStream loadingImageStream = loader.getResourceAsStream(loadingImagePath);
            LOADING = new StaticImageIdPair(ImageIO.read(Objects.requireNonNull(loadingImageStream)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
