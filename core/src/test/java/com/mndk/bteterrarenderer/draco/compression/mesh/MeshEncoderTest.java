//// Copyright 2016 The Draco Authors.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////      http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//#include "draco/compression/mesh/mesh_encoder.h"
//
//#include "draco/compression/expert_encode.h"
//#include "draco/core/decoder_buffer.h"
//#include "draco/core/draco_test_base.h"
//#include "draco/core/draco_test_utils.h"
//#include "draco/io/obj_decoder.h"
//
//namespace draco {
//
//struct MeshEncoderTestParams {
//  MeshEncoderTestParams(const std::string &encoding_method, int cl)
//      : encoding_method(encoding_method), cl(cl) {}
//  std::string encoding_method;
//  int cl;
//};
//
//class MeshEncoderTest : public ::testing::TestWithParam<MeshEncoderTestParams> {
// protected:
//  MeshEncoderTest() {}
//
//  // Fills out_method with id of the encoding method used for the test.
//  // Returns false if the encoding method is not set properly.
//  bool GetMethod(MeshEncoderMethod *out_method) const {
//    if (GetParam().encoding_method == "sequential") {
//      *out_method = MESH_SEQUENTIAL_ENCODING;
//      return true;
//    }
//    if (GetParam().encoding_method == "edgebreaker") {
//      *out_method = MESH_EDGEBREAKER_ENCODING;
//      return true;
//    }
//    return false;
//  }
//
//  void TestGolden(const std::string &file_name) {
//    // This test verifies that a given set of meshes are encoded to an expected
//    // output. This is useful for catching bugs in code changes that are not
//    // supposed to change the encoding.
//    // The test is expected to fail when the encoding is modified. In such case,
//    // the golden files need to be updated to reflect the changes.
//    MeshEncoderMethod method;
//    ASSERT_TRUE(GetMethod(&method))
//        << "Test is run for an unknown encoding method";
//
//    std::string golden_file_name = file_name;
//    golden_file_name += '.';
//    golden_file_name += GetParam().encoding_method;
//    golden_file_name += ".cl";
//    golden_file_name += std::to_string(GetParam().cl);
//    golden_file_name += ".";
//    golden_file_name += std::to_string(kDracoMeshBitstreamVersionMajor);
//    golden_file_name += ".";
//    golden_file_name += std::to_string(kDracoMeshBitstreamVersionMinor);
//    golden_file_name += ".drc";
//    const std::unique_ptr<Mesh> mesh(ReadMeshFromTestFile(file_name));
//    ASSERT_NE(mesh, nullptr) << "Failed to load test model " << file_name;
//
//    ExpertEncoder encoder(*mesh);
//    encoder.SetEncodingMethod(method);
//    encoder.SetSpeedOptions(10 - GetParam().cl, 10 - GetParam().cl);
//    encoder.SetAttributeQuantization(0, 20);
//    for (int i = 1; i < mesh->num_attributes(); ++i) {
//      encoder.SetAttributeQuantization(i, 12);
//    }
//    EncoderBuffer buffer;
//    const Status status = encoder.EncodeToBuffer(&buffer);
//    EXPECT_TRUE(status.ok()) << "Failed encoding test mesh " << file_name
//                             << " with method " << GetParam().encoding_method;
//    DRACO_ASSERT_OK(status);
//    // Check that the encoded mesh was really encoded with the selected method.
//    DecoderBuffer decoder_buffer;
//    decoder_buffer.Init(buffer.data(), buffer.size());
//    decoder_buffer.Advance(8);  // Skip the header to the encoding method id.
//    uint8_t encoded_method;
//    ASSERT_TRUE(decoder_buffer.Decode(&encoded_method));
//    ASSERT_EQ(encoded_method, method);
//
//    if (!FLAGS_update_golden_files) {
//      EXPECT_TRUE(
//          CompareGoldenFile(golden_file_name, buffer.data(), buffer.size()))
//          << "Encoded data is different from the golden file. Please verify "
//             "that the encoding works as expected and update the golden file "
//             "if necessary (run the test with --update_golden_files flag).";
//    } else {
//      // Save the files into the local folder.
//      EXPECT_TRUE(
//          GenerateGoldenFile(golden_file_name, buffer.data(), buffer.size()))
//          << "Failed to generate new golden file for " << file_name;
//    }
//  }
//};
//
//TEST_P(MeshEncoderTest, EncodeGoldenMeshTestNm) { TestGolden("test_nm.obj"); }
//
//TEST_P(MeshEncoderTest, EncodeGoldenMeshCubeAtt) { TestGolden("cube_att.obj"); }
//
//INSTANTIATE_TEST_SUITE_P(
//    MeshEncoderTests, MeshEncoderTest,
//    ::testing::Values(MeshEncoderTestParams("sequential", 3),
//                      MeshEncoderTestParams("edgebreaker", 4),
//                      MeshEncoderTestParams("edgebreaker", 10)));
//
//}  // namespace draco

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.DracoExpertEncoder;
import com.mndk.bteterrarenderer.draco.compression.DracoTestFileUtil;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.StatusAssert;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import org.junit.Assert;
import org.junit.Test;

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
        Mesh mesh = DracoTestFileUtil.decode(fileName);
        // TEST vvv
        System.out.println(goldenFileName);
        // TEST ^^^
        Assert.assertNotNull("Failed to load test model " + fileName, mesh);

        DracoExpertEncoder encoder = new DracoExpertEncoder(mesh);
        encoder.setEncodingMethod(method.getValue());
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

        DracoTestFileUtil.compareGoldenFile(goldenFileName, buffer);
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
