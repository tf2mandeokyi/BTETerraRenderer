package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import de.javagl.jgltf.model.GltfModel;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock
        extends MultiThreadedBlock<TileGlobalKey, Triple<Matrix4f, TileData, Ogc3dTileMapService>, List<PreBakedModel>> {

    protected Ogc3dTileParsingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected List<PreBakedModel> processInternal(TileGlobalKey key, @Nonnull Triple<Matrix4f, TileData, Ogc3dTileMapService> triple) {
        Matrix4f transform = triple.getLeft();
        TileData tileData = triple.getMiddle();

        GltfModel gltfModel = tileData.getGltfModelInstance();
        if(gltfModel == null) return Collections.emptyList();

        Ogc3dTileMapService tms = triple.getRight();
        SpheroidCoordinatesConverter coordConverter = tms.getCoordConverter();
        boolean rotateModelAlongEarthXAxis = tms.isRotateModelAlongEarthXAxis();
        return GltfModelConverter.convertModel(gltfModel, transform, Projections.getHologramProjection(),
                coordConverter, rotateModelAlongEarthXAxis);
    }
}
