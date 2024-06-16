package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

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
            DataBuffer buffer = encoderBuffer.getBuffer();

            Metadata decodedMetadata = new Metadata();
            decoderBuffer.init(buffer);
            StatusAssert.assertOk(decoder.decodeMetadata(decoderBuffer, decodedMetadata));
            this.checkMetadatasAreEqual(metadata, decodedMetadata);
        }

        private void testEncodingGeometryMetadata() {
            StatusAssert.assertOk(encoder.encodeGeometryMetadata(encoderBuffer, geometryMetadata));
            DataBuffer buffer = encoderBuffer.getBuffer();

            GeometryMetadata decodedMetadata = new GeometryMetadata();
            decoderBuffer.init(buffer);
            StatusAssert.assertOk(decoder.decodeGeometryMetadata(decoderBuffer, decodedMetadata));
            this.checkGeometryMetadatasAreEqual(geometryMetadata, decodedMetadata);
        }

        private void checkBlobOfDataAreEqual(DataBuffer data0, DataBuffer data1) {
            Assert.assertEquals(data0.size(), data1.size());
            for(int i = 0; i < data0.size(); ++i) {
                Assert.assertEquals(data0.get(i), data1.get(i));
            }
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
                DataBuffer data0 = metadata0.getEntries().get(entryName).getBuffer();
                DataBuffer data1 = metadata1.getEntries().get(entryName).getBuffer();
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
        p.metadata.addEntryIntArray("int_array", Arrays.asList(1, 2, 3));
        p.metadata.addEntryDoubleArray("double_array", Arrays.asList(0.1, 0.2, 0.3));
        Assert.assertEquals(p.metadata.getNumEntries(), 2);

        p.testEncodingMetadata();
    }

    @Test
    public void testEncodingBinaryEntry() {
        Package p = new Package();
        byte[] binaryData = new byte[] { 0x1, 0x2, 0x3, 0x4 };
        p.metadata.addEntryBinary("binary_data", binaryData);

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
