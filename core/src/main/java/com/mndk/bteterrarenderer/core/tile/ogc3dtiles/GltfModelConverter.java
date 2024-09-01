package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Quaternion;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import de.javagl.jgltf.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html">glTF 2.0 Specification</a>
 */
@Getter
@Builder
@RequiredArgsConstructor
public class GltfModelConverter {

    private static final Matrix4f ROTATE_X_AXIS = new Matrix4f((c, r) -> {
        if(c == 1 && r == 1) return 0;
        if(c == 2 && r == 2) return 0;
        if(c == 1 && r == 2) return 1;
        if(c == 2 && r == 1) return -1;
        return c == r ? 1 : 0;
    });

    private final GeographicProjection projection;
    private final SpheroidCoordinatesConverter coordConverter;
    private final boolean rotateModelAlongEarthXAxis;

    public static List<PreBakedModel> convertModel(GltfModel model, Matrix4f transform,
                                                   GeographicProjection projection,
                                                   SpheroidCoordinatesConverter coordConverter,
                                                   boolean rotateModelAlongEarthXAxis) {
        GltfModelConverter converter = GltfModelConverter.builder()
                .projection(projection)
                .coordConverter(coordConverter)
                .rotateModelAlongEarthXAxis(rotateModelAlongEarthXAxis)
                .build();
        return converter.convertModel(model, transform);
    }

    private List<PreBakedModel> convertModel(GltfModel model, Matrix4f transform) {
        return new SingleGltfModelConverter(model).convert(transform);
    }

    @Data
    private class SingleGltfModelConverter {
        private final GltfModel topLevelModel;
        private final List<PreBakedModel> models = new ArrayList<>();

        private List<PreBakedModel> convert(Matrix4f transform) {
            models.clear();

            CesiumRTC cesiumRTC = GltfExtensionsUtil.getExtension(this.topLevelModel, CesiumRTC.class);
            Cartesian3f translation = cesiumRTC != null ? cesiumRTC.getCenter() : Cartesian3f.ORIGIN;
            transform = transform.multiply(Matrix4f.fromTranslation(translation)).toMatrix4();

            for(SceneModel scene : this.topLevelModel.getSceneModels()) {
                this.convertSceneModel(scene, transform);
            }
            return models;
        }

        private void convertSceneModel(SceneModel sceneModel, Matrix4f transform) {
            for(NodeModel node : sceneModel.getNodeModels()) {
                this.convertNodeModel(node, transform);
            }
        }

        private void convertNodeModel(NodeModel nodeModel, Matrix4f transform) {
            float[] translationArray = nodeModel.getTranslation();
            float[] rotationArray = nodeModel.getRotation();
            float[] scaleArray = nodeModel.getScale();

            Matrix4f nodeTranslation = translationArray != null
                    ? Matrix4f.fromTranslation(Cartesian3f.fromArray(translationArray))
                    : Matrix4f.IDENTITY;
            Matrix4f nodeRotation = rotationArray != null
                    ? Matrix4f.fromScaleMatrix(Quaternion.fromArray(rotationArray).toNormalized().toRotationMatrix())
                    : Matrix4f.IDENTITY;
            Matrix4f nodeScale = scaleArray != null
                    ? Matrix4f.fromScale(Cartesian3f.fromArray(scaleArray))
                    : Matrix4f.IDENTITY;

            transform = transform.multiply(nodeTranslation)
                    .multiply(nodeRotation)
                    .multiply(nodeScale).toMatrix4();
            if(rotateModelAlongEarthXAxis) {
                transform = ROTATE_X_AXIS.multiply(transform).toMatrix4();
            }

            for(MeshModel mesh : nodeModel.getMeshModels()) {
                this.convertMeshModel(mesh, transform);
            }
            for(NodeModel child : nodeModel.getChildren()) {
                this.convertNodeModel(child, transform);
            }
        }

        private void convertMeshModel(MeshModel meshModel, Matrix4f transform) {
            for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                this.convertMeshPrimitiveModel(meshPrimitiveModel, transform);
            }
        }

        private void convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Matrix4f transform) {
            DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
            AbstractMeshPrimitiveModelConverter converter = this.primitiveModelToConverter(meshPrimitiveModel, transform, draco);

            try {
                PreBakedModel model = converter.convert();
                models.add(model);
            } catch (Exception e) {
                Loggers.get(this).error("Failed to convert mesh primitive model", e);
            }
        }

        @Nonnull
        private AbstractMeshPrimitiveModelConverter primitiveModelToConverter(MeshPrimitiveModel meshPrimitiveModel,
                                                                              Matrix4f transform,
                                                                              DracoMeshCompression draco) {
            AbstractMeshPrimitiveModelConverter.Context context = new AbstractMeshPrimitiveModelConverter.Context(
                    transform, projection, coordConverter);
            if(draco != null) {
                List<BufferViewModel> bufferViewModels = this.topLevelModel.getBufferViewModels();
                return new DracoCompressedMeshConverter(meshPrimitiveModel, bufferViewModels, draco, context);
            } else {
                return new DefaultModelConverter(meshPrimitiveModel, context);
            }
        }
    }

}
