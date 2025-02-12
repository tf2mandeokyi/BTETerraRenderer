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

        for (int i = 0; i < 10; i++) {
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.newInt(i));
        }

        pa.setUniqueId(UInt.of(12));

        PointAttribute otherPa = new PointAttribute();
        StatusAssert.assertOk(otherPa.copyFrom(pa));

        Assert.assertEquals(pa.hashCode(), otherPa.hashCode());
        Assert.assertEquals(pa.getUniqueId(), otherPa.getUniqueId());

        // The hash function does not actually compute the hash from atribute values,
        // so ensure the data got copied correctly as well.
        for (int i = 0; i < 10; i++) {
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
        for (int i = 0; i < 5; i++) {
            points[0] = i * 3;
            points[1] = (i * 3) + 1;
            points[2] = (i * 3) + 2;
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.wrap(points));
        }

        for (int i = 0; i < 5; i++) {
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
        for (int i = 0; i < 5; i++) {
            points[0] = i * 3;
            points[1] = (i * 3) + 1;
            points[2] = (i * 3) + 2;
            pa.setAttributeValue(AttributeValueIndex.of(i), Pointer.wrap(points));
        }

        for (int i = 0; i < 5; i++) {
            Pointer<Float> attValue = pa.getValue(AttributeValueIndex.of(i), DataType.float32(), 3);
            Assert.assertEquals(attValue.get(0), i * 3, 0);
            Assert.assertEquals(attValue.get(1), (i * 3) + 1, 0);
            Assert.assertEquals(attValue.get(2), (i * 3) + 2, 0);
        }
        for (int i = 0; i < 5; i++) {
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
        for (int i = 0; i < 5; i++) {
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
        Assert.assertEquals(5, pa.size());
        Assert.assertEquals(pa.getBuffer().size(), 5 * 3 * DataType.float32().byteSize());

        pa.resize(10);
        Assert.assertEquals(10, pa.size());
        Assert.assertEquals(pa.getBuffer().size(), 10 * 3 * DataType.float32().byteSize());
    }
}
