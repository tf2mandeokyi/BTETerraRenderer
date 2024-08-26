package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.GltfModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock
        extends MultiThreadedBlock<TileGlobalKey, Pair<ParsedData, Ogc3dTileMapService>, List<PreBakedModel>> {

    protected Ogc3dTileParsingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected List<PreBakedModel> processInternal(TileGlobalKey key, @Nonnull Pair<ParsedData, Ogc3dTileMapService> pair) {
        ParsedData parsedData = pair.getLeft();
        Matrix4 transform = parsedData.getTransform();
        TileData tileData = parsedData.getTileData();

        GltfModel gltfModel = tileData.getGltfModelInstance();
        if(gltfModel == null) return Collections.emptyList();

        Ogc3dTileMapService tms = pair.getRight();
        return GltfModelConverter.convertModel(gltfModel, transform, Projections.getServerProjection(), tms);
    }
}
