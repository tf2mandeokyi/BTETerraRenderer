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
import lombok.AccessLevel;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GltfModelConverter {

    private final GltfModel topLevelModel;
    private final GeographicProjection projection;
    private final SpheroidCoordinatesConverter coordConverter;
    private final List<PreBakedModel> models = new ArrayList<>();

    public static List<PreBakedModel> convertModel(GltfModel model, Matrix4d transform,
                                                   GeographicProjection projection,
                                                   SpheroidCoordinatesConverter coordConverter) {
        return new GltfModelConverter(model, projection, coordConverter).convert(transform);
    }

    private List<PreBakedModel> convert(Matrix4d transform) {
        models.clear();

        CesiumRTC cesiumRTC = GltfExtensionsUtil.getExtension(this.topLevelModel, CesiumRTC.class);
        if (cesiumRTC != null) {
            Matrix4d translation = new Matrix4d().translate(cesiumRTC.getCenter());
            translation.mul(transform, transform);
        }

        for (SceneModel scene : this.topLevelModel.getSceneModels()) {
            this.convertSceneModel(scene, transform);
        }
        return models;
    }

    private void convertSceneModel(SceneModel sceneModel, Matrix4d transform) {
        for (NodeModel node : sceneModel.getNodeModels()) {
            this.convertNodeModel(node, transform);
        }
    }

    private void convertNodeModel(NodeModel nodeModel, Matrix4d parentTransform) {
        float[] matrixArray = nodeModel.getMatrix();
        Matrix4d localTransform = new Matrix4d(parentTransform);

        if (matrixArray == null) {
            // TODO: Wait for JglTF to support double precision for translation, rotation, and scale
            Matrix4d nodeTransform = new Matrix4d();
            float[] scaleArray = nodeModel.getScale();
            float[] rotationArray = nodeModel.getRotation();
            float[] translationArray = nodeModel.getTranslation();

            if (scaleArray != null) nodeTransform.scale(new Vector3d(scaleArray));
            if (rotationArray != null) nodeTransform.rotate(JOMLUtils.quaternionXYZW(rotationArray).normalize());
            if (translationArray != null) nodeTransform.translate(new Vector3d(translationArray));

            localTransform.mul(nodeTransform);
        }
        else {
            localTransform.mul(JOMLUtils.columnMajor4d(matrixArray));
        }

        for (MeshModel mesh : nodeModel.getMeshModels()) {
            this.convertMeshModel(mesh, localTransform);
        }
        for (NodeModel child : nodeModel.getChildren()) {
            this.convertNodeModel(child, localTransform);
        }
    }

    private void convertMeshModel(MeshModel meshModel, Matrix4d transform) {
        for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
            this.convertMeshPrimitiveModel(meshPrimitiveModel, transform);
        }
    }

    private void convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Matrix4d transform) {
        AbstractMpmConverter converter = this.primitiveModelToConverter(meshPrimitiveModel, transform);

        try {
            PreBakedModel model = converter.convert();
            models.add(model);
        } catch (Exception e) {
            Loggers.get(this).error("Failed to convert mesh primitive model", e);
        }
    }

    @Nonnull
    private AbstractMpmConverter primitiveModelToConverter(MeshPrimitiveModel meshPrimitiveModel, Matrix4d transform) {
        DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
        if (draco != null) {
            List<BufferViewModel> bufferViewModels = this.topLevelModel.getBufferViewModels();
            return DracoCompressedMpmConverter.builder()
                    .transform(transform)
                    .projection(projection)
                    .coordConverter(coordConverter)
                    .meshPrimitiveModel(meshPrimitiveModel)
                    .topLevelBufferViewModels(bufferViewModels)
                    .extension(draco)
                    .build();
        } else {
            return DefaultMpmConverter.builder()
                    .transform(transform)
                    .projection(projection)
                    .coordConverter(coordConverter)
                    .meshPrimitiveModel(meshPrimitiveModel)
                    .build();
        }
    }

}
