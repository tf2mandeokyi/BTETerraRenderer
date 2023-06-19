package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.graphics.GraphicsModel;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileMapService extends TileMapService {
    public Ogc3dTileMapService(String name, ExecutorService downloadExecutor) {
        super(name, downloadExecutor);
    }

    @Override
    protected Set<GraphicsModel> getTileModels(Object poseStack, String tmsId, double px, double py, double pz) {
        return null;
    }
}
