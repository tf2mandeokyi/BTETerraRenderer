package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key.TileGlobalKey;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import de.javagl.jgltf.model.GltfModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4d;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock
        extends MultiThreadedBlock<TileGlobalKey, Ogc3dTileParsingBlock.Payload, List<PreBakedModel>> {

    // From 6.7.1.6.2.2. y-up to z-up:
    // Next, for consistency with the z-up coordinate system of 3D Tiles,
    // glTFs shall be transformed from y-up to z-up at runtime.
    // This is done by rotating the model about the x-axis by pi/2 radians.
    private static final Matrix4d ROTATE_X_AXIS = new Matrix4d().rotateX(Math.PI / 2);

    protected Ogc3dTileParsingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected List<PreBakedModel> processInternal(TileGlobalKey key, @Nonnull Payload payload) {
        Ogc3dTileMapService tms = payload.tms;
        Matrix4d transform = new Matrix4d(payload.transform);

        // From 6.7.1.6. Transforms:
        // ...
        // More broadly the order of transformations is:
        //  1. glTF node hierarchy transformations
        //  2. glTF y-up to z-up transform
        //  3. Tile glTF transform
        if (tms.isRotateModelAlongEarthXAxis()) {
            transform.mul(ROTATE_X_AXIS);
        }

        TileData tileData = payload.tileData;
        GltfModel gltfModel = tileData.getGltfModelInstance();
        if (gltfModel == null) return Collections.emptyList();

        SpheroidCoordinatesConverter coordConverter = tms.getCoordConverter();
        return GltfModelConverter.convertModel(gltfModel, transform, tms.getHologramProjection(), coordConverter);
    }

    public static Payload payload(Matrix4d transform, TileData tileData, Ogc3dTileMapService tms) {
        return new Payload(transform, tileData, tms);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Payload {
        private final Matrix4d transform;
        private final TileData tileData;
        private final Ogc3dTileMapService tms;
    }
}
