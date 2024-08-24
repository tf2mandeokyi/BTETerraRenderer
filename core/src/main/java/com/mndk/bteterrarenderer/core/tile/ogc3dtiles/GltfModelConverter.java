package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        private SingleGltfModelConverter(GltfModel topLevelModel) {
            this.topLevelModel = topLevelModel;
        }

        private List<PreBakedModel> convert() {
            CesiumRTC cesiumRTC = GltfExtensionsUtil.getExtension(this.topLevelModel, CesiumRTC.class);
            Cartesian3 translation = cesiumRTC != null ? cesiumRTC.getCenter() : Cartesian3.ORIGIN;
            return this.topLevelModel.getSceneModels().stream()
                    .flatMap(scene -> this.convertSceneModel(scene, translation))
                    .collect(Collectors.toList());
        }

        private Stream<PreBakedModel> convertSceneModel(SceneModel sceneModel, Cartesian3 translation) {
            return sceneModel.getNodeModels().stream()
                    .flatMap(node -> this.convertNodeModel(node, translation));
        }

        private Stream<PreBakedModel> convertNodeModel(NodeModel nodeModel, Cartesian3 translation) {
            float[] nodeTranslation = nodeModel.getTranslation();
            if(nodeTranslation != null) {
                translation = translation.add(new Cartesian3(nodeTranslation[0], nodeTranslation[1], nodeTranslation[2]));
            }
            Cartesian3 finalTranslation = translation;
            Stream<PreBakedModel> models = nodeModel.getMeshModels().stream()
                    .flatMap(mesh -> this.convertMeshModel(mesh, finalTranslation));
            Stream<PreBakedModel> childModels = nodeModel.getChildren().stream()
                    .flatMap(node -> this.convertNodeModel(node, finalTranslation));

            return Stream.concat(models, childModels);
        }

        private Stream<PreBakedModel> convertMeshModel(MeshModel meshModel, Cartesian3 translation) {
            return meshModel.getMeshPrimitiveModels().stream()
                    .map(primitive -> this.convertMeshPrimitiveModel(primitive, translation))
                    .filter(Objects::nonNull);
        }

        private PreBakedModel convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Cartesian3 translation) {
            DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
            AbstractMeshPrimitiveModelConverter converter = this.primitiveModelToConverter(meshPrimitiveModel, translation, draco);

            try {
                return converter.convert();
            } catch (Exception e) {
                Loggers.get(this).error("Failed to convert mesh primitive model", e);
                return new PreBakedModel(null, new GraphicsShapes());
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
