package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Objects;

public class Mesh extends PointCloud {

    public static class Face {
        private final PointIndex[] points = new PointIndex[3];
        public Face() {
            this(PointIndex.INVALID, PointIndex.INVALID, PointIndex.INVALID);
        }
        public Face(PointIndex p1, PointIndex p2, PointIndex p3) {
            points[0] = p1;
            points[1] = p2;
            points[2] = p3;
        }
        public PointIndex get(int i) { return points[i]; }
        public int getValue(int i) { return points[i].getValue(); }
        public void set(int i, PointIndex p) { points[i] = p; }
        public String toString() {
            return "Face{" + points[0] + ", " + points[1] + ", " + points[2] + "}";
        }
        public int hashCode() {
            return Objects.hash(points[0].hashCode() + points[1].hashCode() + points[2].hashCode());
        }
    }

    private static class AttributeData {
        MeshAttributeElementType elementType = MeshAttributeElementType.CORNER;
        @Override public String toString() {
            return "AttributeData{elementType=" + elementType + "}";
        }
    }

    private final CppVector<AttributeData> attributeData = new CppVector<>(DataType.object(AttributeData::new));
    @Getter(AccessLevel.PROTECTED)
    private final IndexTypeVector<FaceIndex, Face> faces = new IndexTypeVector<>(Face::new);

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

    @Override
    protected void applyPointIdDeduplication(IndexTypeVector<PointIndex, PointIndex> idMap, CppVector<PointIndex> uniquePointIds) {
        //PointCloud::ApplyPointIdDeduplication(id_map, unique_point_ids);
        //for (FaceIndex f(0); f < num_faces(); ++f) {
        //  for (int32_t c = 0; c < 3; ++c) {
        //    faces_[f][c] = id_map[faces_[f][c]];
        //  }
        //}
        super.applyPointIdDeduplication(idMap, uniquePointIds);
        for(FaceIndex f : FaceIndex.range(0, getNumFaces())) {
            for(int c = 0; c < 3; ++c) {
                PointIndex p = faces.get(f).get(c);
                faces.get(f).set(c, idMap.get(p));
            }
        }
    }
}
