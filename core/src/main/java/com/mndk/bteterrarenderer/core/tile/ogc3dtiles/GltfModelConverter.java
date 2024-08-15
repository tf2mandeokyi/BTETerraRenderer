package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.CesiumRTC;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.util.QuantizationUtil;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @see <a href="https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html">glTF 2.0 Specification</a>
 */
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
            DracoMeshCompression draco = GltfExtensionsUtil.getExtension(meshPrimitiveModel, DracoMeshCompression.class);
            if(draco != null) {
                ByteBuffer buffer = topLevelModel.getBufferViewModels()
                        .get(draco.getBufferView())
                        .getBufferViewData();
                ByteBuf buf = Unpooled.wrappedBuffer(buffer);
//                DracoDecodeResult decodeResult = DracoDecodeResult.read(new LoggableByteBuf(buf));
            }
            ParsedPoint[] parsedPoints = this.parsePoints(meshPrimitiveModel);
            GraphicsShapes shapes = this.parsedPointsToShapes(meshPrimitiveModel, parsedPoints);
            BufferedImage image = this.readMaterialModel(meshPrimitiveModel.getMaterialModel());
            return new PreBakedModel(image, shapes);
        }

        private ParsedPoint[] parsePoints(MeshPrimitiveModel meshPrimitiveModel) {
            // From 3.7.2.1. Overview:
            //   POSITION   - Unitless XYZ vertex positions
            //   NORMAL     - Normalized XYZ vertex normals
            //   TEXCOORD_n - ST texture coordinates
            AccessorModel positionAccessor = meshPrimitiveModel.getAttributes().get("POSITION");
            AccessorModel normalAccessor = meshPrimitiveModel.getAttributes().get("NORMAL");
            AccessorModel textureCoordAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
            if(textureCoordAccessor == null) {
                Loggers.get(this).warn("texture coord accessor is null");
            }

            int size = positionAccessor.getCount();
            ParsedPoint[] parsedPoints = new ParsedPoint[size];
            for(int i = 0; i < size; i++) {
                parsedPoints[i] = new ParsedPoint(
                        readPosition(positionAccessor, i),
                        readTextureCoord(textureCoordAccessor, i),
                        normalAccessor == null ? null : readNormal(normalAccessor, i)
                );
            }

            return parsedPoints;
        }

        private Cartesian3 readPosition(AccessorModel positionAccessor, int index) {
            Cartesian3 cartesian = this.readCartesian3(positionAccessor, index);
            if(this.cesiumRTC != null) cartesian = cartesian.add(this.cesiumRTC.getCenter());

            return cartesian;
        }

        private float[] readTextureCoord(@Nullable AccessorModel textureCoordAccessor, int index) {
            // Return random point if the texture coordinate accessor is null
            if(textureCoordAccessor == null) return new float[] { (float) Math.random(), (float) Math.random() };

            AccessorData data = textureCoordAccessor.getAccessorData();
            return readFloatArray(data, index, 2);
        }

        private Cartesian3 readNormal(AccessorModel normalAccessor, int index) {
            Cartesian3 cartesian = this.readCartesian3(normalAccessor, index);

            Web3dQuantizedAttributes extension = GltfExtensionsUtil.getExtension(normalAccessor, Web3dQuantizedAttributes.class);
            if(extension != null) cartesian = cartesian.transform(extension.getDecodeMatrix());

            return cartesian;
        }

        private Cartesian3 readCartesian3(AccessorModel accessor, int index) {
            AccessorData data = accessor.getAccessorData();
            float[] position = readFloatArray(data, index, 3);
            return new Cartesian3(position[0], position[1], position[2]);
        }

        private GraphicsShapes parsedPointsToShapes(MeshPrimitiveModel meshPrimitiveModel, ParsedPoint[] points) {
            GraphicsShapes shapes = new GraphicsShapes();
            AccessorModel indicesAccessor = meshPrimitiveModel.getIndices();
            int meshCount = indicesAccessor != null ? indicesAccessor.getCount() : points.length;

            int meshMode = meshPrimitiveModel.getMode();
            if(meshMode == MeshPrimitiveModelModes.TRIANGLES) {
                for(int i = 0; i < meshCount; i += 3) {
                    int[] meshIndices = indicesAccessor != null ? new int[] {
                            readInteger(indicesAccessor, i),
                            readInteger(indicesAccessor, i + 1),
                            readInteger(indicesAccessor, i + 2)
                    } : new int[] { i, i+1, i+2 };

                    ParsedPoint point1 = points[meshIndices[0]];
                    ParsedPoint point2 = points[meshIndices[1]];
                    ParsedPoint point3 = points[meshIndices[2]];

                    ParsedTriangle triangle = new ParsedTriangle(point1, point2, point3);
                    // Simply setting this to TRI_PTN_ALPHA doesn't seem to fix the
                    // transparent triangles overlapping issue...
                    // TODO: Fix this
                    shapes.add(DrawingFormat.TRI_PTN_ALPHA, triangle.toGraphics());
                }
            } else {
                Loggers.get(this).warn("meshMode not supported: {}", meshMode);
            }

            return shapes;
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
                Loggers.get(this).error("Could not read image model", e);
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
                result[i] = QuantizationUtil.normalizeShort(value, shortData.isUnsigned());
            }
            return result;
        }
        else if(data instanceof AccessorByteData) {
            AccessorByteData byteData = (AccessorByteData) data;
            for(int i = 0; i < componentCount; i++) {
                int value = byteData.getInt(elementIndex, i);
                result[i] = QuantizationUtil.normalizeByte(value, byteData.isUnsigned());
            }
            return result;
        }
        else {
            throw new RuntimeException("unsupported value type: " + data.getComponentType());
        }
    }

    @Data
    private class ParsedPoint {
        private final Cartesian3 gamePos;
        private final float[] tex;
        @Nullable
        private final Cartesian3 gameNormal;

        private ParsedPoint(Cartesian3 cartesian, float[] tex, @Nullable Cartesian3 normal) {
            this.tex = tex;

            try {
                // Matrix + Projection transformation
                Spheroid3 s3Position = cartesian.transform(transform).toSpheroidalCoordinate();
                double[] posXY = projection.fromGeo(s3Position.getLongitudeDegrees(), s3Position.getLatitudeDegrees());
                this.gamePos = new Cartesian3(posXY[0], s3Position.getHeight(), posXY[1]);

                if(normal == null) {
                    this.gameNormal = null;
                    return;
                }

                // Matrix + Projection transformation
                Spheroid3 s3Normal = cartesian.add(normal).transform(transform).toSpheroidalCoordinate();
                double[] normXY = projection.fromGeo(s3Normal.getLongitudeDegrees(), s3Normal.getLatitudeDegrees());
                this.gameNormal = new Cartesian3(
                        normXY[0] - this.gamePos.getX(),
                        s3Normal.getHeight() - this.gamePos.getY(),
                        normXY[1] - this.gamePos.getZ()
                );
            } catch(OutOfProjectionBoundsException e) {
                /* This will never happen */
                throw new RuntimeException("projection out of bounds");
            }
        }
    }

    @Data
    private class ParsedTriangle {
        private final Cartesian3[] gamePositions;
        private final float[][] tex;
        private final Cartesian3[] gameNormals;

        private ParsedTriangle(ParsedPoint p1, ParsedPoint p2, ParsedPoint p3) {
            this.gamePositions = new Cartesian3[] { p1.gamePos, p2.gamePos, p3.gamePos };
            this.tex = new float[][] { p1.tex, p2.tex, p3.tex };

            if(p1.gameNormal != null && p2.gameNormal != null && p3.gameNormal != null) {
                this.gameNormals = new Cartesian3[] { p1.gameNormal, p2.gameNormal, p3.gameNormal };
                return;
            }

            // From 3.7.2.1. Overview:
            //   When normals are not specified, client implementations MUST calculate
            //   flat normals and the provided tangents (if present) MUST be ignored.
            Cartesian3 u = p2.gamePos.subtract(p1.gamePos);
            Cartesian3 v = p3.gamePos.subtract(p1.gamePos);
            Cartesian3 normal = u.cross(v);
            this.gameNormals = new Cartesian3[] { normal, normal, normal };
        }

        private PosTexNorm getGraphicsVertex(int index) {
            return new PosTexNorm(
                    gamePositions[index].getX(), gamePositions[index].getY(), gamePositions[index].getZ(),
                    tex[index][0], tex[index][1],
                    gameNormals[index].getX(), gameNormals[index].getY(), gameNormals[index].getZ()
            );
        }

        private GraphicsTriangle<PosTexNorm> toGraphics() {
            return GraphicsTriangle.newPosTexNorm(
                    this.getGraphicsVertex(0),
                    this.getGraphicsVertex(1),
                    this.getGraphicsVertex(2)
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
