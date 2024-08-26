package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.io.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.io.MeshIOUtil;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class DracoDecoderTest {

    @Test
    public void testSkipAttributeTransformUniqueId() {
        File file = DracoTestFileUtil.toFile("draco/testdata/cube_att.obj");
        Mesh srcMesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull(srcMesh);

        UInt kPosUniqueId = UInt.of(7);
        UInt kNormUniqueId = UInt.of(42);
        // Set unique ids for some of the attributes.
        srcMesh.getAttribute(srcMesh.getNamedAttributeId(GeometryAttribute.Type.POSITION)).setUniqueId(kPosUniqueId);
        srcMesh.getAttribute(srcMesh.getNamedAttributeId(GeometryAttribute.Type.NORMAL)).setUniqueId(kNormUniqueId);

        EncoderBuffer encoderBuffer = new EncoderBuffer();
        DracoEncoder encoder = new DracoEncoder();
        encoder.setAttributeQuantization(GeometryAttribute.Type.POSITION, 10);
        encoder.setAttributeQuantization(GeometryAttribute.Type.NORMAL, 11);
        StatusAssert.assertOk(encoder.encodeMeshToBuffer(srcMesh, encoderBuffer));

        // Create a draco decoding buffer.
        DecoderBuffer buffer = new DecoderBuffer();
        buffer.init(encoderBuffer.getData(), encoderBuffer.size());

        // First we decode the mesh without skipping the attribute transforms.
        DracoDecoder decoderNoSkip = new DracoDecoder();
        Mesh meshNoSkip = decoderNoSkip.decodeMeshFromBuffer(buffer).getValue();
        Assert.assertNotNull(meshNoSkip);

        // Now we decode it again while skipping some attributes.
        DracoDecoder decoderSkip = new DracoDecoder();
        // Make sure we skip dequantization for the position and normal attribute.
        decoderSkip.setSkipAttributeTransform(GeometryAttribute.Type.POSITION);
        decoderSkip.setSkipAttributeTransform(GeometryAttribute.Type.NORMAL);

        // Decode the input data into a geometry.
        buffer.init(encoderBuffer.getData(), encoderBuffer.size());
        Mesh meshSkip = decoderSkip.decodeMeshFromBuffer(buffer).getValue();
        Assert.assertNotNull(meshSkip);

        // Compare the unique ids.
        PointAttribute posAttNoSkip = meshNoSkip.getNamedAttribute(GeometryAttribute.Type.POSITION);
        Assert.assertNotNull(posAttNoSkip);
        Assert.assertEquals(posAttNoSkip.getDataType(), DracoDataType.FLOAT32);

        PointAttribute posAttSkip = meshSkip.getNamedAttribute(GeometryAttribute.Type.POSITION);
        Assert.assertNotNull(posAttSkip);
        Assert.assertEquals(posAttSkip.getDataType(), DracoDataType.INT32);

        PointAttribute normAttNoSkip = meshNoSkip.getNamedAttribute(GeometryAttribute.Type.NORMAL);
        Assert.assertNotNull(normAttNoSkip);
        Assert.assertEquals(normAttNoSkip.getDataType(), DracoDataType.FLOAT32);

        PointAttribute normAttSkip = meshSkip.getNamedAttribute(GeometryAttribute.Type.NORMAL);
        Assert.assertNotNull(normAttSkip);
        Assert.assertEquals(normAttSkip.getDataType(), DracoDataType.INT32);

        Assert.assertEquals(posAttSkip.getUniqueId(), posAttNoSkip.getUniqueId());
        Assert.assertEquals(normAttSkip.getUniqueId(), normAttNoSkip.getUniqueId());
    }

}