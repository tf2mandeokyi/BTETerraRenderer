package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnector;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.util.PropertyAccessor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public abstract class TileMapService implements CategoryMapData.ICategoryMapProperty {

    public static final int DOWNLOAD_RETRY_COUNT = 3;
    public static BufferedImage SOMETHING_WENT_WRONG;

    @Getter @Setter
    protected transient String source = "";
    private transient final Set<GraphicsModel> modelStage = new HashSet<>();
    @Getter
    protected transient final Map<String, PropertyAccessor<?>> properties = new HashMap<>();

    public final void render(Object poseStack, String tmsId, double px, double py, double pz, float opacity) {
        Set<GraphicsModel> models = this.getTileModels(poseStack, tmsId, px, py, pz);

        if(models != null) {
            modelStage.removeIf(model -> !models.contains(model));
            models.stream().filter(model -> !modelStage.contains(model)).forEach(modelStage::add);
        }

        for(GraphicsModel model : modelStage) {
            ModelGraphicsConnector.INSTANCE.drawModel(poseStack, model, px, py, pz, opacity);
        }
    }

    /**
     * @return A list of {@link GraphicsModel}. null if no updates
     */
    @Nullable
    protected abstract Set<GraphicsModel> getTileModels(Object poseStack, String tmsId,
                                                        double px, double py, double pz);

    static {
        try {
            SOMETHING_WENT_WRONG = ImageIO.read(
                    Objects.requireNonNull(FlatTileMapService.class.getClassLoader().getResourceAsStream(
                            "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error_image.png"
                    ))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
