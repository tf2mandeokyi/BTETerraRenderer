package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.TMSIdPair;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.GltfModel;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock extends MultiThreadedBlock<TMSIdPair<TileGlobalKey>, ParsedData, List<PreBakedModel>> {

    protected Ogc3dTileParsingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected List<PreBakedModel> processInternal(TMSIdPair<TileGlobalKey> key, @Nonnull ParsedData preParsedData) {
        Matrix4 transform = preParsedData.getTransform();
        TileData tileData = preParsedData.getTileData();

        GltfModel gltfModel = tileData.getGltfModelInstance();
        if(gltfModel == null) return Collections.emptyList();
        return GltfModelConverter.convertModel(gltfModel, transform, Projections.getServerProjection());
    }
}
