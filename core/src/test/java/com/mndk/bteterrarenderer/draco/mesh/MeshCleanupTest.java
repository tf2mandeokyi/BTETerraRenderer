package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

public class MeshCleanupTest {

    @Test
    public void testDegeneratedFaces() {
        // This test verifies that the mesh cleanup tools removes degenerated faces.
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
    }

    @Test
    public void testDegeneratedFacesAndIsolatedVertices() {
        // This test verifies that the mesh cleanup tools removes degenerated faces
        // and isolated vertices.
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int intAttId = mb.addAttribute(GeometryAttribute.Type.GENERIC, (byte) 2, DracoDataType.INT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(intAttId, FaceIndex.of(0),
                Pointer.wrap(new int[] { 0, 0 }),
                Pointer.wrap(new int[] { 0, 1 }),
                Pointer.wrap(new int[] { 0, 2 }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 10f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 10f, 1f, 0f }));
        mb.setAttributeValuesForFace(intAttId, FaceIndex.of(1),
                Pointer.wrap(new int[] { 0, 0 }),
                Pointer.wrap(new int[] { 0, 1 }),
                Pointer.wrap(new int[] { 0, 2 }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        Assert.assertEquals("Wrong number of point ids in the input mesh.", 5, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(intAttId).size());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
        Assert.assertEquals("Failed to remove isolated attribute indices.", 3, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(intAttId).size());
    }

    @Test
    public void testAttributes() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(2);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int genericAttId = mb.addAttribute(GeometryAttribute.Type.GENERIC, (byte) 2, DracoDataType.FLOAT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(genericAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 10f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 10f, 1f, 0f }));
        mb.setAttributeValuesForFace(genericAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the test mesh.", mesh);
        Assert.assertEquals("Wrong number of faces in the input mesh.", 2, mesh.getNumFaces());
        Assert.assertEquals("Wrong number of point ids in the input mesh.", 5, mesh.getNumPoints());
        Assert.assertEquals(2, mesh.getAttribute(genericAttId).size());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals("Failed to remove degenerated faces.", 1, mesh.getNumFaces());
        Assert.assertEquals("Failed to remove isolated attribute indices.", 3, mesh.getNumPoints());
        Assert.assertEquals(3, mesh.getAttribute(0).size());
        Assert.assertEquals(1, mesh.getAttribute(1).size());
    }

    @Test
    public void testDuplicateFaces() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(5);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int normAttId = mb.addAttribute(GeometryAttribute.Type.NORMAL, (byte) 3, DracoDataType.FLOAT32);

        // Five faces where only two are unique in spatial domain and three are unique
        // when we take into account the normal attribute.

        // clang-format off
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setAttributeValuesForFace(normAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull(mesh);
        Assert.assertEquals(5, mesh.getNumFaces());
        MeshCleanupOptions cleanupOptions = new MeshCleanupOptions();
        StatusAssert.assertOk(MeshCleanup.cleanup(mesh, cleanupOptions));
        Assert.assertEquals(3, mesh.getNumFaces());
    }

}
