package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import de.javagl.jgltf.model.GltfModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock
        extends MultiThreadedBlock<TileGlobalKey, Ogc3dTileParsingBlock.Payload, List<PreBakedModel>> {

    protected Ogc3dTileParsingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected List<PreBakedModel> processInternal(TileGlobalKey key, @Nonnull Payload payload) {
        Matrix4f transform = payload.transform;
        TileData tileData = payload.tileData;

        GltfModel gltfModel = tileData.getGltfModelInstance();
        if (gltfModel == null) return Collections.emptyList();

        Ogc3dTileMapService tms = payload.tms;
        SpheroidCoordinatesConverter coordConverter = tms.getCoordConverter();
        boolean rotateModelAlongEarthXAxis = tms.isRotateModelAlongEarthXAxis();
        return GltfModelConverter.convertModel(gltfModel, transform, tms.getHologramProjection(),
                coordConverter, rotateModelAlongEarthXAxis);
    }

    public static Payload payload(Matrix4f transform, TileData tileData, Ogc3dTileMapService tms) {
        return new Payload(transform, tileData, tms);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Payload {
        private final Matrix4f transform;
        private final TileData tileData;
        private final Ogc3dTileMapService tms;
    }
}
