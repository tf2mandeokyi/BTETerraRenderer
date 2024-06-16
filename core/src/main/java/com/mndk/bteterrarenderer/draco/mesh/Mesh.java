package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.core.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.AccessLevel;
import lombok.Getter;

public class Mesh extends PointCloud {

    public static class Face {
        public final PointIndex[] points = new PointIndex[3];
        public Face(PointIndex p1, PointIndex p2, PointIndex p3) {
            points[0] = p1;
            points[1] = p2;
            points[2] = p3;
        }
        public PointIndex get(int i) {
            return points[i];
        }
    }

    private static class AttributeData {
        MeshAttributeElementType elementType = MeshAttributeElementType.MESH_CORNER_ATTRIBUTE;
    }

    private final CppVector<AttributeData> attributeData = new CppVector<>();
    @Getter(AccessLevel.PROTECTED)
    private final IndexTypeVector<FaceIndex, Face> faces = new IndexTypeVector<>(FaceIndex::of);

    public Mesh() {
        super();
    }

    public void addFace(Face face) {
        faces.pushBack(face);
    }

    public void setFace(FaceIndex index, Face face) {
        faces.set(index, face);
    }

    public void setNumFaces(int numFaces) {
        faces.resize(numFaces);
    }

    public int getNumFaces() {
        return faces.size();
    }

    public Face getFace(FaceIndex index) {
        return faces.get(index);
    }

    public void setAttribute(int attId, PointAttribute data) {
        super.setAttribute(attId, data);
        if(attId >= attributeData.size()) {
            attributeData.resize(attId + 1);
        }
    }

    public void deleteAttribute(int attId) {
        super.deleteAttribute(attId);
        attributeData.erase(attId);
    }

    public MeshAttributeElementType getAttributeElementType(int attId) {
        return attributeData.get(attId).elementType;
    }

    public void setAttributeElementType(int attId, MeshAttributeElementType et) {
        attributeData.get(attId).elementType = et;
    }

    public PointIndex cornerToPointId(int ci) {
        if (ci < 0 || ci == CornerIndex.INVALID.getValue()) {
            return PointIndex.INVALID;
        }
        return this.getFace(FaceIndex.of(ci / 3)).get(ci % 3);
    }

    public PointIndex cornerToPointId(CornerIndex ci) {
        return cornerToPointId(ci.getValue());
    }

}
