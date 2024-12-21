package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryJsonTableElement;
import com.mndk.bteterrarenderer.ogc3dtiles.table.BinaryVector;
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
                BTETerraRenderer.JSON_MAPPER.readValue(json, FeatureTableHeaderTest.class);

        Assert.assertEquals(Byte.valueOf((byte) 1), tableHeaderTest.testValueByte.getValue(binary));
        Assert.assertEquals(Short.valueOf((short) 2), tableHeaderTest.testValueShort.getValue(binary));
        Assert.assertEquals(Integer.valueOf(3), tableHeaderTest.testValueInt.getValue(binary));
        Assert.assertEquals(Float.valueOf(4), tableHeaderTest.testValueFloat.getValue(binary));
        Assert.assertEquals(Double.valueOf(5), tableHeaderTest.testValueDouble.getValue(binary));
        Assert.assertArrayEquals(new Byte[] { 6, 7, 8 }, tableHeaderTest.testValueByte3.getValue(binary).getElements());
        Assert.assertEquals(Integer.valueOf(69), tableHeaderTest.testBinaryInt.getValue(binary));
        Assert.assertArrayEquals(new Integer[] { 9, 10, 11 }, tableHeaderTest.testBinaryInt3.getValue(binary).getElements());
        Assert.assertEquals(new BinaryVector.Vec4<>((byte) 12, (byte) 13, (byte) 14, (byte) 15),
                tableHeaderTest.testUnknownObject.getValue(binary));
    }

    @Data
    private static class FeatureTableHeaderTest {
        private final BinaryJsonTableElement<Byte> testValueByte;
        private final BinaryJsonTableElement<Short> testValueShort;
        private final BinaryJsonTableElement<Integer> testValueInt;
        private final BinaryJsonTableElement<Float> testValueFloat;
        private final BinaryJsonTableElement<Double> testValueDouble;
        private final BinaryJsonTableElement<BinaryVector.Vec3<Byte>> testValueByte3;
        private final BinaryJsonTableElement<Integer> testBinaryInt;
        private final BinaryJsonTableElement<BinaryVector.Vec3<Integer>> testBinaryInt3;
        private final BinaryJsonTableElement<Object> testUnknownObject;

        private FeatureTableHeaderTest(
                @JsonProperty(value = "testValueByte")
                BinaryJsonTableElement<Byte> testValueByte,
                @JsonProperty(value = "testValueShort")
                BinaryJsonTableElement<Short> testValueShort,
                @JsonProperty(value = "testValueInt")
                BinaryJsonTableElement<Integer> testValueInt,
                @JsonProperty(value = "testValueFloat")
                BinaryJsonTableElement<Float> testValueFloat,
                @JsonProperty(value = "testValueDouble")
                BinaryJsonTableElement<Double> testValueDouble,
                @JsonProperty(value = "testValueByte3")
                BinaryJsonTableElement<BinaryVector.Vec3<Byte>> testValueByte3,
                @JsonProperty(value = "testBinaryInt")
                BinaryJsonTableElement<Integer> testBinaryInt,
                @JsonProperty(value = "testBinaryInt3")
                BinaryJsonTableElement<BinaryVector.Vec3<Integer>> testBinaryInt3,
                @JsonProperty(value = "testUnknownObject")
                BinaryJsonTableElement<Object> testUnknownObject
        ) {
            this.testValueByte = testValueByte;
            this.testValueShort = testValueShort;
            this.testValueInt = testValueInt;
            this.testValueFloat = testValueFloat;
            this.testValueDouble = testValueDouble;
            this.testValueByte3 = testValueByte3;
            this.testBinaryInt = testBinaryInt;
            this.testBinaryInt3 = testBinaryInt3;
            this.testUnknownObject = testUnknownObject;
        }
    }

}
