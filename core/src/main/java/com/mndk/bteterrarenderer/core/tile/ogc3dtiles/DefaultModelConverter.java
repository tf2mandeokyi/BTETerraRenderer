package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.GltfExtensionsUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.Web3dQuantizedAttributes;
import com.mndk.bteterrarenderer.ogc3dtiles.util.QuantizationUtil;
import de.javagl.jgltf.model.*;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

class DefaultModelConverter extends AbstractMeshPrimitiveModelConverter {

    private final MeshPrimitiveModel meshPrimitiveModel;

    public DefaultModelConverter(MeshPrimitiveModel meshPrimitiveModel, Context context) {
        super(context);
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

        Web3dQuantizedAttributes positionExtension = GltfExtensionsUtil.getExtension(positionAccessor, Web3dQuantizedAttributes.class);
        Matrix4d positionTransform = positionExtension != null ? positionExtension.getDecodeMatrix() : null;

        Web3dQuantizedAttributes normalExtension = normalAccessor == null ? null
                : GltfExtensionsUtil.getExtension(normalAccessor, Web3dQuantizedAttributes.class);
        Matrix4d normalTransform = normalExtension != null ? normalExtension.getDecodeMatrix() : null;

        int numPoints = positionAccessor.getCount();
        ParsedPoint[] parsedPoints = new ParsedPoint[numPoints];
        for (int i = 0; i < numPoints; i++) {
            Vector3d position = readPosition(positionAccessor, i, positionTransform);
            McCoord gamePos = this.transformEarthCoordToGame(position);

            Vector3d normal = normalAccessor == null ? null
                    : readNormal(normalAccessor, i, normalTransform);
            McCoord gameNormal = normal == null ? null
                    : this.transformEarthCoordToGame(position.add(normal, new Vector3d())).subtract(gamePos);

            float[] tex = readTextureCoord(textureCoordAccessor, i);

            parsedPoints[i] = new ParsedPoint(gamePos, tex, gameNormal);
        }
        return parsedPoints;
    }

    /**
     * Reads the position of the point, and transforms it if necessary
     * @param positionAccessor The position accessor
     * @param index The index
     * @param transform The transformation matrix
     * @return A new instance of {@link Vector3d} corresponding to the position
     */
    private static Vector3d readPosition(AccessorModel positionAccessor, int index, @Nullable Matrix4d transform) {
        Vector3d cartesian = readVector3d(positionAccessor, index);
        if (transform != null) transform.transformPosition(cartesian);
        return cartesian;
    }

    /**
     * Reads the texture coordinate of the point
     * @param textureCoordAccessor The texture coordinate accessor
     * @param index The index
     * @return A new float array of size 2
     */
    private static float[] readTextureCoord(@Nullable AccessorModel textureCoordAccessor, int index) {
        // Return random point if the texture coordinate accessor is null
        if (textureCoordAccessor == null) return new float[] { 0, 0 };
        AccessorData data = textureCoordAccessor.getAccessorData();
        return readFloatArray(data, index, 2);
    }

    /**
     * Reads the normal of the point, and transforms it if necessary
     * @param normalAccessor The normal accessor
     * @param index The index
     * @param transform The transformation matrix
     * @return A new instance of {@link Vector3d} corresponding to the normal
     */
    private static Vector3d readNormal(AccessorModel normalAccessor, int index, @Nullable Matrix4d transform) {
        Vector3d cartesian = readVector3d(normalAccessor, index);
        if (transform != null) transform.transformPosition(cartesian);
        return cartesian;
    }

    private static GraphicsShapes parsedPointsToShapes(MeshPrimitiveModel meshPrimitiveModel, ParsedPoint[] points) {
        GraphicsShapes shapes = new GraphicsShapes();
        AccessorModel indicesAccessor = meshPrimitiveModel.getIndices();
        int meshCount = indicesAccessor != null ? indicesAccessor.getCount() : points.length;

        int meshMode = meshPrimitiveModel.getMode();
        if (meshMode == MeshPrimitiveModelModes.TRIANGLES) {
            for (int i = 0; i < meshCount; i += 3) {
                int[] meshIndices = indicesAccessor != null ? new int[] {
                        readInteger(indicesAccessor, i),
                        readInteger(indicesAccessor, i + 1),
                        readInteger(indicesAccessor, i + 2)
                } : new int[] { i, i+1, i+2 };

                ParsedPoint point0 = points[meshIndices[0]];
                ParsedPoint point1 = points[meshIndices[1]];
                ParsedPoint point2 = points[meshIndices[2]];

                ParsedTriangle triangle = new ParsedTriangle(point0, point1, point2);
                shapes.add(DrawingFormat.QUAD_PTN, triangle.toGraphics().toQuad());
            }
        } else {
            Loggers.get().warn("meshMode not supported: {}", meshMode);
        }

        return shapes;
    }

    /**
     * Reads a 3d vector from the accessor
     * @param accessor The accessor
     * @param index The index
     * @return A new instance of {@link Vector3d}
     */
    private static Vector3d readVector3d(AccessorModel accessor, int index) {
        AccessorData data = accessor.getAccessorData();
        return new Vector3d(readFloatArray(data, index, 3));
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

    private static float[] readFloatArray(AccessorData data, int elementIndex, int componentCount) {
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
                result[i] = QuantizationUtil.normalizeShort(value, shortData.isUnsigned());
            }
            return result;
        }
        else if (data instanceof AccessorByteData) {
            AccessorByteData byteData = (AccessorByteData) data;
            for (int i = 0; i < componentCount; i++) {
                int value = byteData.getInt(elementIndex, i);
                result[i] = QuantizationUtil.normalizeByte(value, byteData.isUnsigned());
            }
            return result;
        }
        else {
            throw new RuntimeException("unsupported value type: " + data.getComponentType());
        }
    }

}
