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

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshUtil;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.io.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.io.MeshIOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class CornerTableTest {

    @Test
    public void normalWithSeams() {
        File file = DracoTestFileUtil.toFile("draco/testdata/cube_att.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model: " + file, mesh);

        CornerTable table = MeshUtil.createCornerTableFromPositionAttribute(mesh);
        table.getValenceCache().cacheValences();
        table.getValenceCache().cacheValencesInaccurate();

        for (VertexIndex index : VertexIndex.range(0, table.getNumVertices())) {
            int valence = table.getValence(index);
            int valence2 = table.getValenceCache().valenceFromCache(index);
            int valence3 = table.getValenceCache().valenceFromCacheInaccurate(index);
            Assert.assertEquals(valence, valence2);
            Assert.assertTrue(valence >= valence3);

            Assert.assertTrue(valence <= 6);
            Assert.assertTrue(valence2 <= 6);

            Assert.assertTrue(valence >= 3);
            Assert.assertTrue(valence2 >= 3);
            Assert.assertTrue(valence3 >= 3);
        }

        for (CornerIndex index : CornerIndex.range(0, table.getNumCorners())) {
            int valence = table.getValence(index);
            int valence2 = table.getValenceCache().valenceFromCache(index);
            int valence3 = table.getValenceCache().valenceFromCacheInaccurate(index);
            Assert.assertEquals(valence, valence2);
            Assert.assertTrue(valence >= valence3);

            Assert.assertTrue(valence <= 6);
            Assert.assertTrue(valence2 <= 6);

            Assert.assertTrue(valence >= 3);
            Assert.assertTrue(valence2 >= 3);
            Assert.assertTrue(valence3 >= 3);
        }

        table.getValenceCache().clearValenceCache();
        table.getValenceCache().clearValenceCacheInaccurate();
    }

    @Test
    public void testNonManifoldEdges() {
        File file = DracoTestFileUtil.toFile("draco/testdata/non_manifold_wrap.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull(mesh);

        CornerTable ct = MeshUtil.createCornerTableFromPositionAttribute(mesh);
        Assert.assertNotNull(ct);

        MeshConnectedComponents connectedComponents = new MeshConnectedComponents();
        connectedComponents.findConnectedComponents(ct);
        Assert.assertEquals(2, connectedComponents.getNumConnectedComponents());
    }

    @Test
    public void testNewFace() {
        File file = DracoTestFileUtil.toFile("draco/testdata/cube_att.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull(mesh);

        CornerTable ct = MeshUtil.createCornerTableFromPositionAttribute(mesh);
        Assert.assertNotNull(ct);
        Assert.assertEquals(12, ct.getNumFaces());
        Assert.assertEquals(3 * 12, ct.getNumCorners());
        Assert.assertEquals(8, ct.getNumVertices());

        VertexIndex newVi = ct.addNewVertex();
        Assert.assertEquals(9, ct.getNumVertices());

        Assert.assertEquals(12, ct.addNewFace(new VertexIndex[] { VertexIndex.of(6), VertexIndex.of(7), newVi }).getValue());
        Assert.assertEquals(13, ct.getNumFaces());
        Assert.assertEquals(3 * 13, ct.getNumCorners());

        Assert.assertEquals(6, ct.getVertex(CornerIndex.of(3 * 12    )).getValue());
        Assert.assertEquals(7, ct.getVertex(CornerIndex.of(3 * 12 + 1)).getValue());
        Assert.assertEquals(ct.getVertex(CornerIndex.of(3 * 12 + 2)), newVi);
    }
}
