package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import org.junit.Assert;
import org.junit.Test;

public class TriangleSoupMeshBuilderTest {

    @Test
    public void cubeTest() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(12);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        // Front face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }));

        // Back face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 1f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));

        // Top face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(5),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 1f }));

        // Bottom face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(6),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(7),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));

        // Right face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(8),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(9),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 1f }));

        // Left face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(10),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(11),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the cube mesh.", mesh);
        Assert.assertEquals("Unexpected number of vertices.", 8, mesh.getNumPoints());
        Assert.assertEquals("Unexpected number of faces.", 12, mesh.getNumFaces());
    }

    @Test
    public void testPerFaceAttribs() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(12);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        int genAttId = mb.addAttribute(GeometryAttribute.Type.GENERIC, (byte) 1, DracoDataType.BOOL);
        Pointer<Byte> boolTrue = Pointer.newByte((byte) 1);
        Pointer<Byte> boolFalse = Pointer.newByte((byte) 0);
        // Front face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(0), boolFalse);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(1),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(1), boolTrue);

        // Back face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(2),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(2), boolTrue);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(3),
                Pointer.wrap(new float[] { 1f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(3), boolTrue);

        // Top face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(4),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(4), boolFalse);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(5),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 1f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(5), boolFalse);

        // Bottom face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(6),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(6), boolTrue);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(7),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(7), boolTrue);

        // Right face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(8),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(8), boolFalse);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(9),
                Pointer.wrap(new float[] { 1f, 1f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 1f }),
                Pointer.wrap(new float[] { 1f, 1f, 1f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(9), boolTrue);

        // Left face.
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(10),
                Pointer.wrap(new float[] { 0f, 1f, 0f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(10), boolTrue);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(11),
                Pointer.wrap(new float[] { 0f, 1f, 1f }),
                Pointer.wrap(new float[] { 0f, 0f, 1f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setPerFaceAttributeValueForFace(genAttId, FaceIndex.of(11), boolFalse);

        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull("Failed to build the cube mesh.", mesh);
        Assert.assertEquals("Unexpected number of faces.", 12, mesh.getNumFaces());
        Assert.assertEquals("Unexpected attribute element type.", MeshAttributeElementType.FACE,
                mesh.getAttributeElementType(genAttId));
    }

    @Test
    public void propagatesAttributeUniqueIds() {
        TriangleSoupMeshBuilder mb = new TriangleSoupMeshBuilder();
        mb.start(1);
        int posAttId = mb.addAttribute(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32);
        mb.setAttributeValuesForFace(posAttId, FaceIndex.of(0),
                Pointer.wrap(new float[] { 0f, 0f, 0f }),
                Pointer.wrap(new float[] { 1f, 0f, 0f }),
                Pointer.wrap(new float[] { 0f, 1f, 0f }));
        mb.setAttributeUniqueId(posAttId, UInt.of(1234));
        Mesh mesh = mb.finalizeMesh();
        Assert.assertNotNull(mesh);
        Assert.assertEquals(mesh.getAttributeByUniqueId(UInt.of(1234)), mesh.getAttribute(posAttId));
    }
}
