package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.graphics.format.PosTex;
import com.mndk.bteterrarenderer.core.graphics.model.PreBakedModel;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsShape;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.v1.MaterialModelV1;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
            ParsedPoint[] parsedPoints = this.parsePoints(meshPrimitiveModel);
            List<GraphicsShape<?>> shapes = this.parsedPointsToShapes(meshPrimitiveModel, parsedPoints);
            BufferedImage image = this.readMaterialModel(meshPrimitiveModel.getMaterialModel());
            return new PreBakedModel(image, shapes);
        }

        private ParsedPoint[] parsePoints(MeshPrimitiveModel meshPrimitiveModel) {
            AccessorModel positionAccessor = meshPrimitiveModel.getAttributes().get("POSITION");
            AccessorModel textureCoordAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
            if(textureCoordAccessor == null) {
                BTETerraRendererConstants.LOGGER.warn("texture coord accessor is null");
            }

            int size = positionAccessor.getCount();
            ParsedPoint[] parsedPoints = new ParsedPoint[size];
            for(int i = 0; i < size; i++) {
                parsedPoints[i] = new ParsedPoint(
                        readPosition(positionAccessor, i),
                        readTextureCoord(textureCoordAccessor, i)
                );
            }

            return parsedPoints;
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

        private float[] readTextureCoord(@Nullable AccessorModel textureCoordAccessor, int index) {
            // Return random point if the texture coordinate accessor is null
            if(textureCoordAccessor == null) return new float[] { (float) Math.random(), (float) Math.random() };

            AccessorData data = textureCoordAccessor.getAccessorData();
            return readFloatArray(data, index, 2);
        }

        private float[] readNormal(AccessorModel normalAccessor, int index) {
            // TODO
            throw new UnsupportedOperationException("Not implemented");
        }

        private List<GraphicsShape<?>> parsedPointsToShapes(MeshPrimitiveModel meshPrimitiveModel, ParsedPoint[] points) {
            List<GraphicsShape<?>> shapeList;
            AccessorModel indicesAccessor = meshPrimitiveModel.getIndices();
            int meshCount = indicesAccessor != null ? indicesAccessor.getCount() : points.length;

            int meshMode = meshPrimitiveModel.getMode();
            if(meshMode == MeshPrimitiveModelModes.TRIANGLES) {
                shapeList = new ArrayList<>(meshCount / 3);

                for(int i = 0; i < meshCount; i += 3) {
                    int[] meshIndices = indicesAccessor != null ? new int[] {
                            readInteger(indicesAccessor, i),
                            readInteger(indicesAccessor, i + 1),
                            readInteger(indicesAccessor, i + 2)
                    } : new int[] { i, i+1, i+2 };

                    PosTex vertex1 = points[meshIndices[0]].getGraphicsVertex();
                    PosTex vertex2 = points[meshIndices[1]].getGraphicsVertex();
                    PosTex vertex3 = points[meshIndices[2]].getGraphicsVertex();
                    shapeList.add(GraphicsTriangle.newPosTex(vertex1, vertex2, vertex3));
                }
            } else {
                throw new RuntimeException("meshMode not supported: " + meshMode);
            }
            return shapeList;
        }

        private BufferedImage readMaterialModel(MaterialModel materialModel) {
            BufferedImage image = null;
            if(materialModel instanceof MaterialModelV1) {
                throw new UnsupportedOperationException("material model v1 not supported");
            } else if(materialModel instanceof MaterialModelV2) {
                MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;

                TextureModel textureModel = materialModelV2.getBaseColorTexture();
                if(textureModel != null) image = this.readImageModel(textureModel.getImageModel());
                // TODO: read mag/minFilter, wrapS/T from texture
                // TODO: read emissive, normal, occlusion, and roughness texture from material
            }

            return image != null ? image : WHITE_BLANK_IMAGE;
        }

        @Nullable
        private BufferedImage readImageModel(ImageModel imageModel) {
            try {
                ByteBuffer byteBuffer = imageModel.getImageData();
                ByteBuf nettyBuf = Unpooled.copiedBuffer(byteBuffer);
                InputStream stream = new ByteBufInputStream(nettyBuf);

                // TODO: Add compressedImage3DTiles extension

                return ImageIO.read(stream);
            } catch(IOException e) {
                BTETerraRendererConstants.LOGGER.error(e);
                return null;
            }
        }
    }

    private static int readInteger(@Nonnull AccessorModel accessor, int defaultValue) {
        AccessorData data = accessor.getAccessorData();
        if(data == null) return defaultValue;

        if(data instanceof AccessorByteData) {
            return ((AccessorByteData) data).get(defaultValue);
        }
        else if(data instanceof AccessorShortData) {
            return ((AccessorShortData) data).get(defaultValue);
        }
        else if(data instanceof AccessorIntData) {
            return ((AccessorIntData) data).get(defaultValue);
        }
        else {
            throw new RuntimeException("unsupported data type: " + data.getComponentType());
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
    private class ParsedPoint {
        private final double[] gamePos;
        private final float[] tex;

        private ParsedPoint(Cartesian3 cartesian, float[] tex) {
            this.tex = tex;

            Spheroid3 spheroid3 = cartesian.transform(transform).toSpheroidalCoordinate();
            try {
                // Matrix + Projection transformation
                double[] gameXY = projection.fromGeo(spheroid3.getLongitudeDegrees(), spheroid3.getLatitudeDegrees());
                this.gamePos = new double[] { gameXY[0], spheroid3.getHeight(), gameXY[1] };
            } catch(OutOfProjectionBoundsException e) {
                /* This will never happen */
                throw new RuntimeException(
                        "projection out of bounds: cartesian=" + cartesian + ", spheroid=" + spheroid3, e);
            }
        }

        private PosTex getGraphicsVertex() {
            return new PosTex(
                    gamePos[0], gamePos[1], gamePos[2], // position
                    tex[0], tex[1] // texture coordinate
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
