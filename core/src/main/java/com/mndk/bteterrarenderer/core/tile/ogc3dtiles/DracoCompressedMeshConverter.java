package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.DracoDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.DrawingFormat;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsShapes;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions.DracoMeshCompression;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.BufferViewModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DracoCompressedMeshConverter extends AbstractMeshPrimitiveModelConverter {

    private final MeshPrimitiveModel meshPrimitiveModel;
    private final BufferViewModel bufferViewModel;
    private final Map<String, Integer> attributeMapping;

    public DracoCompressedMeshConverter(MeshPrimitiveModel meshPrimitiveModel,
                                        List<BufferViewModel> topLevelBufferViewModels, DracoMeshCompression extension,
                                        Cartesian3 translation, Matrix4 transform, GeographicProjection projection) {
        super(translation, transform, projection);
        this.meshPrimitiveModel = meshPrimitiveModel;
        this.bufferViewModel = topLevelBufferViewModels.get(extension.getBufferView());
        this.attributeMapping = extension.getAttributes();
    }

    @Override
    protected PreBakedModel convert() throws Exception {
        Mesh mesh = this.decodeMesh();
        ParsedPoint[] points = this.parsePoints(mesh);
        GraphicsShapes shapes = parsedPointsToShapes(mesh, points);
        BufferedImage texture = readMaterialModel(meshPrimitiveModel.getMaterialModel());
        return new PreBakedModel(texture, shapes);
    }

    private ParsedPoint[] parsePoints(Mesh mesh) throws OutOfProjectionBoundsException {
        Map<String, PointAttribute> attributes = attributeMapping.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), mesh.getAttribute(entry.getValue())), HashMap::putAll);
        PointAttribute positionAttribute = attributes.get("POSITION");
        PointAttribute normalAttribute = attributes.get("NORMAL");
        PointAttribute texAttribute = attributes.get("TEXCOORD_0");

        float[] posArray = new float[3];
        float[] normArray = new float[3];
        float[] texArray = new float[2];

        int numPoints = mesh.getNumPoints();
        ParsedPoint[] parsedPoints = new ParsedPoint[numPoints];
        for(PointIndex pointIndex : PointIndex.range(0, numPoints)) {
            positionAttribute.getMappedValue(pointIndex, Pointer.wrap(posArray));
            Cartesian3 position = new Cartesian3(posArray);
            Cartesian3 gamePos = this.transformEarthCoordToGame(position);

            if(normalAttribute != null) normalAttribute.getMappedValue(pointIndex, Pointer.wrap(normArray));
            Cartesian3 normal = normalAttribute == null ? null : new Cartesian3(normArray);
            Cartesian3 gameNormal = normal == null ? null : this.transformEarthCoordToGame(position.add(normal)).subtract(gamePos);

            if(texAttribute != null) texAttribute.getMappedValue(pointIndex, Pointer.wrap(texArray));
            float[] tex = texAttribute == null ? null : texArray;

            parsedPoints[pointIndex.getValue()] = new ParsedPoint(gamePos, tex, gameNormal);
        }
        return parsedPoints;
    }

    private static GraphicsShapes parsedPointsToShapes(Mesh mesh, ParsedPoint[] points) {
        GraphicsShapes shapes = new GraphicsShapes();
        for(FaceIndex faceIndex : FaceIndex.range(0, mesh.getNumFaces())) {
            ParsedPoint point0 = points[mesh.getFace(faceIndex).get(0).getValue()];
            ParsedPoint point1 = points[mesh.getFace(faceIndex).get(1).getValue()];
            ParsedPoint point2 = points[mesh.getFace(faceIndex).get(2).getValue()];

            ParsedTriangle triangle = new ParsedTriangle(point0, point1, point2);
            shapes.add(DrawingFormat.TRI_PTN_ALPHA, triangle.toGraphics());
        }
        return shapes;
    }

    private Mesh decodeMesh() {
        ByteBuffer byteBuffer = bufferViewModel.getBufferViewData();
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(byteBuffer);

        EncodedGeometryType geometryType = DracoDecoder.getEncodedGeometryType(decoderBuffer).getValue();
        if(geometryType != EncodedGeometryType.TRIANGULAR_MESH) {
            throw new IllegalStateException("Unsupported geometry type: " + geometryType);
        }

        DracoDecoder decoder = new DracoDecoder();
        return decoder.decodeMeshFromBuffer(decoderBuffer).getValue();
    }
}
