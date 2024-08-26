package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

public class SequentialIntegerAttributeEncodingTest {

    @Test
    public void doesCompress() {
        int[] values = new int[] { 1, 8, 7, 5, 5, 5, 9, 155, -6, -9, 9, 125, 1, 0 };
        Pointer<Integer> valuesPointer = Pointer.wrap(values);

        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.GENERIC, (byte) 1, DracoDataType.INT32, false, values.length);
        for(int i = 0; i < values.length; ++i) {
            pa.setAttributeValue(AttributeValueIndex.of(i), valuesPointer.add(i));
        }
        CppVector<PointIndex> pointIds = new CppVector<>(PointIndex.type(), values.length);
        for(int i = 0; i < values.length; ++i) {
            pointIds.set(i, PointIndex.of(i));
        }

        EncoderBuffer outBuf = new EncoderBuffer();
        SequentialIntegerAttributeEncoder ie = new SequentialIntegerAttributeEncoder();
        StatusAssert.assertOk(ie.initializeStandalone(pa));
        StatusAssert.assertOk(ie.transformAttributeToPortableFormat(pointIds));
        StatusAssert.assertOk(ie.encodePortableAttribute(pointIds, outBuf));
        StatusAssert.assertOk(ie.encodeDataNeededByPortableTransform(outBuf));

        DecoderBuffer inBuf = new DecoderBuffer();
        inBuf.init(outBuf.getData(), outBuf.size());
        inBuf.setBitstreamVersion(DracoVersions.MESH_BIT_STREAM_VERSION);
        SequentialIntegerAttributeDecoder id = new SequentialIntegerAttributeDecoder();
        StatusAssert.assertOk(id.initializeStandalone(pa));
        StatusAssert.assertOk(id.decodePortableAttribute(pointIds, inBuf));
        StatusAssert.assertOk(id.decodeDataNeededByPortableTransform(pointIds, inBuf));
        StatusAssert.assertOk(id.transformAttributeToOriginalFormat(pointIds));

        for(int i = 0; i < values.length; ++i) {
            Pointer<Integer> entryVal = Pointer.newInt();
            pa.getValue(AttributeValueIndex.of(i), entryVal);
            Assert.assertEquals(values[i], entryVal.get().intValue());
        }
    }

}
