package com.mndk.bteterrarenderer.tile;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnector;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.loader.CategoryMap;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@JsonDeserialize(using = TileMapService.Deserializer.class)
public abstract class TileMapService {

    public static final int DOWNLOAD_RETRY_COUNT = 3;
    public static BufferedImage SOMETHING_WENT_WRONG;

    /**
     * Put a localization key as a key, and a property accessor as a value.
     * TODO: Save this as a file
     */
    @Getter
    protected transient final List<PropertyAccessor.Localized<?>> properties = new ArrayList<>();

    @Getter @Setter
    protected transient CategoryMap.Category<TileMapService> category;
    private transient final Set<GraphicsModel> modelStage = new HashSet<>();

    @Getter
    protected final String name;
    protected final ExecutorService downloadExecutor;

    public final void render(Object poseStack, String tmsId, double px, double py, double pz, float opacity) {
        Set<GraphicsModel> models = this.getTileModels(poseStack, tmsId, px, py, pz);

        if(models != null) {
            modelStage.removeIf(model -> !models.contains(model));
            models.stream().filter(model -> !modelStage.contains(model)).forEach(modelStage::add);
        }

        for(GraphicsModel model : modelStage) {
            //  - (settings.getFlatMapYAxis() + Y_EPSILON)
            ModelGraphicsConnector.INSTANCE.drawModel(poseStack, model, px, py - this.getYAlign(), pz, opacity);
        }
    }

    protected double getYAlign() { return 0; }

    /**
     * @return A list of {@link GraphicsModel}. null for no updates
     */
    @Nullable
    protected abstract Set<GraphicsModel> getTileModels(Object poseStack, String tmsId, double px, double py, double pz);

    static {
        try {
            String wrongImagePath = "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error_image.png";
            InputStream wrongImage = FlatTileMapService.class.getClassLoader().getResourceAsStream(wrongImagePath);
            SOMETHING_WENT_WRONG = ImageIO.read(Objects.requireNonNull(wrongImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Deserializer extends JsonDeserializer<TileMapService> {
        @Override
        public TileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
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
}
