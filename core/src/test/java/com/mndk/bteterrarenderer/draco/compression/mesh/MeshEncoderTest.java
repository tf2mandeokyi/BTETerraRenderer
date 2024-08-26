package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.DracoExpertEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.io.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.io.MeshIOUtil;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class MeshEncoderTest {

    private static class TestParams {
        private final String encodingMethod;
        private final int cl;

        public TestParams(String encodingMethod, int cl) {
            this.encodingMethod = encodingMethod;
            this.cl = cl;
        }

        public String toString() {
            return "TestParams{" +
                    "method='" + encodingMethod + '\'' +
                    ", cl=" + cl +
                    '}';
        }
    }

    private MeshEncoderMethod getMethod(TestParams params) {
        if (params.encodingMethod.equals("sequential")) {
            return MeshEncoderMethod.SEQUENTIAL;
        }
        if (params.encodingMethod.equals("edgebreaker")) {
            return MeshEncoderMethod.EDGEBREAKER;
        }
        throw new IllegalArgumentException("Test is run for an unknown encoding method: " + params.encodingMethod);
    }

    private void testGolden(TestParams params, String fileName) {
        MeshEncoderMethod method = this.getMethod(params);

        String goldenFileName = fileName;
        goldenFileName += "." + params.encodingMethod;
        goldenFileName += ".cl" + params.cl;
        goldenFileName += "." + DracoVersions.MESH_BIT_STREAM_VERSION_MAJOR;
        goldenFileName += "." + DracoVersions.MESH_BIT_STREAM_VERSION_MINOR;
        goldenFileName += ".drc";

        File file = DracoTestFileUtil.toFile(fileName);
        Mesh mesh = MeshIOUtil.decode(file).getValueOr(Status::throwException);
        Assert.assertNotNull("Failed to load test model " + fileName, mesh);

        DracoExpertEncoder encoder = new DracoExpertEncoder(mesh);
        encoder.setEncodingMethod(method);
        encoder.setSpeedOptions(10 - params.cl, 10 - params.cl);
        encoder.setAttributeQuantization(0, 20);
        for (int i = 1; i < mesh.getNumAttributes(); ++i) {
            encoder.setAttributeQuantization(i, 12);
        }
        EncoderBuffer buffer = new EncoderBuffer();
        StatusAssert.assertOk(encoder.encodeToBuffer(buffer));
        // Check that the encoded mesh was really encoded with the selected method.
        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(buffer.getData(), buffer.size());
        decoderBuffer.advance(8);  // Skip the header to the encoding method id.
        Pointer<UByte> encodedMethodRef = Pointer.newUByte();
        StatusAssert.assertOk(decoderBuffer.decode(encodedMethodRef));
        Assert.assertEquals(method.getValue(), encodedMethodRef.get().intValue());

        File goldenFile = DracoTestFileUtil.toFile(goldenFileName);
        DracoTestFileUtil.compareGoldenFile(goldenFile, buffer);
    }

    @Test
    public void givenParams_testReadability() {
        TestParams[] params = new TestParams[] {
            new TestParams("sequential", 3),
            new TestParams("edgebreaker", 4),
            new TestParams("edgebreaker", 10)
        };
        String[] fileNames = new String[] {
                "draco/testdata/test_nm.obj",
                "draco/testdata/cube_att.obj"
        };
        for(String fileName : fileNames) {
            for(TestParams param : params) {
                this.testGolden(param, fileName);
            }
        }
    }

}
