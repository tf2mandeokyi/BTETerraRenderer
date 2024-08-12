package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class MeshCleanup {

    public static Status cleanup(Mesh mesh, MeshCleanupOptions options) {
        if (!options.removeDegeneratedFaces && !options.removeUnusedAttributes &&
                !options.removeDuplicateFaces && !options.makeGeometryManifold) {
            return Status.ok();  // Nothing to cleanup.
        }
        PointAttribute posAtt = mesh.getNamedAttribute(GeometryAttribute.Type.POSITION);
        if (posAtt == null) return Status.dracoError("Missing position attribute.");

        if (options.removeDegeneratedFaces) removeDegeneratedFaces(mesh);
        if (options.removeDuplicateFaces) removeDuplicateFaces(mesh);
        if (options.removeUnusedAttributes) removeUnusedAttributes(mesh);
        return Status.ok();
    }

    private static void removeDegeneratedFaces(Mesh mesh) {
        PointAttribute posAtt = mesh.getNamedAttribute(GeometryAttribute.Type.POSITION);
        int numDegeneratedFaces = 0;

        int[] posIndices = new int[3];
        for(FaceIndex f : FaceIndex.range(0, mesh.getNumFaces())) {
            Mesh.Face face = mesh.getFace(f);
            for(int p = 0; p < 3; ++p) {
                posIndices[p] = posAtt.getMappedIndex(face.get(p)).getValue();
            }
            if (posIndices[0] == posIndices[1] || posIndices[0] == posIndices[2] ||
                    posIndices[1] == posIndices[2]) {
                ++numDegeneratedFaces;
            } else if (numDegeneratedFaces > 0) {
                mesh.setFace(f.subtract(numDegeneratedFaces), face);
            }
        }
        if (numDegeneratedFaces > 0) {
            mesh.setNumFaces(mesh.getNumFaces() - numDegeneratedFaces);
        }
    }

    private static void removeDuplicateFaces(Mesh mesh) {
        Set<Mesh.Face> isFaceUsed = new HashSet<>();

        int numDuplicateFaces = 0;
        for(FaceIndex fi : FaceIndex.range(0, mesh.getNumFaces())) {
            Mesh.Face face = mesh.getFace(fi);
            while (face.get(0).getValue() > face.get(1).getValue() || face.get(0).getValue() > face.get(2).getValue()) {
                PointIndex temp = face.get(0);
                face.set(0, face.get(1));
                face.set(1, temp);
                temp = face.get(1);
                face.set(1, face.get(2));
                face.set(2, temp);
            }
            if (isFaceUsed.contains(face)) {
                numDuplicateFaces++;
            } else {
                isFaceUsed.add(face);
                if (numDuplicateFaces > 0) {
                    mesh.setFace(fi.subtract(numDuplicateFaces), face);
                }
            }
        }
        if (numDuplicateFaces > 0) {
            mesh.setNumFaces(mesh.getNumFaces() - numDuplicateFaces);
        }
    }

    private static void removeUnusedAttributes(Mesh mesh) {
        CppVector<Boolean> isPointUsed = new CppVector<>(DataType.bool());
        int numNewPoints = 0;
        isPointUsed.resize(mesh.getNumPoints(), false);
        for(FaceIndex f : FaceIndex.range(0, mesh.getNumFaces())) {
            Mesh.Face face = mesh.getFace(f);
            for(int p = 0; p < 3; ++p) {
                if (!isPointUsed.get(face.get(p).getValue())) {
                    isPointUsed.set(face.get(p).getValue(), true);
                    numNewPoints++;
                }
            }
        }

        boolean pointsChanged = false;
        int numOriginalPoints = mesh.getNumPoints();
        IndexTypeVector<PointIndex, PointIndex> pointMap =
                new IndexTypeVector<>(PointIndex.type(), numOriginalPoints);
        if(numNewPoints < mesh.getNumPoints()) {
            numNewPoints = 0;
            for(PointIndex i : PointIndex.range(0, numOriginalPoints)) {
                if(isPointUsed.get(i.getValue())) {
                    pointMap.set(i, PointIndex.of(numNewPoints++));
                } else {
                    pointMap.set(i, PointIndex.INVALID);
                }
            }
            // Go over faces and update their points.
            for(FaceIndex f : FaceIndex.range(0, mesh.getNumFaces())) {
                Mesh.Face face = mesh.getFace(f);
                for(int p = 0; p < 3; p++) {
                    face.set(p, pointMap.get(face.get(p)));
                }
                mesh.setFace(f, face);
            }
            // Set the new number of points.
            mesh.setNumPoints(numNewPoints);
            pointsChanged = true;
        }
        else {
            // No points were removed. Initialize identity map between the old and new points.
            for(PointIndex i : PointIndex.range(0, numOriginalPoints)) {
                pointMap.set(i, i);
            }
        }

        // Update index mapping for attributes.
        IndexTypeVector<AttributeValueIndex, Boolean> isAttIndexUsed =
                new IndexTypeVector<>(DataType.bool());
        IndexTypeVector<AttributeValueIndex, AttributeValueIndex> attIndexMap =
                new IndexTypeVector<>(AttributeValueIndex.type());
        for(int a = 0; a < mesh.getNumAttributes(); a++) {
            PointAttribute att = mesh.getAttribute(a);
            // First detect which attribute entries are used (included in a point).
            isAttIndexUsed.assign(att.size(), false);
            attIndexMap.clear();
            int numUsedEntries = 0;
            for(PointIndex i : PointIndex.range(0, numOriginalPoints)) {
                if(pointMap.get(i).isValid()) {
                    AttributeValueIndex entryId = att.getMappedIndex(i);
                    if(!isAttIndexUsed.get(entryId)) {
                        isAttIndexUsed.set(entryId, true);
                        numUsedEntries++;
                    }
                }
            }
            boolean attIndicesChanged = false;
            // If there are some unused attribute entries, remap the attribute values
            // in the attribute buffer.
            if(numUsedEntries < att.size()) {
                attIndexMap.resize(att.size());
                numUsedEntries = 0;
                for(AttributeValueIndex i : AttributeValueIndex.range(0, att.size())) {
                    if(isAttIndexUsed.get(i)) {
                        attIndexMap.set(i, AttributeValueIndex.of(numUsedEntries));
                        if(i.getValue() > numUsedEntries) {
                            Pointer<UByte> srcAdd = att.getAddress(i, DataType.uint8());
                            att.getBuffer().write(att.getBytePos(AttributeValueIndex.of(numUsedEntries)),
                                    srcAdd, att.getByteStride());
                        }
                        numUsedEntries++;
                    }
                }
                // Update the number of unique entries in the vertex buffer.
                att.resize(numUsedEntries);
                attIndicesChanged = true;
            }
            // If either the points or attribute indices have changed, we need to
            // update the attribute index mapping.
            if(pointsChanged || attIndicesChanged) {
                if(att.isMappingIdentity()) {
                    if(numUsedEntries != mesh.getNumPoints()) {
                        att.setExplicitMapping(numOriginalPoints);
                        for(PointIndex i : PointIndex.range(0, numOriginalPoints)) {
                            att.setPointMapEntry(i, AttributeValueIndex.of(i.getValue()));
                        }
                    }
                }
                if(!att.isMappingIdentity()) {
                    for(PointIndex i : PointIndex.range(0, numOriginalPoints)) {
                        PointIndex newPointId = pointMap.get(i);
                        if(newPointId.isInvalid()) {
                            continue;
                        }
                        AttributeValueIndex originalEntryIndex = att.getMappedIndex(i);
                        AttributeValueIndex newEntryIndex = attIndicesChanged ?
                                attIndexMap.get(originalEntryIndex) : originalEntryIndex;
                        att.setPointMapEntry(newPointId, newEntryIndex);
                    }
                    att.setExplicitMapping(mesh.getNumPoints());
                }
            }
        }
    }

    private static Status makeGeometryManifold(Mesh mesh) {
        return Status.dracoError("Unsupported function.");
    }

}
