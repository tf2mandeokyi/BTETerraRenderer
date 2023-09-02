package com.mndk.bteterrarenderer.core.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.MeshPrimitiveModelModes;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import de.javagl.jgltf.model.*;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Ogc3dObjectBaker {

    public List<GraphicsModel> bakeGltfModel(GltfModel model, Matrix4 transform) {
        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    public GraphicsModel bakeGltfMeshPrimitiveModel(MeshPrimitiveModel meshPrimitiveModel, Matrix4 transform) {
        Index[] parsedIndices = parseIndices(meshPrimitiveModel, transform);
        return bakeIndices(meshPrimitiveModel, parsedIndices);
    }

    private Index[] parseIndices(MeshPrimitiveModel meshPrimitiveModel, Matrix4 transform) {
        AccessorModel indices = meshPrimitiveModel.getIndices();
        AccessorModel positions = meshPrimitiveModel.getAttributes().get("POSITION");
//        AccessorModel texCoords = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");

        int size = indices != null ? indices.getCount() : positions.getCount();
        Index[] parsedIndices = new Index[size];

        Class<?> indicesDataType = indices != null ? indices.getComponentDataType() : null;
        for(int i = 0; i < size; i++) {
            int index = indices != null ? (indicesDataType == Short.class || indicesDataType == short.class ?
                    ((AccessorShortData) indices.getAccessorData()).get(i) :
                    ((AccessorIntData) indices.getAccessorData()).get(i)
            ) : i;
            parsedIndices[index] = new Index(readPosition(positions, index, transform));
        }

        return parsedIndices;
    }

    private GraphicsModel bakeIndices(MeshPrimitiveModel meshPrimitiveModel, Index[] indices) {
        // TODO: Add normal
        List<GraphicsQuad<GraphicsQuad.PosTexColor>> quadList = new ArrayList<>(indices.length / 3);
        int meshMode = meshPrimitiveModel.getMode();
        if(meshMode == MeshPrimitiveModelModes.TRIANGLES) {
            for(int i = 0; i < indices.length; i += 3) {
                Index i1 = indices[i], i2 = indices[i+1], i3 = indices[i+2];

                quadList.add(new GraphicsQuad<>(
                        new GraphicsQuad.PosTexColor(i1.pos[0], i1.pos[1], i1.pos[2], 0, 0, 1f, 1f, 1f, 1f),
                        new GraphicsQuad.PosTexColor(i2.pos[0], i2.pos[1], i2.pos[2], 0, 0, 1f, 1f, 1f, 1f),
                        new GraphicsQuad.PosTexColor(i3.pos[0], i3.pos[1], i3.pos[2], 0, 0, 1f, 1f, 1f, 1f),
                        new GraphicsQuad.PosTexColor(i1.pos[0], i1.pos[1], i1.pos[2], 0, 0, 1f, 1f, 1f, 1f)
                ));
            }
        } else {
            throw new RuntimeException("meshMode not supported: " + meshMode);
        }

        // TODO: Replace this with texture

        // TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    private float[] readPosition(AccessorModel positions, int index, Matrix4 transform) {
        Class<?> positionsDataType = positions.getComponentDataType();

        float[] position = new float[3];
        if(positionsDataType == Float.class || positionsDataType == float.class) {
            AccessorFloatData positionData = (AccessorFloatData) positions.getAccessorData();
            position[0] = positionData.get(index, 0);
            position[1] = positionData.get(index, 1);
            position[2] = positionData.get(index, 2);
        } else if(positionsDataType == Integer.class || positionsDataType == int.class) {
            AccessorIntData positionData = (AccessorIntData) positions.getAccessorData();
            position[0] = positionData.get(index, 0);
            position[1] = positionData.get(index, 1);
            position[2] = positionData.get(index, 2);
        } else if(positionsDataType == Short.class || positionsDataType == short.class) {
            AccessorShortData positionData = (AccessorShortData) positions.getAccessorData();
            position[0] = positionData.get(index, 0);
            position[1] = positionData.get(index, 1);
            position[2] = positionData.get(index, 2);
        }

        // TODO: Add extension for WEB3D_quantized_attributes
        // TODO: Add transformation

        return position;
    }

    private float[] readTexture(AccessorModel textureCoords, int index) {
        // TODO: Add Texture
        throw new UnsupportedOperationException("Not implemented");
    }

    @Data
    class Index {
        private final float[] pos;
//        private final float[] tex;
    }

}
