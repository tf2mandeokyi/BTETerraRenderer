package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.draco.compression.DracoTestFileUtil;
import org.junit.Assert;
import org.junit.Test;

public class MeshAreEquivalentTest {

    @Test
    public void testOnIdenticalMesh() {
        String fileName = "draco/test_nm.obj";
        Mesh mesh = DracoTestFileUtil.decode(fileName);
        Assert.assertNotNull("Failed to load test model: " + fileName, mesh);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        Assert.assertTrue(equiv.equals(mesh, mesh));
    }

    @Test
    public void testPermutedOneFace() {
        String fileName0 = "draco/one_face_123.obj";
        String fileName1 = "draco/one_face_312.obj";
        String fileName2 = "draco/one_face_321.obj";
        Mesh mesh0 = DracoTestFileUtil.decode(fileName0);
        Mesh mesh1 = DracoTestFileUtil.decode(fileName1);
        Mesh mesh2 = DracoTestFileUtil.decode(fileName2);
        Assert.assertNotNull("Failed to load test model: " + fileName0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + fileName1, mesh1);
        Assert.assertNotNull("Failed to load test model: " + fileName2, mesh2);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        Assert.assertTrue(equiv.equals(mesh0, mesh0));
        Assert.assertTrue(equiv.equals(mesh0, mesh1)); // Face rotated
        Assert.assertFalse(equiv.equals(mesh0, mesh2)); // Face inverted
    }

    @Test
    public void testPermutedTwoFaces() {
        String fileName0 = "draco/two_faces_123.obj";
        String fileName1 = "draco/two_faces_312.obj";
        Mesh mesh0 = DracoTestFileUtil.decode(fileName0);
        Mesh mesh1 = DracoTestFileUtil.decode(fileName1);
        Assert.assertNotNull("Failed to load test model: " + fileName0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + fileName1, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        Assert.assertTrue(equiv.equals(mesh0, mesh0));
        Assert.assertTrue(equiv.equals(mesh1, mesh1));
        Assert.assertTrue(equiv.equals(mesh0, mesh1));
    }

    @Test
    public void testPermutedThreeFaces() {
        String fileName0 = "draco/three_faces_123.obj";
        String fileName1 = "draco/three_faces_312.obj";
        Mesh mesh0 = DracoTestFileUtil.decode(fileName0);
        Mesh mesh1 = DracoTestFileUtil.decode(fileName1);
        Assert.assertNotNull("Failed to load test model: " + fileName0, mesh0);
        Assert.assertNotNull("Failed to load test model: " + fileName1, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        Assert.assertTrue(equiv.equals(mesh0, mesh0));
        Assert.assertTrue(equiv.equals(mesh1, mesh1));
        Assert.assertTrue(equiv.equals(mesh0, mesh1));
    }

    @Test
    public void testOnBigMesh() {
        String fileName = "draco/test_nm.obj";
        Mesh mesh0 = DracoTestFileUtil.decode(fileName);
        Assert.assertNotNull("Failed to load test model: " + fileName, mesh0);

        Mesh mesh1 = DracoTestFileUtil.decode(fileName);
        Assert.assertNotNull("Failed to load test model: " + fileName, mesh1);

        MeshAreEquivalent equiv = new MeshAreEquivalent();
        Assert.assertTrue(equiv.equals(mesh0, mesh0));
        Assert.assertTrue(equiv.equals(mesh1, mesh1));
        Assert.assertTrue(equiv.equals(mesh0, mesh1));
    }

}
