package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.graphics.*;
import com.mndk.bteterrarenderer.core.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = TileMapService.Deserializer.class)
public abstract class TileMapService<TileId> {

    private static final Timer TIMER = new Timer();
    public static final int PREPARE_RETRY_COUNT = 3;

    protected static StaticImageIdPair SOMETHING_WENT_WRONG, LOADING;
    private static boolean STATIC_IMAGES_BAKED = false;

    protected final String name;
    protected final ExecutorService downloadExecutor;

    /** Put a localization key as a key, and a property accessor as a value. */
    @Getter
    protected transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();
    private transient final GraphicsModelBaker<TmsIdPair<TileId>> baker = GraphicsModelBaker.getInstance();

    public final void render(Object poseStack, String tmsId, double px, double py, double pz, float opacity) {

        // Bake images
        baker.process();
        bakeStaticImages();

        List<TileId> renderTileIdList = this.getRenderTileIdList(px, py, pz);

        for(TileId tileId : renderTileIdList) {

            GraphicsModel model = null;
            List<GraphicsQuad<?>> nonTexturedModel;

            try {
                TmsIdPair<TileId> idPair = new TmsIdPair<>(tmsId, tileId);
                ModelBakingState bakedState = baker.getModelBakingState(idPair);
                switch (bakedState) {
                    case BAKED:
                        model = baker.updateAndGetModel(idPair);
                        break;
                    case PREPARING:
                    case BAKING:
                        nonTexturedModel = this.getNonTexturedModel(tileId);
                        if (nonTexturedModel != null) {
                            model = new GraphicsModel(LOADING.getId(), nonTexturedModel);
                        }
                        break;
                    case ERROR:
                        nonTexturedModel = this.getNonTexturedModel(tileId);
                        if (nonTexturedModel != null) {
                            model = new GraphicsModel(SOMETHING_WENT_WRONG.getId(), nonTexturedModel);
                        }
                        break;
                    case NOT_BAKED:
                        baker.setModelInPreparingState(idPair);
                        this.downloadExecutor.execute(new TileModelPreparingTask(idPair, 0));
                        break;
                }
            } catch(OutOfProjectionBoundsException ignored) {
            } catch(Exception e) {
                BTETerraRendererConstants.LOGGER.warn("Caught exception while rendering tile: " + tileId, e);
            }

            if (model != null) {
                GraphicsModelVisualManager.drawModel(poseStack, model, px, py - this.getYAlign(), pz, opacity);
            }
        }
    }

    protected double getYAlign() { return 0; }

    protected abstract List<TileId> getRenderTileIdList(double px, double py, double pz);
    protected abstract PreBakedModel getPreBakedModel(TileId tileId) throws IOException, OutOfProjectionBoundsException;
    @Nullable
    protected abstract List<GraphicsQuad<?>> getNonTexturedModel(TileId tileId) throws OutOfProjectionBoundsException;

    @Data
    private class TileModelPreparingTask implements Runnable {

        private final TmsIdPair<TileId> idPair;
        private final int retry;

        @Override
        public void run() {
            if (retry >= PREPARE_RETRY_COUNT) {
                baker.modelPreparingError(idPair);
                return;
            }

            try {
                PreBakedModel preBakedModel = getPreBakedModel(idPair.tileId);
                baker.modelBakingReady(idPair, preBakedModel);
                return;
            } catch(Exception e) {
                BTETerraRendererConstants.LOGGER.error("Caught exception while preparing a tile model (" +
                        "TileId=" + idPair.tileId + ", Retry #" + (retry + 1) + ")", e);
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    downloadExecutor.execute(new TileModelPreparingTask(idPair, retry + 1));
                }
            }, 1000);
        }
    }

    public static class Deserializer extends JsonDeserializer<TileMapService<?>> {
        @Override
        public TileMapService<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            if(!node.has("projection") || !node.get("projection").isTextual()) {
                throw JsonMappingException.from(p, "property \"projection\" missing");
            }

            String projectionName = node.get("projection").asText();
            if(ProjectionYamlLoader.INSTANCE.getResult().containsKey(projectionName)) {
                return ctxt.readTreeAsValue(node, FlatTileMapService.class);
            } else if("ogc3dtiles".equals(projectionName)) {
                return ctxt.readTreeAsValue(node, Ogc3dTileMapService.class);
            }

            throw JsonMappingException.from(p, "unknown projection name" + projectionName);
        }
    }

    @RequiredArgsConstructor
    protected static class StaticImageIdPair {
        private final BufferedImage image;
        @Getter
        private int id = -1;

        private void bake() {
            if(this.id != -1) return;
            this.id = GraphicsModelVisualManager.allocateAndUploadTexture(this.image);
        }
    }

    @Data
    private static class TmsIdPair<TileId> {
        private final String tmsId;
        private final TileId tileId;
    }

    private static void bakeStaticImages() {
        if(STATIC_IMAGES_BAKED) return;
        SOMETHING_WENT_WRONG.bake();
        LOADING.bake();
        STATIC_IMAGES_BAKED = true;
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
