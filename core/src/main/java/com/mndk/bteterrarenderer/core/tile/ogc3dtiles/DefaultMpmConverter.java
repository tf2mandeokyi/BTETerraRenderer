package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.util.QuantizationUtil;
import com.mndk.bteterrarenderer.util.Loggers;
import de.javagl.jgltf.model.*;
import lombok.Builder;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector2f;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

class DefaultMpmConverter extends AbstractMpmConverter {

    private final MeshPrimitiveModel meshPrimitiveModel;

    @Builder
    private DefaultMpmConverter(Matrix4d transform, GeographicProjection projection,
                                SpheroidCoordinatesConverter coordConverter, MeshPrimitiveModel meshPrimitiveModel) {
        super(transform, projection, coordConverter);
        this.meshPrimitiveModel = meshPrimitiveModel;
    }

    public PreBakedModel convert() throws Exception {
        ParsedPoint[] points = parsePoints();
        GraphicsShapes shapes = parsedPointsToShapes(meshPrimitiveModel, points);
        BufferedImage texture = readMaterialModel(meshPrimitiveModel.getMaterialModel());
        return new PreBakedModel(texture, shapes);
    }

    private ParsedPoint[] parsePoints() throws OutOfProjectionBoundsException {
        AccessorModel positionAccessor = meshPrimitiveModel.getAttributes().get("POSITION");
        AccessorModel normalAccessor = meshPrimitiveModel.getAttributes().get("NORMAL");
        AccessorModel textureCoordAccessor = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
        if (textureCoordAccessor == null) {
            Loggers.get(this).warn("texture coord accessor is null");
        }

        Matrix4d positionTransform = new Matrix4d();
        Matrix3d normalTransform = new Matrix3d();
        Web3dQuantizedAttributes positionExtension = GltfExtensionsUtil.getExtension(positionAccessor, Web3dQuantizedAttributes.class);
        if (positionExtension != null) {
            positionTransform = positionExtension.getDecodeMatrix();
            normalTransform = new Matrix3d(positionTransform).invert().transpose();
        }

        int numPoints = positionAccessor.getCount();
        ParsedPoint[] parsedPoints = new ParsedPoint[numPoints];
        for (int i = 0; i < numPoints; i++) {
            Vector3d position = readVector3d(positionAccessor, i);
            Vector3d normal = normalAccessor == null ? null : readVector3d(normalAccessor, i);
            Vector2f tex = readTextureCoord(textureCoordAccessor, i);
            parsedPoints[i] = this.toParsedPoint(position, positionTransform, normal, normalTransform, tex);
        }
        return parsedPoints;
    }

    /**
     * Reads the texture coordinate of the point
     * @param textureCoordAccessor The texture coordinate accessor
     * @param index The index
     * @return A new float array of size 2
     */
    private static Vector2f readTextureCoord(@Nullable AccessorModel textureCoordAccessor, int index) {
        // Return random point if the texture coordinate accessor is null
        if (textureCoordAccessor == null) return new Vector2f();
        AccessorData data = textureCoordAccessor.getAccessorData();
        boolean normalized = textureCoordAccessor.isNormalized();
        return new Vector2f(readFloatArray(data, normalized, index, 2));
    }

    private static GraphicsShapes parsedPointsToShapes(MeshPrimitiveModel meshPrimitiveModel, ParsedPoint[] points) {
        GraphicsShapes shapes = new GraphicsShapes();
        AccessorModel indicesAccessor = meshPrimitiveModel.getIndices();
        int meshCount = indicesAccessor != null ? indicesAccessor.getCount() : points.length;

        int meshMode = meshPrimitiveModel.getMode();
        if (meshMode == MeshPrimitiveModelModes.TRIANGLES) {
            for (int i = 0; i < meshCount; i += 3) {
                ParsedPoint point0 = points[readMeshIndex(indicesAccessor, i)];
                ParsedPoint point1 = points[readMeshIndex(indicesAccessor, i + 1)];
                ParsedPoint point2 = points[readMeshIndex(indicesAccessor, i + 2)];

                ParsedTriangle triangle = new ParsedTriangle(point0, point1, point2);
                shapes.add(DrawingFormat.TRI_PTN, triangle.toGraphics());
            }
        } else {
            Loggers.get().warn("meshMode not supported: {}", meshMode);
        }

        return shapes;
    }

    private static int readMeshIndex(@Nullable AccessorModel indicesAccessor, int index) {
        return indicesAccessor == null ? index : readInteger(indicesAccessor, index);
    }

    /**
     * Reads a 3d vector from the accessor
     * @param accessor The accessor
     * @param index The index
     * @return A new instance of {@link Vector3d}
     */
    private static Vector3d readVector3d(AccessorModel accessor, int index) {
        AccessorData data = accessor.getAccessorData();
        boolean normalized = accessor.isNormalized();
        return new Vector3d(readFloatArray(data, normalized, index, 3));
    }

    private static int readInteger(@Nonnull AccessorModel accessor, int defaultValue) {
        AccessorData data = accessor.getAccessorData();
        if (data == null) return defaultValue;

        if (data instanceof AccessorByteData) {
            return ((AccessorByteData) data).get(defaultValue);
        }
        else if (data instanceof AccessorShortData) {
            return ((AccessorShortData) data).get(defaultValue);
        }
        else if (data instanceof AccessorIntData) {
            return ((AccessorIntData) data).get(defaultValue);
        }
        else {
            throw new RuntimeException("unsupported data type: " + data.getComponentType());
        }
    }

    private static float[] readFloatArray(AccessorData data, boolean normalized, int elementIndex, int componentCount) {
        float[] result = new float[componentCount];
        if (data instanceof AccessorFloatData) {
            for (int i = 0; i < componentCount; i++) {
                result[i] = ((AccessorFloatData) data).get(elementIndex, i);
            }
            return result;
        }
        else if (data instanceof AccessorIntData) {
            for (int i = 0; i < componentCount; i++) {
                result[i] = ((AccessorIntData) data).get(elementIndex, i);
            }
            return result;
        }
        else if (data instanceof AccessorShortData) {
            AccessorShortData shortData = (AccessorShortData) data;
            for (int i = 0; i < componentCount; i++) {
                int value = shortData.getInt(elementIndex, i);
                result[i] = normalized ? QuantizationUtil.normalizeShort(value, shortData.isUnsigned()) : value;
            }
            return result;
        }
        else if (data instanceof AccessorByteData) {
            AccessorByteData byteData = (AccessorByteData) data;
            for (int i = 0; i < componentCount; i++) {
                int value = byteData.getInt(elementIndex, i);
                result[i] = normalized ? QuantizationUtil.normalizeByte(value, byteData.isUnsigned()) : value;
            }
            return result;
        }
        else {
            throw new RuntimeException("unsupported value type: " + data.getComponentType());
        }
    }

}
