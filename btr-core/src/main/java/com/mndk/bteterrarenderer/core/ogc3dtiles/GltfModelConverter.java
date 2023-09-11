package com.mndk.bteterrarenderer.core.ogc3dtiles;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.KHRDracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class GltfModelConverter {

    private static final BufferedImage WHITE_BLANK_IMAGE;

    private final Matrix4 transform;
    private final GeographicProjection projection;

    public static List<PreBakedModel> convertModel(GltfModel model, Matrix4 transform,
                                                   GeographicProjection projection) {
        return new GltfModelConverter(transform, projection).convertModel(model);
    }

    private List<PreBakedModel> convertModel(GltfModel model) {
        return new SingleGltfModelConverter(model).convert();
    }

    @Data
    private class SingleGltfModelConverter {
        private final GltfModel topLevelModel;
        @Nullable
        private final CesiumRTC cesiumRTC;

        private SingleGltfModelConverter(GltfModel topLevelModel) {
            this.topLevelModel = topLevelModel;
            this.cesiumRTC = GltfExtensionsUtil.getExtension(topLevelModel, CesiumRTC.class);
        }

        private List<PreBakedModel> convert() {
            return this.topLevelModel.getSceneModels().stream()
                    .flatMap(this::convertSceneModel)
                    .collect(Collectors.toList());
        }

        private Stream<PreBakedModel> convertSceneModel(SceneModel sceneModel) {
            return sceneModel.getNodeModels().stream()
                    .flatMap(this::convertNodeModel);
        }

        private Stream<PreBakedModel> convertNodeModel(NodeModel nodeModel) {
            Stream<PreBakedModel> models = nodeModel.getMeshModels().stream()
                    .flatMap(this::convertMeshModel);
            Stream<PreBakedModel> childModels = nodeModel.getChildren().stream()
                    .flatMap(this::convertNodeModel);

            return Stream.concat(models, childModels);
        }

        private Stream<PreBakedModel> convertMeshModel(MeshModel meshModel) {
            return meshModel.getMeshPrimitiveModels().stream()
                    .map(this::convertMeshPrimitiveModel);
        }

        private PreBakedModel convertMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel) {
            KHRDracoMeshCompression draco =
                    GltfExtensionsUtil.getExtension(meshPrimitiveModel, KHRDracoMeshCompression.class);
            // TODO: Add draco decompression

            ParsedIndex[] parsedIndices = this.parseIndices(meshPrimitiveModel);
            return bakeParsedIndices(meshPrimitiveModel, parsedIndices);
        }

        private ParsedIndex[] parseIndices(MeshPrimitiveModel meshPrimitiveModel) {
            AccessorModel positionAccessor = meshPrimitiveModel.getAttributes().get("POSITION");
            AccessorModel textureCoordAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
            if(textureCoordAccessor == null) {
                BTETerraRendererConstants.LOGGER.warn("texture coord accessor is null");
            }

            int size = positionAccessor.getCount();
            ParsedIndex[] parsedIndices = new ParsedIndex[size];
            for(int i = 0; i < size; i++) {
                parsedIndices[i] = new ParsedIndex(
                        readPosition(positionAccessor, i),
                        readTexture(textureCoordAccessor, i)
                );
            }

            return parsedIndices;
        }

        private Cartesian3 readPosition(AccessorModel positionAccessor, int index) {
            AccessorData data = positionAccessor.getAccessorData();
            float[] position = readFloatArray(data, index, 3);
            Cartesian3 cartesian = new Cartesian3(position[0], position[1], position[2]);

            Web3dQuantizedAttributes extension = GltfExtensionsUtil.getExtension(positionAccessor, Web3dQuantizedAttributes.class);
            if(extension != null) cartesian = cartesian.transform(extension.getDecodeMatrix());
            if(this.cesiumRTC != null) cartesian = cartesian.add(this.cesiumRTC.getCenter());

            return cartesian;
        }

        private float[] readTexture(@Nullable AccessorModel textureCoordAccessor, int index) {
            // Return random point if the texture coordinate accessor is null
            if(textureCoordAccessor == null) return new float[] { (float) Math.random(), (float) Math.random() };

            AccessorData data = textureCoordAccessor.getAccessorData();
            return readFloatArray(data, index, 2);
        }

        private float[] readNormal(AccessorModel normalAccessor, int index) {
            // TODO
            throw new UnsupportedOperationException("Not implemented");
        }

        private PreBakedModel bakeParsedIndices(MeshPrimitiveModel meshPrimitiveModel, ParsedIndex[] indices) {
            // TODO: Add normal
            List<GraphicsQuad<?>> quadList = new ArrayList<>(indices.length / 3);
            AccessorModel indicesAccessor = meshPrimitiveModel.getIndices();

            int meshMode = meshPrimitiveModel.getMode();
            if(meshMode == MeshPrimitiveModelModes.TRIANGLES) {
                for(int i = 0; i < indices.length; i += 3) {
                    int[] meshIndices = indicesAccessor != null ? new int[] {
                            readInteger(indicesAccessor, i),
                            readInteger(indicesAccessor, i + 1),
                            readInteger(indicesAccessor, i + 2)
                    } : new int[] { i, i+1, i+2 };

                    GraphicsQuad.PosTexColor vertex1 = indices[meshIndices[0]].getGraphicsVertex();
                    GraphicsQuad.PosTexColor vertex2 = indices[meshIndices[1]].getGraphicsVertex();
                    GraphicsQuad.PosTexColor vertex3 = indices[meshIndices[2]].getGraphicsVertex();
                    quadList.add(new GraphicsQuad<>(vertex1, vertex2, vertex3, vertex1));
                }
            } else {
                throw new RuntimeException("meshMode not supported: " + meshMode);
            }

            // TODO: Add texture reading
            // TODO: Add compressedImage3DTiles extension

            return new PreBakedModel(WHITE_BLANK_IMAGE, quadList);
        }
    }

    private static int readInteger(@Nonnull AccessorModel indicesAccessor, int defaultIndex) {
        AccessorData indicesData = indicesAccessor.getAccessorData();
        if(indicesData == null) return defaultIndex;

        if(indicesData instanceof AccessorByteData) {
            return ((AccessorByteData) indicesData).get(defaultIndex);
        }
        else if(indicesData instanceof AccessorShortData) {
            return ((AccessorShortData) indicesData).get(defaultIndex);
        }
        else if(indicesData instanceof AccessorIntData) {
            return ((AccessorIntData) indicesData).get(defaultIndex);
        }
        else {
            throw new RuntimeException("unsupported indices type: " + indicesData.getComponentType());
        }
    }

    private static float[] readFloatArray(AccessorData data, int elementIndex, int componentCount) {
        float[] result = new float[componentCount];
        if(data instanceof AccessorFloatData) {
            for(int i = 0; i < componentCount; i++) {
                result[i] = ((AccessorFloatData) data).get(elementIndex, i);
            }
            return result;
        }
        else if(data instanceof AccessorIntData) {
            for(int i = 0; i < componentCount; i++) {
                result[i] = ((AccessorIntData) data).get(elementIndex, i);
            }
            return result;
        }
        else if(data instanceof AccessorShortData) {
            AccessorShortData shortData = (AccessorShortData) data;
            for(int i = 0; i < componentCount; i++) {
                int value = shortData.getInt(elementIndex, i);
                result[i] = shortData.isUnsigned() ? value / 65535f : value / 32767f;
            }
            return result;
        }
        else if(data instanceof AccessorByteData) {
            AccessorByteData byteData = (AccessorByteData) data;
            for(int i = 0; i < componentCount; i++) {
                int value = byteData.getInt(elementIndex, i);
                result[i] = byteData.isUnsigned() ? value / 255f : value / 127f;
            }
            return result;
        }
        else {
            throw new RuntimeException("unsupported value type: " + data.getComponentType());
        }
    }

    @Data
    private class ParsedIndex {
        private final double[] gamePos;
        private final float[] tex;

        private ParsedIndex(Cartesian3 cartesian, float[] tex) {
            this.tex = tex;

            Spheroid3 spheroid3 = cartesian.transform(transform).toSpheroidalCoordinate();
            try {
                double[] gameXY = projection.fromGeo(spheroid3.getLongitudeDegrees(), spheroid3.getLatitudeDegrees());
                this.gamePos = new double[] { gameXY[0], spheroid3.getHeight(), gameXY[1] };
            } catch(OutOfProjectionBoundsException e) {
                /* This will never happen */
                throw new RuntimeException(
                        "projection out of bounds: cartesian=" + cartesian + ", spheroid=" + spheroid3, e);
            }
        }

        private GraphicsQuad.PosTexColor getGraphicsVertex() {
            // Matrix + Projection transformation
            return new GraphicsQuad.PosTexColor(
                    gamePos[0], gamePos[1], gamePos[2],
                    tex[0], tex[1], 1f, 1f, 1f, 1f
            );
        }
    }

    static {
        WHITE_BLANK_IMAGE = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        for(int y = 0; y < 256; y++) for(int x = 0; x < 256; x++) {
            WHITE_BLANK_IMAGE.setRGB(x, y, 0xFFFFFFFF);
        }
    }

}
