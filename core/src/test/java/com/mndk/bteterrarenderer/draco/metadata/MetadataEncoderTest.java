package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

public class MetadataEncoderTest {

    private static class Package {
        private final MetadataEncoder encoder = new MetadataEncoder();
        private final MetadataDecoder decoder = new MetadataDecoder();
        private final EncoderBuffer encoderBuffer = new EncoderBuffer();
        private final DecoderBuffer decoderBuffer = new DecoderBuffer();
        private final Metadata metadata = new Metadata();
        private final GeometryMetadata geometryMetadata = new GeometryMetadata();

        private void testEncodingMetadata() {
            StatusAssert.assertOk(encoder.encodeMetadata(encoderBuffer, metadata));

            Metadata decodedMetadata = new Metadata();
            decoderBuffer.init(encoderBuffer.getData(), encoderBuffer.size());
            StatusAssert.assertOk(decoder.decodeMetadata(decoderBuffer, decodedMetadata));
            this.checkMetadatasAreEqual(metadata, decodedMetadata);
        }

        private void testEncodingGeometryMetadata() {
            StatusAssert.assertOk(encoder.encodeGeometryMetadata(encoderBuffer, geometryMetadata));

            GeometryMetadata decodedMetadata = new GeometryMetadata();
            decoderBuffer.init(encoderBuffer.getData(), encoderBuffer.size());
            StatusAssert.assertOk(decoder.decodeGeometryMetadata(decoderBuffer, decodedMetadata));
            this.checkGeometryMetadatasAreEqual(geometryMetadata, decodedMetadata);
        }

        private void checkBlobOfDataAreEqual(BigUByteArray data0, BigUByteArray data1) {
            Assert.assertEquals(data0.size(), data1.size());
            Assert.assertTrue(data0.equals(data1));
        }

        private void checkGeometryMetadatasAreEqual(GeometryMetadata metadata0, GeometryMetadata metadata1) {
            Assert.assertEquals(metadata0.getAttributeMetadatas().size(), metadata1.getAttributeMetadatas().size());
            for(int i = 0; i < metadata0.getAttributeMetadatas().size(); ++i) {
                this.checkMetadatasAreEqual(metadata0.getAttributeMetadatas().get(i), metadata1.getAttributeMetadatas().get(i));
            }
            this.checkMetadatasAreEqual(metadata0, metadata1);
        }

        private void checkMetadatasAreEqual(Metadata metadata0, Metadata metadata1) {
            Assert.assertEquals(metadata0.getNumEntries(), metadata1.getNumEntries());
            for(String entryName : metadata0.getEntries().keySet()) {
                BigUByteArray data0 = metadata0.getEntries().get(entryName).getBuffer();
                BigUByteArray data1 = metadata1.getEntries().get(entryName).getBuffer();
                this.checkBlobOfDataAreEqual(data0, data1);
            }
            Assert.assertEquals(metadata0.getSubMetadatas().size(), metadata1.getSubMetadatas().size());
            for(String subMetadataName : metadata0.getSubMetadatas().keySet()) {
                this.checkMetadatasAreEqual(metadata0.getSubMetadatas().get(subMetadataName), metadata1.getSubMetadatas().get(subMetadataName));
            }
        }
    }

    @Test
    public void testSingleEntry() {
        Package p = new Package();
        p.metadata.addEntryInt("int", 100);
        Assert.assertEquals(p.metadata.getNumEntries(), 1);

        p.testEncodingMetadata();
    }

    @Test
    public void testMultipleEntries() {
        Package p = new Package();
        p.metadata.addEntryInt("int", 100);
        p.metadata.addEntryDouble("double", 1.234);
        String entryValue = "test string entry";
        p.metadata.addEntryString("string", entryValue);
        Assert.assertEquals(p.metadata.getNumEntries(), 3);

        p.testEncodingMetadata();
    }

    @Test
    public void testEncodingArrayEntries() {
        Package p = new Package();
        p.metadata.addEntryIntArray("int_array", new int[] { 1, 2, 3 });
        p.metadata.addEntryDoubleArray("double_array", new double[] { 0.1, 0.2, 0.3 });
        Assert.assertEquals(p.metadata.getNumEntries(), 2);

        p.testEncodingMetadata();
    }

    @Test
    public void testEncodingBinaryEntry() {
        Package p = new Package();
        byte[] binaryData = new byte[] { 0x1, 0x2, 0x3, 0x4 };
        p.metadata.addEntryBinary("binary_data", BigUByteArray.create(binaryData));

        p.testEncodingMetadata();
    }

    @Test
    public void testEncodingNestedMetadata() {
        Package p = new Package();
        p.metadata.addEntryDouble("double", 1.234);
        Metadata subMetadata = new Metadata();
        subMetadata.addEntryInt("int", 100);
        p.metadata.addSubMetadata("sub0", subMetadata);

        p.testEncodingMetadata();
    }

    @Test
    public void testEncodingGeometryMetadata() {
        Package p = new Package();
        AttributeMetadata attMetadata = new AttributeMetadata();
        attMetadata.addEntryInt("int", 100);
        attMetadata.addEntryString("name", "pos");
        Assert.assertTrue(p.geometryMetadata.addAttributeMetadata(attMetadata));

        p.testEncodingGeometryMetadata();
    }
}
