package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.*;
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
@RequiredArgsConstructor
public class GltfModelConverter {

    private final Matrix4 transform;
    private final GeographicProjection projection;
    private final Ogc3dTileMapService parentService;

    public static List<PreBakedModel> convertModel(GltfModel model, Matrix4 transform,
                                                   GeographicProjection projection, Ogc3dTileMapService parentService) {
        return new GltfModelConverter(transform, projection, parentService)
                .convertModel(model);
    }

    private List<PreBakedModel> convertModel(GltfModel model) {
        return new SingleGltfModelConverter(model).convert();
    }

    @Data
    private class SingleGltfModelConverter {
        private final GltfModel topLevelModel;
        private final List<PreBakedModel> models = new ArrayList<>();

        private List<PreBakedModel> convert() {
            models.clear();
            CesiumRTC cesiumRTC = GltfExtensionsUtil.getExtension(this.topLevelModel, CesiumRTC.class);
            Cartesian3 translation = cesiumRTC != null ? cesiumRTC.getCenter() : Cartesian3.ORIGIN;
            for(SceneModel scene : this.topLevelModel.getSceneModels()) {
                this.convertSceneModel(scene, translation);
            }
            return models;
        }

        private void convertSceneModel(SceneModel sceneModel, Cartesian3 translation) {
            for(NodeModel node : sceneModel.getNodeModels()) {
                this.convertNodeModel(node, translation);
            }
        }

        private void convertNodeModel(NodeModel nodeModel, Cartesian3 translation) {
            float[] nodeTranslation = nodeModel.getTranslation();
            if(nodeTranslation != null) {
                translation = translation.add(new Cartesian3(nodeTranslation[0], nodeTranslation[1], nodeTranslation[2]));
            }
            for(MeshModel mesh : nodeModel.getMeshModels()) {
                this.convertMeshModel(mesh, translation);
            }
            for(NodeModel child : nodeModel.getChildren()) {
                this.convertNodeModel(child, translation);
            }
        }

        private void convertMeshModel(MeshModel meshModel, Cartesian3 translation) {
            for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                this.convertMeshPrimitiveModel(meshPrimitiveModel, translation);
            }
        }

        private void convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Cartesian3 translation) {
            DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
            AbstractMeshPrimitiveModelConverter converter = this.primitiveModelToConverter(meshPrimitiveModel, translation, draco);

            try {
                PreBakedModel model = converter.convert();
                models.add(model);
            } catch (Exception e) {
                Loggers.get(this).error("Failed to convert mesh primitive model", e);
            }
        }

        @Nonnull
        private AbstractMeshPrimitiveModelConverter primitiveModelToConverter(MeshPrimitiveModel meshPrimitiveModel,
                                                                              Cartesian3 translation,
                                                                              DracoMeshCompression draco) {
            SingleGltfModelParsingContext context = new SingleGltfModelParsingContext(
                    translation, transform, projection,
                    parentService.getCoordConverter(),
                    parentService.isRotateModelAlongEarthXAxis());
            if(draco != null) {
                List<BufferViewModel> bufferViewModels = this.topLevelModel.getBufferViewModels();
                return new DracoCompressedMeshConverter(meshPrimitiveModel, bufferViewModels, draco, context);
            } else {
                return new DefaultModelConverter(meshPrimitiveModel, context);
            }
        }
    }

}
