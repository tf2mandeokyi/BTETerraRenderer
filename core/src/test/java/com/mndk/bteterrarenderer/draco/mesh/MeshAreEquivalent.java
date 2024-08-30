/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MeshAreEquivalent {

    private static class MeshInfo {
        private final Mesh mesh;
        private final CppVector<FaceIndex> orderedIndexOfFace =
                new CppVector<>(FaceIndex.type());
        private final IndexTypeVector<FaceIndex, Integer> cornerIndexOfSmallestVertex =
                new IndexTypeVector<>(DataType.int32());

        public MeshInfo(Mesh mesh) {
            this.mesh = mesh;
        }
    }

    private static class FaceIndexLess implements Comparator<FaceIndex> {
        private final MeshInfo meshInfo;

        public FaceIndexLess(MeshInfo meshInfo) {
            this.meshInfo = meshInfo;
        }

        @Override
        public int compare(FaceIndex o1, FaceIndex o2) {
            if(o1.equals(o2)) return 0;
            int c0 = meshInfo.cornerIndexOfSmallestVertex.get(o1);
            int c1 = meshInfo.cornerIndexOfSmallestVertex.get(o2);

            for(int i = 0; i < 3; ++i) {
                VectorD.D3<Float> vf0 = getPosition(meshInfo.mesh, o1, (c0 + i) % 3);
                VectorD.D3<Float> vf1 = getPosition(meshInfo.mesh, o2, (c1 + i) % 3);
                if(vf0.compareTo(vf1) < 0) return -1;
                if(vf0.compareTo(vf1) > 0) return 1;
            }
            // In case the two faces are equivalent.
            return 0;
        }
    }

    private final List<MeshInfo> meshInfos = new ArrayList<>();
    private int numFaces;

    public <T> Status equals(Mesh mesh0, Mesh mesh1) {
        StatusChain chain = new StatusChain();

        if(mesh0.getNumFaces() != mesh1.getNumFaces()) {
            return Status.invalidParameter("Number of faces mismatch");
        }
        if(mesh0.getNumAttributes() != mesh1.getNumAttributes()) {
            return Status.invalidParameter("Number of attributes mismatch");
        }

        if(this.init(mesh0, mesh1).isError(chain)) return chain.get();

        int attMax = GeometryAttribute.Type.NAMED_ATTRIBUTES_COUNT;
        for(int attId = 0; attId < attMax; ++attId) {
            GeometryAttribute.Type namedType = GeometryAttribute.Type.valueOf(attId);
            PointAttribute att0 = mesh0.getNamedAttribute(namedType);
            PointAttribute att1 = mesh1.getNamedAttribute(namedType);
            if (att0 == null && att1 == null) continue;
            if (att0 == null) return Status.invalidParameter("Attribute 0 for type " + namedType + " is null");
            if (att1 == null) return Status.invalidParameter("Attribute 1 for type " + namedType + " is null");

            if (att0.getDataType() != att1.getDataType()) return Status.invalidParameter("Attribute data type mismatch" +
                    " (" + namedType + "): left=" + att0.getDataType() + ", right=" + att1.getDataType());
            if (!att0.getNumComponents().equals(att1.getNumComponents())) return Status.invalidParameter("Attribute component count mismatch" +
                    " (" + namedType + "): left=" + att0.getNumComponents() + ", right=" + att1.getNumComponents());
            if (att0.isNormalized() != att1.isNormalized()) return Status.invalidParameter("Attribute normalization mismatch" +
                    " (" + namedType + "): left=" + att0.isNormalized() + ", right=" + att1.isNormalized());
            if (att0.getByteStride() != att1.getByteStride()) return Status.invalidParameter("Attribute byte stride mismatch" +
                    " (" + namedType + "): left=" + att0.getByteStride() + ", right=" + att1.getByteStride());

            if (!att0.isValid()) return Status.invalidParameter("Attribute 0 for type " + namedType + " is not valid");
            if (!att1.isValid()) return Status.invalidParameter("Attribute 1 for type " + namedType + " is not valid");

            DataType<T> dataType = att0.getDataType().getActualType();
            int numComponents = att0.getNumComponents().intValue();

            Pointer<T> data0 = dataType.newArray(numComponents);
            Pointer<T> data1 = dataType.newArray(numComponents);

            // Check every corner of every face.
            for(int i = 0; i < numFaces; i++) {
                FaceIndex f0 = meshInfos.get(0).orderedIndexOfFace.get(i);
                FaceIndex f1 = meshInfos.get(1).orderedIndexOfFace.get(i);
                int c0Off = meshInfos.get(0).cornerIndexOfSmallestVertex.get(f0);
                int c1Off = meshInfos.get(1).cornerIndexOfSmallestVertex.get(f1);

                for(int c = 0; c < 3; c++) {
                    PointIndex corner0 = mesh0.getFace(f0).get((c0Off + c) % 3);
                    PointIndex corner1 = mesh1.getFace(f1).get((c1Off + c) % 3);
                    AttributeValueIndex index0 = att0.getMappedIndex(corner0);
                    AttributeValueIndex index1 = att1.getMappedIndex(corner1);

                    // Obtaining the data.
                    att0.getValue(index0, data0);
                    att1.getValue(index1, data1);
                    if(!PointerHelper.contentEquals(data0, data1, numComponents)) {
                        return Status.invalidParameter("Attribute data mismatch");
                    }
                }
            }
        }
        return Status.ok();
    }

    private Status init(Mesh mesh0, Mesh mesh1) {
        meshInfos.clear();

        numFaces = mesh1.getNumFaces();
        meshInfos.add(new MeshInfo(mesh0));
        meshInfos.add(new MeshInfo(mesh1));

        if(meshInfos.size() != 2) {
            return Status.invalidParameter("Mesh infos size mismatch");
        }
        if(!meshInfos.get(0).cornerIndexOfSmallestVertex.isEmpty()) {
            return Status.invalidParameter("Corner index of smallest vertex is not empty");
        }
        if(!meshInfos.get(1).cornerIndexOfSmallestVertex.isEmpty()) {
            return Status.invalidParameter("Corner index of smallest vertex is not empty");
        }
        if(!meshInfos.get(0).orderedIndexOfFace.isEmpty()) {
            return Status.invalidParameter("Ordered index of face is not empty");
        }
        if(!meshInfos.get(1).orderedIndexOfFace.isEmpty()) {
            return Status.invalidParameter("Ordered index of face is not empty");
        }

        this.initCornerIndexOfSmallestPointXYZ();
        this.initOrderedFaceIndex();
        return Status.ok();
    }

    private static VectorD.D3<Float> getPosition(Mesh mesh, FaceIndex f, int c) {
        PointAttribute posAtt = mesh.getNamedAttribute(GeometryAttribute.Type.POSITION);
        PointIndex verIndex = mesh.getFace(f).get(c);
        AttributeValueIndex posIndex = posAtt.getMappedIndex(verIndex);
        float[] pos = new float[3];
        posAtt.getValue(posIndex, Pointer.wrap(pos));
        return VectorD.float3(pos[0], pos[1], pos[2]);
    }

    private static int computeCornerIndexOfSmallestPointXYZ(Mesh mesh, FaceIndex f) {
        VectorD.D3<Float>[] pos = BTRUtil.uncheckedCast(new VectorD.D3[3]);
        for(int i = 0; i < 3; ++i) {
            pos[i] = getPosition(mesh, f, i);
        }
        int minIndex = 0;
        for(int i = 1; i < 3; ++i) {
            if(pos[i].compareTo(pos[minIndex]) < 0) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void initCornerIndexOfSmallestPointXYZ() {
        if(!meshInfos.get(0).cornerIndexOfSmallestVertex.isEmpty()) {
            throw new IllegalStateException("Corner index of smallest vertex is not empty");
        }
        if(!meshInfos.get(1).cornerIndexOfSmallestVertex.isEmpty()) {
            throw new IllegalStateException("Corner index of smallest vertex is not empty");
        }
        for(int i = 0; i < 2; ++i) {
            meshInfos.get(i).cornerIndexOfSmallestVertex.reserve(numFaces);
            for(FaceIndex f : FaceIndex.range(0, numFaces)) {
                meshInfos.get(i).cornerIndexOfSmallestVertex.pushBack(
                        computeCornerIndexOfSmallestPointXYZ(meshInfos.get(i).mesh, f));
            }
        }
        if(meshInfos.get(0).cornerIndexOfSmallestVertex.size() != numFaces) {
            throw new IllegalStateException("Corner index of smallest vertex size mismatch");
        }
        if(meshInfos.get(1).cornerIndexOfSmallestVertex.size() != numFaces) {
            throw new IllegalStateException("Corner index of smallest vertex size mismatch");
        }
    }

    private void initOrderedFaceIndex() {
        if(!meshInfos.get(0).orderedIndexOfFace.isEmpty()) {
            throw new IllegalStateException("Ordered index of face is not empty");
        }
        if(!meshInfos.get(1).orderedIndexOfFace.isEmpty()) {
            throw new IllegalStateException("Ordered index of face is not empty");
        }
        for(int i = 0; i < 2; ++i) {
            meshInfos.get(i).orderedIndexOfFace.reserve(numFaces);
            for(FaceIndex j : FaceIndex.range(0, numFaces)) {
                meshInfos.get(i).orderedIndexOfFace.pushBack(j);
            }
            FaceIndexLess less = new FaceIndexLess(meshInfos.get(i));
            meshInfos.get(i).orderedIndexOfFace.sort(less);

            if(meshInfos.get(i).orderedIndexOfFace.size() != numFaces) {
                throw new IllegalStateException("Ordered index of face size mismatch");
            }
            if(!meshInfos.get(i).orderedIndexOfFace.isSorted(less)) {
                throw new IllegalStateException("Ordered index of face is not sorted");
            }
        }
    }

}
