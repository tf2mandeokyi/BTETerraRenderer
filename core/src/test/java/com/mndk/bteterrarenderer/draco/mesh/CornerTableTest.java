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

        for(VertexIndex index : VertexIndex.range(0, table.getNumVertices())) {
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

        for(CornerIndex index : CornerIndex.range(0, table.getNumCorners())) {
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
        Assert.assertEquals(connectedComponents.getNumConnectedComponents(), 2);
    }

    @Test
    public void testNewFace() {
        File file = DracoTestFileUtil.toFile("draco/testdata/cube_att.obj");
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull(mesh);

        CornerTable ct = MeshUtil.createCornerTableFromPositionAttribute(mesh);
        Assert.assertNotNull(ct);
        Assert.assertEquals(ct.getNumFaces(), 12);
        Assert.assertEquals(ct.getNumCorners(), 3 * 12);
        Assert.assertEquals(ct.getNumVertices(), 8);

        VertexIndex newVi = ct.addNewVertex();
        Assert.assertEquals(ct.getNumVertices(), 9);

        Assert.assertEquals(ct.addNewFace(new VertexIndex[] { VertexIndex.of(6), VertexIndex.of(7), newVi }).getValue(), 12);
        Assert.assertEquals(ct.getNumFaces(), 13);
        Assert.assertEquals(ct.getNumCorners(), 3 * 13);

        Assert.assertEquals(ct.getVertex(CornerIndex.of(3 * 12    )).getValue(), 6);
        Assert.assertEquals(ct.getVertex(CornerIndex.of(3 * 12 + 1)).getValue(), 7);
        Assert.assertEquals(ct.getVertex(CornerIndex.of(3 * 12 + 2)), newVi);
    }
}
