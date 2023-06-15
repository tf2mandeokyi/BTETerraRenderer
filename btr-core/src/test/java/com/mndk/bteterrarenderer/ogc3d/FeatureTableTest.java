package com.mndk.bteterrarenderer.ogc3d;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.ogc3d.format.table.BinaryVector;
import com.mndk.bteterrarenderer.ogc3d.format.table.BinaryJsonTableElement;
import lombok.Builder;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FeatureTableTest {

    @Test
    public void givenFeatureJsonAndBinary_testReadability() throws IOException {
        String json = "{" +
                "\"testValueByte\": 1," +
                "\"testValueShort\": 2," +
                "\"testValueInt\": 3," +
                "\"testValueFloat\": 4," +
                "\"testValueDouble\": 5," +
                "\"testValueByte3\": [6, 7, 8]," +
                "\"testBinaryInt\": { \"byteOffset\": 0 }," +
                "\"testBinaryInt3\": { \"byteOffset\": 4 }," +
                "\"testUnknownObject\": { \"byteOffset\": 16, \"type\": \"VEC4\", \"componentType\": \"BYTE\" }" +
        "}";
        byte[] binary = new byte[] { 69, 0, 0, 0, 9, 0, 0, 0, 10, 0, 0, 0, 11, 0, 0, 0, 12, 13, 14, 15 };
        FeatureTableHeaderTest tableHeaderTest =
                BTETerraRendererConstants.JSON_MAPPER.readValue(json, FeatureTableHeaderTest.class);

        Assert.assertEquals(tableHeaderTest.testValueByte.getValue(binary), Byte.valueOf((byte) 1));
        Assert.assertEquals(tableHeaderTest.testValueShort.getValue(binary), Short.valueOf((short) 2));
        Assert.assertEquals(tableHeaderTest.testValueInt.getValue(binary), Integer.valueOf(3));
        Assert.assertEquals(tableHeaderTest.testValueFloat.getValue(binary), Float.valueOf(4));
        Assert.assertEquals(tableHeaderTest.testValueDouble.getValue(binary), Double.valueOf(5));
        Assert.assertArrayEquals(tableHeaderTest.testValueByte3.getValue(binary).getElements(), new Byte[] { 6, 7, 8 });
        Assert.assertEquals(tableHeaderTest.testBinaryInt.getValue(binary), Integer.valueOf(69));
        Assert.assertArrayEquals(tableHeaderTest.testBinaryInt3.getValue(binary).getElements(), new Integer[] { 9, 10, 11 });
        Assert.assertEquals(tableHeaderTest.testUnknownObject.getValue(binary),
                new BinaryVector.Vec4<>((byte) 12, (byte) 13, (byte) 14, (byte) 15));
    }

    @Data
    @Builder
    @JsonDeserialize(builder = FeatureTableHeaderTest.FeatureTableHeaderTestBuilder.class)
    private static class FeatureTableHeaderTest {
        BinaryJsonTableElement<Byte> testValueByte;
        BinaryJsonTableElement<Short> testValueShort;
        BinaryJsonTableElement<Integer> testValueInt;
        BinaryJsonTableElement<Float> testValueFloat;
        BinaryJsonTableElement<Double> testValueDouble;
        BinaryJsonTableElement<BinaryVector.Vec3<Byte>> testValueByte3;
        BinaryJsonTableElement<Integer> testBinaryInt;
        BinaryJsonTableElement<BinaryVector.Vec3<Integer>> testBinaryInt3;
        BinaryJsonTableElement<Object> testUnknownObject;

        @JsonPOJOBuilder(withPrefix = "")
        public static class FeatureTableHeaderTestBuilder {}
    }

}
