package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.metadata.AttributeMetadata;
import com.mndk.bteterrarenderer.draco.metadata.Metadata;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ObjectDecoderTest {

    @Test
    public void extraVertexObj() {
        DracoTestFileUtil.testDecoding("draco/extra_vertex.obj");
    }

    @Test
    public void partialAttributesObj() {
        DracoTestFileUtil.testDecoding("draco/cube_att_partial.obj");
    }

    @Test
    public void subObjects() {
        Mesh mesh = DracoTestFileUtil.decode("draco/cube_att_sub_o.obj");
        Assert.assertTrue(mesh.getNumFaces() > 0);
        Assert.assertEquals(4, mesh.getNumAttributes());
        Assert.assertEquals(GeometryAttribute.Type.GENERIC, mesh.getAttribute(3).getAttributeType());
        Assert.assertEquals(3, mesh.getAttribute(3).size());
        Assert.assertEquals(UInt.of(3), mesh.getAttribute(3).getUniqueId());
    }

    @Test
    public void subObjectsWithMetadata() {
        Mesh mesh = DracoTestFileUtil.decodeWithMetadata("draco/cube_att_sub_o.obj");
        Assert.assertTrue(mesh.getNumFaces() > 0);

        Assert.assertEquals(4, mesh.getNumAttributes());
        Assert.assertEquals(GeometryAttribute.Type.GENERIC, mesh.getAttribute(3).getAttributeType());
        Assert.assertEquals(3, mesh.getAttribute(3).size());

        Assert.assertNotNull(mesh.getMetadata());
        AttributeMetadata attributeMetadata = mesh.getAttributeMetadataByAttributeId(3);
        Assert.assertNotNull(attributeMetadata);
        AtomicReference<Integer> subObjectId = new AtomicReference<>();
        StatusAssert.assertOk(attributeMetadata.getEntryInt("obj2", subObjectId::set));
        Assert.assertEquals(2, subObjectId.get().intValue());
    }

    @Test
    public void quadTriangulateObj() {
        Mesh mesh = DracoTestFileUtil.decode("draco/cube_quads.obj");
        Assert.assertEquals(12, mesh.getNumFaces());
        Assert.assertEquals(3, mesh.getNumAttributes());
        Assert.assertEquals(4 * 6, mesh.getNumPoints());
    }

    @Test
    public void quadPreserveObj() {
        Mesh mesh = DracoTestFileUtil.decodeWithPolygons("draco/cube_quads.obj");
        Assert.assertEquals(12, mesh.getNumFaces());

        Assert.assertEquals(4, mesh.getNumAttributes());
        Assert.assertEquals(4 * 6, mesh.getNumPoints());

        Assert.assertEquals(GeometryAttribute.Type.GENERIC, mesh.getAttribute(3).getAttributeType());

        PointAttribute attribute = mesh.getAttribute(3);
        Assert.assertEquals(2, attribute.size());
        Assert.assertEquals(0, attribute.getValue(AttributeValueIndex.of(0), DataType.uint8(), 1).get().intValue());
        Assert.assertEquals(1, attribute.getValue(AttributeValueIndex.of(1), DataType.uint8(), 1).get().intValue());
        for(int i = 0; i < 6; i++) {
            Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of((4 * i    ))).getValue());
            Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of((4 * i + 1))).getValue());
            Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of((4 * i + 2))).getValue());
            Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of((4 * i + 3))).getValue());
        }

        Metadata metadata = mesh.getAttributeMetadataByAttributeId(3);
        Assert.assertNotNull(metadata);
        Assert.assertEquals(1, metadata.getEntries().size());
        StringBuilder name = new StringBuilder();
        StatusAssert.assertOk(metadata.getEntryString("name", name));
        Assert.assertEquals("added_edges", name.toString());
    }

    @Test
    public void octagonTriangulatedObj() {
        Mesh mesh = DracoTestFileUtil.decode("draco/octagon.obj");
        Assert.assertEquals(1, mesh.getNumAttributes());
        Assert.assertEquals(8, mesh.getNumPoints());
        Assert.assertEquals(GeometryAttribute.Type.POSITION, mesh.getAttribute(0).getAttributeType());
        Assert.assertEquals(8, mesh.getAttribute(0).size());
    }

    @Test
    public void octagonPreservedObj() {
        Mesh mesh = DracoTestFileUtil.decodeWithPolygons("draco/octagon.obj");
        Assert.assertEquals(2, mesh.getNumAttributes());

        PointAttribute attribute = mesh.getAttribute(0);
        Assert.assertEquals(GeometryAttribute.Type.POSITION, attribute.getAttributeType());
        Assert.assertEquals(8, attribute.size());

        // Expect a new generic attribute.
        attribute = mesh.getAttribute(1);
        Assert.assertEquals(GeometryAttribute.Type.GENERIC, attribute.getAttributeType());

        // There are four vertices with both old and new edges in their ring.
        Assert.assertEquals(8 + 4, mesh.getNumPoints());

        Assert.assertEquals(2, attribute.size());
        Assert.assertEquals(0, attribute.getValue(AttributeValueIndex.of(0), DataType.uint8(), 1).get().intValue());
        Assert.assertEquals(1, attribute.getValue(AttributeValueIndex.of(1), DataType.uint8(), 1).get().intValue());

        // 0: Old edge, 1: New edge
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(0)).getValue());
        Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of(1)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(2)).getValue());
        Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of(3)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(4)).getValue());
        Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of(5)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(6)).getValue());
        Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of(7)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(8)).getValue());
        Assert.assertEquals(1, attribute.getMappedIndex(PointIndex.of(9)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(10)).getValue());
        Assert.assertEquals(0, attribute.getMappedIndex(PointIndex.of(11)).getValue());

        Metadata metadata = mesh.getAttributeMetadataByAttributeId(1);
        Assert.assertNotNull(metadata);
        Assert.assertEquals(1, metadata.getEntries().size());
        StringBuilder name = new StringBuilder();
        StatusAssert.assertOk(metadata.getEntryString("name", name));
        Assert.assertEquals("added_edges", name.toString());
    }

    @Test
    public void emptyNameObj() {
        Mesh mesh = DracoTestFileUtil.decode("draco/empty_name.obj");
        Assert.assertEquals(1, mesh.getNumAttributes());
        Assert.assertEquals(3, mesh.getAttribute(0).size());
    }

    @Test
    public void pointCloudObj() {
        Mesh mesh = DracoTestFileUtil.decode("draco/test_lines.obj", false);
        Assert.assertEquals(0, mesh.getNumFaces());
        Assert.assertEquals(1, mesh.getNumAttributes());
        Assert.assertEquals(484, mesh.getAttribute(0).size());
    }

    @Test
    public void wrongAttributeMapping() {
        Mesh mesh = DracoTestFileUtil.decode("draco/test_wrong_attribute_mapping.obj");
        Assert.assertEquals(1, mesh.getNumAttributes());
        Assert.assertEquals(3, mesh.getAttribute(0).size());
    }

    @Test
    public void testObjDecodingAll() {
        DracoTestFileUtil.testDecoding("draco/bunny_norm.obj");
        DracoTestFileUtil.testDecoding("draco/cube_att.obj");
        DracoTestFileUtil.testDecoding("draco/cube_att_partial.obj");
        DracoTestFileUtil.testDecoding("draco/cube_att_sub_o.obj");
        DracoTestFileUtil.testDecoding("draco/cube_quads.obj");
        DracoTestFileUtil.testDecoding("draco/cube_subd.obj");
        DracoTestFileUtil.testDecoding("draco/eof_test.obj");
        DracoTestFileUtil.testDecoding("draco/extra_vertex.obj");
        DracoTestFileUtil.testDecoding("draco/mat_test.obj");
        DracoTestFileUtil.testDecoding("draco/one_face_123.obj");
        DracoTestFileUtil.testDecoding("draco/one_face_312.obj");
        DracoTestFileUtil.testDecoding("draco/one_face_321.obj");
        DracoTestFileUtil.testDecoding("draco/sphere.obj");
        DracoTestFileUtil.testDecoding("draco/test_nm.obj");
        DracoTestFileUtil.testDecoding("draco/test_nm_trans.obj");
        DracoTestFileUtil.testDecoding("draco/test_sphere.obj");
        DracoTestFileUtil.testDecoding("draco/three_faces_123.obj");
        DracoTestFileUtil.testDecoding("draco/three_faces_312.obj");
        DracoTestFileUtil.testDecoding("draco/two_faces_123.obj");
        DracoTestFileUtil.testDecoding("draco/two_faces_312.obj");
        DracoTestFileUtil.testDecoding("draco/inf_nan.obj");
    }
}
