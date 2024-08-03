package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

public class PointAttributeTest {

    @Test
    public void testCopy() {
        // This test verifies that PointAttribute can copy data from another point
        // attribute.
        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.POSITION, (byte) 1, DracoDataType.INT32, false, 10);

        for(int i = 0; i < 10; i++) {
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.newInt(i));
        }

        pa.setUniqueId(UInt.of(12));

        PointAttribute otherPa = new PointAttribute();
        StatusAssert.assertOk(otherPa.copyFrom(pa));

        Assert.assertEquals(pa.hashCode(), otherPa.hashCode());
        Assert.assertEquals(pa.getUniqueId(), otherPa.getUniqueId());

        // The hash function does not actually compute the hash from atribute values,
        // so ensure the data got copied correctly as well.
        for(int i = 0; i < 10; i++) {
            Pointer<Integer> out = Pointer.newInt(0);
            StatusAssert.assertOk(otherPa.getValue(AttributeValueIndex.of(i), out));
            Assert.assertEquals(out.get().intValue(), i);
        }
    }

    @Test
    public void testGetValueFloat() {
        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32, false, 5);
        float[] points = new float[3];
        for(int i = 0; i < 5; i++) {
            points[0] = i * 3;
            points[1] = (i * 3) + 1;
            points[2] = (i * 3) + 2;
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.wrap(points));
        }

        for(int i = 0; i < 5; i++) {
            StatusAssert.assertOk(pa.getValue(AttributeValueIndex.of(i), Pointer.wrap(points)));
            Assert.assertEquals(points[0], i * 3, 0);
            Assert.assertEquals(points[1], (i * 3) + 1, 0);
            Assert.assertEquals(points[2], (i * 3) + 2, 0);
        }
    }

    @Test
    public void testGetArray() {
        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32, false, 5);
        float[] points = new float[3];
        for(int i = 0; i < 5; i++) {
            points[0] = i * 3;
            points[1] = (i * 3) + 1;
            points[2] = (i * 3) + 2;
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.wrap(points));
        }

        for(int i = 0; i < 5; i++) {
            Pointer<Float> attValue = pa.getValue(AttributeValueIndex.of(i), DataType.float32(), 3);
            Assert.assertEquals(attValue.get(0), i * 3, 0);
            Assert.assertEquals(attValue.get(1), (i * 3) + 1, 0);
            Assert.assertEquals(attValue.get(2), (i * 3) + 2, 0);
        }
        for(int i = 0; i < 5; i++) {
            pa.getValue(AttributeValueIndex.of(i), Pointer.wrap(points));
            Assert.assertEquals(points[0], i * 3, 0);
            Assert.assertEquals(points[1], (i * 3) + 1, 0);
            Assert.assertEquals(points[2], (i * 3) + 2, 0);
        }
    }

    @Test
    public void testArrayReadError() {
        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32, false, 5);
        float[] points = new float[3];
        for(int i = 0; i < 5; i++) {
            points[0] = i * 3;
            points[1] = (i * 3) + 1;
            points[2] = (i * 3) + 2;
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.wrap(points));
        }

        StatusAssert.assertError(pa.getValue(AttributeValueIndex.of(5), Pointer.wrap(points)));
    }

    @Test
    public void testResize() {
        PointAttribute pa = new PointAttribute();
        pa.init(GeometryAttribute.Type.POSITION, (byte) 3, DracoDataType.FLOAT32, false, 5);
        Assert.assertEquals(pa.size(), 5);
        Assert.assertEquals(pa.getBuffer().size(), 5 * 3 * DataType.float32().byteSize());

        pa.resize(10);
        Assert.assertEquals(pa.size(), 10);
        Assert.assertEquals(pa.getBuffer().size(), 10 * 3 * DataType.float32().byteSize());
    }
}
