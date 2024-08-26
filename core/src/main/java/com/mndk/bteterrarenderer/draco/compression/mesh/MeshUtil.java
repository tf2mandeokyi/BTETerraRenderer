package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeshUtil {

    public CornerTable createCornerTableFromPositionAttribute(Mesh mesh) {
        return createCornerTableFromAttribute(mesh, GeometryAttribute.Type.POSITION);
    }

    public CornerTable createCornerTableFromAttribute(Mesh mesh, GeometryAttribute.Type type) {
        PointAttribute att = mesh.getNamedAttribute(type);
        if(att == null) return null;
        IndexTypeVector<FaceIndex, CornerTable.FaceType> faces =
                new IndexTypeVector<>(CornerTable.FaceType::new, mesh.getNumFaces());
        for(FaceIndex i : FaceIndex.range(0, mesh.getNumFaces())) {
            CornerTable.FaceType newFace = new CornerTable.FaceType();
            Mesh.Face face = mesh.getFace(i);
            for(int j = 0; j < 3; ++j) {
                newFace.set(j, VertexIndex.of(att.getMappedIndex(face.get(j)).getValue()));
            }
            faces.set(i, newFace);
        }
        return CornerTable.create(faces).getValue();
    }

    public CornerTable createCornerTableFromAllAttributes(Mesh mesh) {
        IndexTypeVector<FaceIndex, CornerTable.FaceType> faces =
                new IndexTypeVector<>(CornerTable.FaceType::new, mesh.getNumFaces());
        for(FaceIndex i : FaceIndex.range(0, mesh.getNumFaces())) {
            CornerTable.FaceType newFace = new CornerTable.FaceType();
            Mesh.Face face = mesh.getFace(i);
            for(int j = 0; j < 3; ++j) {
                newFace.set(j, VertexIndex.of(face.get(j).getValue()));
            }
            faces.set(i, newFace);
        }
        return CornerTable.create(faces).getValue();
    }

}
