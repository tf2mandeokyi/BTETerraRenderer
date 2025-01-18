package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.JOMLUtils;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.util.Loggers;
import de.javagl.jgltf.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4d;
import org.joml.Vector3d;

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

    // Next, for consistency with the z-up coordinate system of 3D Tiles,
    // glTFs shall be transformed from y-up to z-up at runtime.
    // This is done by rotating the model about the x-axis by pi/2 radians.
    private static final Matrix4d ROTATE_X_AXIS = new Matrix4d().rotateX(Math.PI / 2);

    private final GeographicProjection projection;
    private final SpheroidCoordinatesConverter coordConverter;
    private final boolean rotateModelAlongEarthXAxis;

    public static List<PreBakedModel> convertModel(GltfModel model, Matrix4d transform,
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

    private List<PreBakedModel> convertModel(GltfModel model, Matrix4d transform) {
        return new SingleGltfModelConverter(model).convert(transform);
    }

    @Data
    private class SingleGltfModelConverter {
        private final GltfModel topLevelModel;
        private final List<PreBakedModel> models = new ArrayList<>();

        private List<PreBakedModel> convert(Matrix4d transform) {
            models.clear();

            CesiumRTC cesiumRTC = GltfExtensionsUtil.getExtension(this.topLevelModel, CesiumRTC.class);
            Matrix4d newTransform = new Matrix4d(transform);
            if (cesiumRTC != null) newTransform.translate(cesiumRTC.getCenter());

            for (SceneModel scene : this.topLevelModel.getSceneModels()) {
                this.convertSceneModel(scene, newTransform);
            }
            return models;
        }

        private void convertSceneModel(SceneModel sceneModel, Matrix4d transform) {
            for (NodeModel node : sceneModel.getNodeModels()) {
                this.convertNodeModel(node, transform);
            }
        }

        private void convertNodeModel(NodeModel nodeModel, Matrix4d transform) {
            float[] matrixArray = nodeModel.getMatrix();
            Matrix4d newTransform = new Matrix4d(transform);

            if (matrixArray == null) {
                // TODO: Wait for JglTF to support double precision for translation, rotation, and scale
                float[] translationArray = nodeModel.getTranslation();
                float[] rotationArray = nodeModel.getRotation();
                float[] scaleArray = nodeModel.getScale();

                if (translationArray != null) {
                    newTransform.translate(new Vector3d(translationArray));
                }
                if (rotationArray != null) {
                    newTransform.rotate(JOMLUtils.quaternionXYZW(rotationArray).normalize());
                }
                if (scaleArray != null) {
                    newTransform.scale(new Vector3d(scaleArray));
                }
            }
            else {
                newTransform.mul(JOMLUtils.columnMajor4d(matrixArray));
            }

            if (rotateModelAlongEarthXAxis) {
                newTransform.mulLocal(ROTATE_X_AXIS);
            }

            for (MeshModel mesh : nodeModel.getMeshModels()) {
                this.convertMeshModel(mesh, newTransform);
            }
            for (NodeModel child : nodeModel.getChildren()) {
                this.convertNodeModel(child, newTransform);
            }
        }

        private void convertMeshModel(MeshModel meshModel, Matrix4d transform) {
            for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                this.convertMeshPrimitiveModel(meshPrimitiveModel, transform);
            }
        }

        private void convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Matrix4d transform) {
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
                                                                              Matrix4d transform,
                                                                              DracoMeshCompression draco) {
            AbstractMeshPrimitiveModelConverter.Context context = new AbstractMeshPrimitiveModelConverter.Context(
                    transform, projection, coordConverter);
            if (draco != null) {
                List<BufferViewModel> bufferViewModels = this.topLevelModel.getBufferViewModels();
                return new DracoCompressedMeshConverter(meshPrimitiveModel, bufferViewModels, draco, context);
            } else {
                return new DefaultModelConverter(meshPrimitiveModel, context);
            }
        }
    }

}
