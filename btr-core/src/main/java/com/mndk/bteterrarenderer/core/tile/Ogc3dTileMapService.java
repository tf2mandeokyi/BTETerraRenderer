package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileMapService extends TileMapService<Object> {
    public Ogc3dTileMapService(String name, ExecutorService downloadExecutor) {
        super(name, downloadExecutor);
    }

    @Override
    protected List<Object> getRenderTileIdList(double px, double py, double pz) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected PreBakedModel getPreBakedModel(Object o) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    protected List<GraphicsQuad<?>> getNonTexturedModel(Object o) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }
}
