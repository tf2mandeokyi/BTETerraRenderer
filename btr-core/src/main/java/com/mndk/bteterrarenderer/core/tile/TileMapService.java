package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.graphics.ModelGraphicsManager;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelBaker;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.loader.ProjectionYamlLoader;
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
public abstract class TileMapService {

    public static final int DOWNLOAD_RETRY_COUNT = 3;
    protected static ImageIdPair SOMETHING_WENT_WRONG, LOADING;
    private static boolean BAKED = false;

    protected final String name;
    protected final ExecutorService downloadExecutor;

    /** Put a localization key as a key, and a property accessor as a value. */
    @Getter
    protected transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();

    private transient Set<GraphicsModel> modelStage = new HashSet<>();

    public final void render(Object poseStack, String tmsId, double px, double py, double pz, float opacity) {
        GraphicsModelBaker.INSTANCE.process();
        bakeStaticImages();
        Set<GraphicsModel> models = this.getTileModels(poseStack, tmsId, px, py, pz);
        if(models != null) modelStage = models;

        for(GraphicsModel model : modelStage) {
            ModelGraphicsManager.drawModel(poseStack, model, px, py - this.getYAlign(), pz, opacity);
        }
    }

    protected double getYAlign() { return 0; }

    /**
     * @return A list of {@link GraphicsModel}. null for no updates
     */
    @Nullable
    protected abstract Set<GraphicsModel> getTileModels(Object poseStack, String tmsId, double px, double py, double pz);

    private static void bakeStaticImages() {
        if(BAKED) return;
        SOMETHING_WENT_WRONG.bake();
        LOADING.bake();
        BAKED = true;
    }

    static {
        try {
            ClassLoader loader = FlatTileMapService.class.getClassLoader();

            String errorImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error.png";
            InputStream errorImageStream = loader.getResourceAsStream(errorImagePath);
            SOMETHING_WENT_WRONG = new ImageIdPair(ImageIO.read(Objects.requireNonNull(errorImageStream)));

            String loadingImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/loading.png";
            InputStream loadingImageStream = loader.getResourceAsStream(loadingImagePath);
            LOADING = new ImageIdPair(ImageIO.read(Objects.requireNonNull(loadingImageStream)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Deserializer extends JsonDeserializer<TileMapService> {
        @Override
        public TileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            if(!node.has("projection") || !node.get("projection").isTextual()) {
                throw JsonMappingException.from(p, "property \"projection\" missing");
            }

            String projectionName = node.get("projection").asText();
            if(ProjectionYamlLoader.INSTANCE.getResult().containsKey(projectionName)) {
                return ctxt.readTreeAsValue(node, FlatTileMapService.class);
            } else if("ogc3d".equals(projectionName)) {
                return ctxt.readTreeAsValue(node, Ogc3dTileMapService.class);
            }

            throw JsonMappingException.from(p, "unknown projection name" + projectionName);
        }
    }

    @RequiredArgsConstructor
    protected static class ImageIdPair {
        private final BufferedImage image;
        @Getter
        private int id = -1;

        private void bake() {
            if(this.id != -1) return;
            this.id = ModelGraphicsManager.allocateAndUploadTexture(this.image);
        }
    }
}
