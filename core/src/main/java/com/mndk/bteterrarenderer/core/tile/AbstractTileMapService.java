package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFX;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.McFXElement;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.image.McFXImage;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.util.concurrent.CacheStorage;
import com.mndk.bteterrarenderer.util.concurrent.ManualThreadExecutor;
import com.mndk.bteterrarenderer.util.json.JsonString;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    private static final ManualThreadExecutor TEXTURE_BAKER = new ManualThreadExecutor();

    public static final int DEFAULT_MAX_THREAD = 2;

    private final int nThreads;
    private final Translatable<String> name;
    @Nullable private final Translatable<String> copyrightTextJson;
    @Nullable private final URL iconUrl;
    @Nullable private final URL hudImageUrl;
    @Nullable private final GeographicProjection hologramProjection;
    @Getter(value = AccessLevel.PACKAGE)
    private final String dummyTileUrl;

    @Getter @Setter @Nullable
    private transient String source;
    private transient final List<PropertyAccessor.Localized<?>> stateAccessors = new ArrayList<>();
    private transient final ModelStorage storage;
    private transient McFXElement hudElement;
    private final transient McFXElement bakingIndicatorWrapper = McFX.div().setColor(0xFFFFFFFF);
    private final transient McFXImage hudImage = McFX.image().setDimension(null, 32);

    protected AbstractTileMapService(TileMapServiceCommonProperties properties) {
        this.name = properties.getName();
        this.dummyTileUrl = properties.getTileUrl();
        this.iconUrl = properties.getIconUrl();
        this.hudImageUrl = properties.getHudImageUrl();
        this.copyrightTextJson = Optional.ofNullable(properties.getCopyrightTextJson())
                .map(json -> json.map(JsonString::getValue))
                .orElse(null);
        this.nThreads = properties.getNThreads();
        this.hologramProjection = properties.getHologramProjection();
        this.stateAccessors.addAll(this.makeStateAccessors());
        this.storage = new ModelStorage(properties.getCacheConfig());
    }

    @Override
    public final List<GraphicsModel> getModels(McCoord cameraPos, double yawDegrees, double pitchDegrees) {
        // Bake textures
        this.preRender(cameraPos);
        TEXTURE_BAKER.process(2);

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
            return future == null ? null : future.thenApplyAsync(this::bake, TEXTURE_BAKER);
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
                    this.hudImage,
                    element,
                    this.bakingIndicatorWrapper
            );
            if (this.hudImageUrl != null) {
                HttpResourceManager.downloadAsImage(this.hudImageUrl.toString(), null)
                        .thenApplyAsync(
                                image -> McConnector.client().textureManager.allocateAndGetTextureObject(BTETerraRenderer.MODID, image),
                                TEXTURE_BAKER
                        )
                        .whenComplete((texture, error) -> {
                            if (error != null) Loggers.get(this).error("Error while parsing hud image", error);
                            else this.hudImage.setTexture(texture);
                        });
            }
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
     * Returns a future that completes with the list of pre-baked models for the given tile ID.
     * To defer processing (e.g., when textures or data are not yet available), return {@code null}.
     * The storage layer will treat {@code null} as "not ready" and retry later.
     * To indicate an out-of-bounds or error condition, return a non-null future with an empty list.
     * @param tileId The tile ID to process
     * @return A future for the pre-baked models, or {@code null} to defer processing
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

}
